package behavior.bpmn.events;

import behavior.bpmn.activities.Activity;
import behavior.bpmn.auxiliary.visitors.EventVisitor;
import behavior.bpmn.auxiliary.visitors.FlowNodeVisitor;
import behavior.bpmn.events.definitions.EventDefinition;
import com.google.common.base.Objects;

public class BoundaryEvent extends CatchEvent {
  private final BoundaryEventType type;
  /** Decides if the event is interrupting or non-interrupting. */
  private final boolean interrupt;

  private Activity attachedTo;

  public BoundaryEvent(
      String id,
      String name,
      BoundaryEventType type,
      boolean interrupt,
      EventDefinition eventDefinition) {
    super(id, name, eventDefinition);
    this.type = type;
    this.interrupt = interrupt;
  }

  public BoundaryEvent(String id, String name, BoundaryEventType type, boolean interrupt) {
    this(id, name, type, interrupt, EventDefinition.empty());
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

  public Activity getAttachedTo() {
    return attachedTo;
  }

  public void setAttachedTo(Activity attachedTo) {
    this.attachedTo = attachedTo;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof BoundaryEvent)) {
      return false;
    }
    if (!super.equals(o)) {
      return false;
    }
    BoundaryEvent that = (BoundaryEvent) o;
    return interrupt == that.interrupt && type == that.type;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(super.hashCode(), type, interrupt);
  }
}
