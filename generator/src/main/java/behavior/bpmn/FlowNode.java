package behavior.bpmn;

import behavior.bpmn.auxiliary.visitors.FlowNodeVisitor;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Stream;

public abstract class FlowNode extends FlowElement {
    private final Set<SequenceFlow> outgoingFlows = new LinkedHashSet<>();
    private final Set<SequenceFlow> incomingFlows = new LinkedHashSet<>();

    protected FlowNode(String id, String name) {
        super(id, name);
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

    public abstract void accept(FlowNodeVisitor visitor);

    public abstract boolean isInclusiveGateway();

    /**
     * @return is the flow nodes should instantiate the process, such as instantiate receive tasks, message start events or signal start events.
     */
    public abstract boolean isInstantiateFlowNode();

    public abstract boolean isTask();

    public abstract boolean isGateway();

    public abstract boolean isExclusiveEventBasedGateway();
}
