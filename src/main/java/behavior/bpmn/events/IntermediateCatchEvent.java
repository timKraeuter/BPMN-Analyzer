package behavior.bpmn.events;

import behavior.bpmn.auxiliary.FlowNodeVisitor;
import com.google.common.base.Objects;

public class IntermediateCatchEvent extends CatchEvent {
    private final IntermediateCatchEventType type;

    public IntermediateCatchEvent(String name, IntermediateCatchEventType type) {
        this(name, type, EventDefinition.empty());
    }

    public IntermediateCatchEvent(String name, IntermediateCatchEventType type, EventDefinition eventDefinition) {
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

    public IntermediateCatchEventType getType() {
        return type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IntermediateCatchEvent that = (IntermediateCatchEvent) o;
        return getName().equals(that.getName())
                && type == that.type
                && getEventDefinition().equals(that.getEventDefinition());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getName(), type, getEventDefinition());
    }
}
