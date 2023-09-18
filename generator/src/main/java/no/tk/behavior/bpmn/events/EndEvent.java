package no.tk.behavior.bpmn.events;

import no.tk.behavior.bpmn.auxiliary.visitors.EventVisitor;
import no.tk.behavior.bpmn.auxiliary.visitors.FlowNodeVisitor;
import no.tk.behavior.bpmn.events.definitions.EventDefinition;
import com.google.common.base.Objects;

public class EndEvent extends ThrowEvent {

  private final EndEventType type;

  /** Creates and end event of the default type "None". */
  public EndEvent(String id, String name) {
    this(id, name, EndEventType.NONE);
  }

  /** Creates and end event of the given type. */
  public EndEvent(String id, String name, EndEventType type) {
    this(id, name, type, EventDefinition.empty());
  }

  /** Creates and end event of the given type with an event definition. */
  public EndEvent(String id, String name, EndEventType type, EventDefinition eventDefinition) {
    super(id, name, eventDefinition);
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
  public void accept(EventVisitor visitor) {
    visitor.handle(this);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof EndEvent)) {
      return false;
    }
    if (!super.equals(o)) {
      return false;
    }
    EndEvent endEvent = (EndEvent) o;
    return type == endEvent.type;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(super.hashCode(), type);
  }
}
