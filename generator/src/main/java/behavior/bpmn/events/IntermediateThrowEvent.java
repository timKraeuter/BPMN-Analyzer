package behavior.bpmn.events;

import behavior.bpmn.auxiliary.EventVisitor;
import behavior.bpmn.auxiliary.FlowNodeVisitor;
import com.google.common.base.Objects;

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
    public void accept(EventVisitor visitor) {
        visitor.handle(this);
    }

    @Override
    public boolean isInstantiateFlowNode() {
        return false;
    }

    public IntermediateThrowEventType getType() {
        return type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IntermediateThrowEvent that = (IntermediateThrowEvent) o;
        return getName().equals(that.getName())
                && type == that.type
                && getEventDefinition().equals(that.getEventDefinition());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getName(), type, getEventDefinition());
    }
}
