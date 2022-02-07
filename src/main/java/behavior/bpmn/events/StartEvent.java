package behavior.bpmn.events;

import behavior.bpmn.auxiliary.FlowNodeVisitor;

public class StartEvent extends CatchEvent {
    private final StartEventType type;

    public StartEvent(String name) {
        this(name, StartEventType.NONE);
    }

    public StartEvent(String name, StartEventType type) {
        this(name, type, EventDefinition.empty());
    }

    public StartEvent(String name, StartEventType type, EventDefinition eventDefinition) {
        super(name, eventDefinition);
        this.type = type;
    }

    @Override
    public void accept(FlowNodeVisitor visitor) {
        visitor.handle(this);
    }

    @Override
    public boolean isMessageOrSignalStartEvent() {
        return this.type == StartEventType.MESSAGE || this.type == StartEventType.SIGNAL;
    }

    public StartEventType getType() {
        return type;
    }
}
