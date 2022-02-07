package behavior.bpmn;

import behavior.Behavior;
import behavior.BehaviorVisitor;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class BPMNCollaboration implements Behavior {
    private final String name;
    private final Set<Process> participants;
    private final Set<MessageFlow> messageFlows;

    public BPMNCollaboration(String name, Set<Process> participants, Set<MessageFlow> messageFlows) {
        this.name = name;
        this.participants = participants;
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
        return this.getParticipants().stream()
                   .filter(process -> process.getControlFlowNodes().anyMatch(flowNode -> flowNode == flow.getTarget()))
                   .findFirst()
                   .get(); // Must exist.
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
