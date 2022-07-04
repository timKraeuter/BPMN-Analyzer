package behavior.bpmn.events;

import behavior.bpmn.auxiliary.EventVisitor;
import behavior.bpmn.auxiliary.FlowNodeVisitor;
import com.google.common.base.Objects;

public class StartEvent extends CatchEvent {
    private final StartEventType type;

    public StartEvent(String id, String name) {
        this(id, name, StartEventType.NONE);
    }

    public StartEvent(String id, String name, StartEventType type) {
        this(id, name, type, EventDefinition.empty());
    }

    public StartEvent(String id, String name, StartEventType type, EventDefinition eventDefinition) {
        super(id, name, eventDefinition);
        this.type = type;
    }

    @Override
    public void accept(FlowNodeVisitor visitor) {
        visitor.handle(this);
    }

    @Override
    public void accept(EventVisitor visitor) {
        visitor.handle(this);
    }

    @Override
    public boolean isInstantiateFlowNode() {
        return this.type == StartEventType.MESSAGE ||
               this.type == StartEventType.SIGNAL ||
               this.type == StartEventType.MESSAGE_NON_INTERRUPTING ||
               this.type == StartEventType.SIGNAL_NON_INTERRUPTING;
    }

    public StartEventType getType() {
        return type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        StartEvent that = (StartEvent) o;
        return type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(super.hashCode(), type);
    }
}
