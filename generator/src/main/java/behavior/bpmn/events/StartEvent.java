package behavior.bpmn.events;

import behavior.bpmn.auxiliary.visitors.EventVisitor;
import behavior.bpmn.auxiliary.visitors.FlowNodeVisitor;
import behavior.bpmn.events.definitions.EventDefinition;
import com.google.common.base.Objects;

public class StartEvent extends CatchEvent {
  private final StartEventType type;
  /** Decides if the event is interrupting or non-interrupting. */
  private final boolean interrupt;

  public StartEvent(String id, String name) {
    this(id, name, StartEventType.NONE);
  }

  public StartEvent(String id, String name, StartEventType type) {
    this(id, name, type, EventDefinition.empty(), false);
  }

  public StartEvent(
      String id,
      String name,
      StartEventType type,
      EventDefinition eventDefinition,
      boolean interrupt) {
    super(id, name, eventDefinition);
    this.type = type;
    this.interrupt = interrupt;
  }
  public boolean isInterrupt() {
    return interrupt;
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
    return this.type == StartEventType.MESSAGE
        || this.type == StartEventType.SIGNAL
        || this.type == StartEventType.ERROR
        || this.type == StartEventType.ESCALATION;
  }

  public StartEventType getType() {
    return type;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof StartEvent)) {
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
