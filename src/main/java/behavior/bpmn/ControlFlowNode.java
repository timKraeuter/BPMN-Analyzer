package behavior.bpmn;

import behavior.bpmn.auxiliary.ControlFlowNodeVisitor;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Stream;

public abstract class ControlFlowNode {
    private final String name;
    private final Set<SequenceFlow> outgoingFlows = new LinkedHashSet<>();
    private final Set<SequenceFlow> incomingFlows = new LinkedHashSet<>();

    public ControlFlowNode(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public void addOutgoingSequenceFlow(SequenceFlow outgoingFlow) {
        this.outgoingFlows.add(outgoingFlow);
    }

    public void addIncomingSequenceFlow(SequenceFlow incomingFlow) {
        this.incomingFlows.add(incomingFlow);
    }

    public Stream<SequenceFlow> getOutgoingFlows() {
        return this.outgoingFlows.stream();
    }

    public Stream<SequenceFlow> getIncomingFlows() {
        return this.incomingFlows.stream();
    }

    public abstract void accept(ControlFlowNodeVisitor visitor);

    public abstract boolean isInclusiveGateway();

    public abstract boolean isTask();

    public abstract boolean isGateway();
}
