package behavior.bpmn.events;

import behavior.bpmn.auxiliary.FlowNodeVisitor;

public class IntermediateCatchEvent extends CatchEvent {
    private final IntermediateEventType type;

    public IntermediateCatchEvent(String name, IntermediateEventType type) {
        this(name, type, EventDefinition.empty());
    }

    public IntermediateCatchEvent(String name, IntermediateEventType type, EventDefinition eventDefinition) {
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

    public IntermediateEventType getType() {
        return type;
    }
}
