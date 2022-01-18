package behavior.bpmn.events;

import behavior.bpmn.auxiliary.FlowNodeVisitor;

public class StartEvent extends CatchEvent {
    private StartEventType type;

    public StartEvent(String name) {
        this(name, StartEventType.NONE);
    }

    public StartEvent(String name, StartEventType type) {
        super(name);
        this.type = type;
    }

    @Override
    public void accept(FlowNodeVisitor visitor) {
        visitor.handle(this);
    }

    public StartEventType getType() {
        return type;
    }
}
