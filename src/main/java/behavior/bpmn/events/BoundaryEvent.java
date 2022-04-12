package behavior.bpmn.events;

import behavior.bpmn.auxiliary.EventVisitor;
import behavior.bpmn.auxiliary.FlowNodeVisitor;

public class BoundaryEvent extends CatchEvent {
    private final BoundaryEventType type;
    /**
     * Decides if the event is interrupting or non-interrupting.
     */
    private final boolean cancelActivity;

    public BoundaryEvent(String name, BoundaryEventType type, boolean cancelActivity, EventDefinition eventDefinition) {
        super(name, eventDefinition);
        this.type = type;
        this.cancelActivity = cancelActivity;
    }

    public BoundaryEvent(String name, BoundaryEventType type, boolean cancelActivity) {
        this(name, type, cancelActivity, EventDefinition.empty());
    }

    public BoundaryEventType getType() {
        return type;
    }

    public boolean isCancelActivity() {
        return cancelActivity;
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
}
