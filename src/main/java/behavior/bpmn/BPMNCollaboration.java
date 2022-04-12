package behavior.bpmn;

import behavior.Behavior;
import behavior.BehaviorVisitor;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class BPMNCollaboration implements Behavior {
    private final String name;
    private final Set<Process> participants;
    /**
     * Derived from call activities of the participants.
     */
    private final Set<Process> subprocesses;
    private final Set<MessageFlow> messageFlows;

    public BPMNCollaboration(String name,
                             Set<Process> participants,
                             Set<Process> subprocesses,
                             Set<MessageFlow> messageFlows) {
        this.name = name;
        this.participants = participants;
        this.subprocesses = subprocesses;
        this.messageFlows = messageFlows;
    }

    public Set<Process> getParticipants() {
        return participants;
    }

    public Set<MessageFlow> getMessageFlows() {
        return messageFlows;
    }

    public Set<MessageFlow> getIncomingMessageFlows(FlowNode node) {
        return this.getMessageFlows().stream()
                   .filter(flow -> flow.getTarget().equals(node))
                   .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public Process getMessageFlowReceiver(MessageFlow flow) {
        Optional<Process> optionalProcess = this.getParticipants().stream()
                                                .filter(process -> process.getControlFlowNodes().anyMatch(flowNode ->
                                                                                                                  flowNode ==
                                                                                                                  flow.getTarget()))
                                                .findFirst();
        if (optionalProcess.isPresent()) {
            return optionalProcess.get();
        }
        // The message flow must go to a subprocess!.
        return subprocesses.stream()
                           .filter(process -> process.getControlFlowNodes().anyMatch(flowNode -> flowNode ==
                                                                                                 flow.getTarget()))
                           .findFirst().get();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void accept(BehaviorVisitor visitor) {
        visitor.handle(this);
    }

    public AbstractProcess getParentProcessForEventSubprocess(EventSubprocess eventSubprocess) {
        // TODO: Digg multiple levels deep!
        final Optional<Process> foundParentProcess = this.participants.stream()
                                                                      .filter(process -> process.getEventSubprocesses()
                                                                                                .anyMatch(
                                                                                                        eventSubprocess1 -> eventSubprocess1.equals(
                                                                                                                eventSubprocess)))
                                                                      .findFirst();
        if (foundParentProcess.isPresent()) {
            return foundParentProcess.get();
        }
        throw new RuntimeException("Parent process could not be found for event subprocess!" + eventSubprocess);
    }


    public AbstractProcess findProcessForFlowNode(FlowNode flowNode) {
        for (Process participant : this.getParticipants()) {
            final boolean processFound =
                    participant.getControlFlowNodes().anyMatch(subProcessFlowNode -> subProcessFlowNode.equals(flowNode));
            if (processFound) {
                return participant;
            }
            // TODO: Subprocesses of subprocesses?
            Optional<Process> optionalSubprocess =
                    participant.getSubProcesses().filter(subprocess -> subprocess.getControlFlowNodes().anyMatch(
                            subProcessFlowNode -> subProcessFlowNode.equals(flowNode))).findFirst();
            if (optionalSubprocess.isPresent()) {
                return optionalSubprocess.get();
            }
            final Optional<EventSubprocess> optionalEventSubprocess = participant.getEventSubprocesses().filter(
                    eventSubprocess -> eventSubprocess.getStartEvents().stream().anyMatch(startEvent -> startEvent.equals(
                            flowNode))).findFirst();
            if (optionalEventSubprocess.isPresent()) {
                return optionalEventSubprocess.get();
            }
        }
        // Should not happen.
        throw new RuntimeException(String.format("No process for the flow node %s found!", flowNode));
    }
}
