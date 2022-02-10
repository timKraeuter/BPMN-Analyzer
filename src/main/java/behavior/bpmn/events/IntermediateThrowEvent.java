package behavior.bpmn.events;

import behavior.bpmn.auxiliary.FlowNodeVisitor;

public class IntermediateThrowEvent extends ThrowEvent {
    private final IntermediateThrowEventType type;

    public IntermediateThrowEvent(String name, IntermediateThrowEventType type) {
        this(name, type, EventDefinition.empty());
    }

    public IntermediateThrowEvent(String name, IntermediateThrowEventType type, EventDefinition eventDefinition) {
        super(name, eventDefinition);
        this.type = type;
    }

    @Override
    public void accept(FlowNodeVisitor visitor) {
        visitor.handle(this);
    }

    @Override
    public boolean isInstantiateFlowNode() {
        return false;
    }

    public IntermediateThrowEventType getType() {
        return type;
    }
}
