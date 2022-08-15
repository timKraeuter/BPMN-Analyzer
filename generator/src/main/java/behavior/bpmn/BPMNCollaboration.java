package behavior.bpmn;

import behavior.Behavior;
import behavior.BehaviorVisitor;
import behavior.bpmn.auxiliary.exceptions.ShouldNotHappenRuntimeException;
import behavior.bpmn.events.BoundaryEvent;
import behavior.bpmn.events.Event;
import behavior.bpmn.events.EventDefinition;
import org.apache.commons.lang3.tuple.Pair;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class BPMNCollaboration implements Behavior {
    private final String name;
    private final Set<Process> participants;
    /**
     * Recursively derived from all call activities of the participants.
     */
    private final Set<Process> allSubprocesses;
    private final Set<MessageFlow> messageFlows;

    public BPMNCollaboration(String name,
                             Set<Process> participants,
                             Set<Process> allSubprocesses,
                             Set<MessageFlow> messageFlows) {
        this.name = name;
        this.participants = participants;
        this.allSubprocesses = allSubprocesses;
        this.messageFlows = messageFlows;
    }

    public Set<Process> getParticipants() {
        return participants;
    }

    public Set<MessageFlow> getMessageFlows() {
        return messageFlows;
    }


    public Set<MessageFlow> outgoingMessageFlows(FlowNode producingMessageFlowNode) {
        return this.getMessageFlows().stream().filter(messageFlow -> messageFlow.getSource() ==
                                                                     producingMessageFlowNode).collect(Collectors.toCollection(
                LinkedHashSet::new));
    }

    public Set<MessageFlow> getIncomingMessageFlows(FlowNode messageTarget) {
        return this.getMessageFlows().stream().filter(flow -> flow.getTarget().equals(messageTarget)).collect(Collectors.toCollection(
                LinkedHashSet::new));
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void accept(BehaviorVisitor visitor) {
        visitor.handle(this);
    }

    /**
     * @return the parent process or the process itself if it is at the top level of the collaboration.
     */
    public AbstractProcess getParentProcess(AbstractProcess process) {
        for (Process participant : participants) {
            if (participant.equals(process)) {
                return process; // Process is at the top level
            }
            if (participant.getSubProcesses().anyMatch(Predicate.isEqual(process))) {
                return participant;
            }
            if (participant.getEventSubprocesses().anyMatch(Predicate.isEqual(process))) {
                return participant;
            }
        }
        for (Process subprocess : allSubprocesses) {
            if (subprocess.getSubProcesses().anyMatch(Predicate.isEqual(process))) {
                return subprocess;
            }
            if (subprocess.getEventSubprocesses().anyMatch(Predicate.isEqual(process))) {
                return subprocess;
            }
        }
        throw new ShouldNotHappenRuntimeException("Parent process could not be found for event subprocess!" +
                                                  process);
    }

    public AbstractProcess getMessageFlowReceiverProcess(MessageFlow flow) {
        return findProcessForFlowNode(flow.getTarget());
    }

    public AbstractProcess findProcessForFlowNode(FlowNode flowNode) {
        // Check participants and der event subprocesses first.
        for (Process participant : this.getParticipants()) {
            if (participant.getFlowNodes().anyMatch(Predicate.isEqual(flowNode))) {
                return participant;
            }
            final Optional<EventSubprocess> eventSubprocess = getFromEVSubprocessIfExists(flowNode, participant);
            if (eventSubprocess.isPresent()) {
                return eventSubprocess.get();
            }
        }
        // Check all other subprocesses and their event subprocesses.
        for (Process subprocess : allSubprocesses) {
            if (subprocess.getFlowNodes().anyMatch(Predicate.isEqual(flowNode))) {
                return subprocess;
            }
            final Optional<EventSubprocess> eventSubprocess = getFromEVSubprocessIfExists(flowNode, subprocess);
            if (eventSubprocess.isPresent()) {
                return eventSubprocess.get();
            }
        }
        // Should not happen.
        throw new ShouldNotHappenRuntimeException(String.format("No process for the flow node %s found!", flowNode));
    }

    private Optional<EventSubprocess> getFromEVSubprocessIfExists(FlowNode flowNode, Process participant) {
        return participant.getEventSubprocesses()
                          .filter(evSubprocess -> evSubprocess.getFlowNodes().anyMatch(Predicate.isEqual(flowNode)))
                          .findFirst();
    }


    public Pair<Set<Event>, Set<BoundaryEvent>> findAllCorrespondingSignalCatchEvents(EventDefinition eventDefinition) {
        Set<Event> signalCatchEvents = new LinkedHashSet<>();
        Set<BoundaryEvent> signalBoundaryCatchEvents = new LinkedHashSet<>();
        Set<Process> seenProcesses = new HashSet<>();
        this.getParticipants().forEach(process -> {
            Pair<Set<Event>, Set<BoundaryEvent>> signalAndSignalBoundaryCatchEvents =
                    findAllCorrespondingSignalCatchEvents(
                            process,
                            eventDefinition,
                            seenProcesses);
            signalCatchEvents.addAll(signalAndSignalBoundaryCatchEvents.getLeft());
            signalBoundaryCatchEvents.addAll(signalAndSignalBoundaryCatchEvents.getRight());
        });
        return Pair.of(signalCatchEvents, signalBoundaryCatchEvents);
    }

    Pair<Set<Event>, Set<BoundaryEvent>> findAllCorrespondingSignalCatchEvents(Process process,
                                                                               EventDefinition eventDefinition,
                                                                               Set<Process> seenProcesses) {
        Set<Event> signalCatchEvents = new LinkedHashSet<>();
        Set<BoundaryEvent> signalBoundaryCatchEvents = new LinkedHashSet<>();
        if (seenProcesses.contains(process)) {
            return Pair.of(signalCatchEvents, signalBoundaryCatchEvents);
        }
        seenProcesses.add(process);

        process.getFlowNodes().forEach(flowNode -> flowNode.accept(new SignalCatchEventFlowNodeVisitor(this,
                                                                                                       eventDefinition,
                                                                                                       signalCatchEvents,
                                                                                                       signalBoundaryCatchEvents,
                                                                                                       seenProcesses)

        ));
        process.getEventSubprocesses().forEach(eventSubprocess -> eventSubprocess.getFlowNodes().forEach(flowNode -> flowNode.accept(
                new SignalCatchEventFlowNodeVisitor(this,
                                                    eventDefinition,
                                                    signalCatchEvents,
                                                    signalBoundaryCatchEvents,
                                                    seenProcesses))));
        return Pair.of(signalCatchEvents, signalBoundaryCatchEvents);
    }
}
