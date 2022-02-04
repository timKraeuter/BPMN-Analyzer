package behavior.bpmn.events;

import behavior.bpmn.auxiliary.FlowNodeVisitor;

public class IntermediateThrowEvent extends ThrowEvent {
    private final IntermediateEventType type;

    public IntermediateThrowEvent(String name, IntermediateEventType type) {
        this(name, type, EventDefinition.empty());
    }

    public IntermediateThrowEvent(String name, IntermediateEventType type, EventDefinition eventDefinition) {
        super(name, eventDefinition);
        this.type = type;
    }

    @Override
    public void accept(FlowNodeVisitor visitor) {
        visitor.handle(this);
    }

    public IntermediateEventType getType() {
        return type;
    }
}
