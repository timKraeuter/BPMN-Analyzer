package behavior.bpmn.events;

import behavior.bpmn.auxiliary.FlowNodeVisitor;

public class EndEvent extends ThrowEvent {

    private final EndEventType type;

    /**
     * Creates and end event of the default type "None".
     */
    public EndEvent(String name) {
        this(name, EndEventType.NONE);
    }

    /**
     * Creates and end event of the given type.
     */
    public EndEvent(String name, EndEventType type) {
        super(name);
        this.type = type;
    }

    public EndEventType getType() {
        return this.type;
    }

    @Override
    public void accept(FlowNodeVisitor visitor) {
        visitor.handle(this);
    }

}
