package behavior.bpmn.events;

import behavior.bpmn.auxiliary.FlowNodeVisitor;
import com.google.common.base.Objects;

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
        this(name, type, EventDefinition.empty());
    }

    /**
     * Creates and end event of the given type with an event definition.
     */
    public EndEvent(String name, EndEventType type, EventDefinition eventDefinition) {
        super(name, eventDefinition);
        this.type = type;
    }

    public EndEventType getType() {
        return this.type;
    }

    @Override
    public void accept(FlowNodeVisitor visitor) {
        visitor.handle(this);
    }

    @Override
    public boolean isInstantiateFlowNode() {
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EndEvent endEvent = (EndEvent) o;
        return getName().equals(endEvent.getName())
                && type == endEvent.type
                && this.getEventDefinition().equals(endEvent.getEventDefinition());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getName(), type, this.getEventDefinition());
    }
}
