package behavior.bpmn.events;

import behavior.bpmn.auxiliary.FlowNodeVisitor;

public class IntermediateCatchEvent extends CatchEvent {
    private final IntermediateEventType type;

    public IntermediateCatchEvent(String name, IntermediateEventType type) {
        super(name);
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
