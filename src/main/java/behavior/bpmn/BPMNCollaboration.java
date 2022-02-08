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

    public BPMNCollaboration(String name, Set<Process> participants, Set<Process> subprocesses, Set<MessageFlow> messageFlows) {
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
                                                .filter(process -> process.getControlFlowNodes().anyMatch(flowNode -> flowNode == flow.getTarget()))
                                                .findFirst();
        if (optionalProcess.isPresent()) {
            return optionalProcess.get();
        }
        // The message flow must go to a subprocess!.
        return subprocesses.stream()
                           .filter(process -> process.getControlFlowNodes().anyMatch(flowNode -> flowNode == flow.getTarget()))
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
}
