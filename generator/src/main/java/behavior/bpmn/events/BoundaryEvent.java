package behavior.bpmn.events;

import behavior.bpmn.activities.Activity;
import behavior.bpmn.auxiliary.EventVisitor;
import behavior.bpmn.auxiliary.FlowNodeVisitor;
import com.google.common.base.Objects;

public class BoundaryEvent extends CatchEvent {
    private final BoundaryEventType type;
    /**
     * Decides if the event is interrupting or non-interrupting.
     */
    private final boolean interrupt;
    private Activity attachedTo;

    public BoundaryEvent(String name, BoundaryEventType type, boolean interrupt, EventDefinition eventDefinition) {
        super(name, eventDefinition);
        this.type = type;
        this.interrupt = interrupt;
    }

    public BoundaryEvent(String name, BoundaryEventType type, boolean interrupt) {
        this(name, type, interrupt, EventDefinition.empty());
    }

    public BoundaryEventType getType() {
        return type;
    }

    public boolean isInterrupt() {
        return interrupt;
    }

    @Override
    public void accept(FlowNodeVisitor visitor) {
        // Never appears in the flow.
    }

    @Override
    public void accept(EventVisitor visitor) {
        // Never appears in the flow.
    }

    @Override
    public boolean isInstantiateFlowNode() {
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        BoundaryEvent that = (BoundaryEvent) o;
        return getName().equals(that.getName()) && interrupt == that.interrupt && type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getName(), type, interrupt);
    }

    public Activity getAttachedTo() {
        return attachedTo;
    }

    public void setAttachedTo(Activity attachedTo) {
        this.attachedTo = attachedTo;
    }
}
