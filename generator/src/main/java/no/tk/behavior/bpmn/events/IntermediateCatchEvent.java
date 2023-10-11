package no.tk.behavior.bpmn.events;

import com.google.common.base.Objects;
import no.tk.behavior.bpmn.auxiliary.visitors.EventVisitor;
import no.tk.behavior.bpmn.auxiliary.visitors.FlowElementVisitor;
import no.tk.behavior.bpmn.auxiliary.visitors.FlowNodeVisitor;
import no.tk.behavior.bpmn.events.definitions.EventDefinition;

public class IntermediateCatchEvent extends CatchEvent {
  private final IntermediateCatchEventType type;

  public IntermediateCatchEvent(String id, String name, IntermediateCatchEventType type) {
    this(id, name, type, EventDefinition.empty());
  }

  public IntermediateCatchEvent(
      String id, String name, IntermediateCatchEventType type, EventDefinition eventDefinition) {
    super(id, name, eventDefinition);
    this.type = type;
  }

  @Override
  public void accept(FlowElementVisitor visitor) {
    visitor.handle(this);
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

  public IntermediateCatchEventType getType() {
    return type;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof IntermediateCatchEvent that)) {
      return false;
    }
    if (!super.equals(o)) {
      return false;
    }
    return type == that.type && this.getEventDefinition().equals(that.getEventDefinition());
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(super.hashCode(), type, getEventDefinition().hashCode());
  }
}
