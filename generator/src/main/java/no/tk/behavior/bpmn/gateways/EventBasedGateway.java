package no.tk.behavior.bpmn.gateways;

import com.google.common.base.Objects;
import no.tk.behavior.bpmn.auxiliary.visitors.FlowElementVisitor;
import no.tk.behavior.bpmn.auxiliary.visitors.FlowNodeVisitor;

/** Represents and exclusive event based gateway (parallel ones are currently not supported). */
public class EventBasedGateway extends Gateway {

  private final boolean instantiate;

  public EventBasedGateway(String id, String name) {
    this(id, name, false);
  }

  public EventBasedGateway(String id, String name, boolean instantiate) {
    super(id, name);
    this.instantiate = instantiate;
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
  public boolean isInclusiveGateway() {
    return false;
  }

  @Override
  public boolean isInstantiateFlowNode() {
    return instantiate;
  }

  @Override
  public boolean isExclusiveEventBasedGateway() {
    return true;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof EventBasedGateway)) {
      return false;
    }
    if (!super.equals(o)) {
      return false;
    }
    EventBasedGateway that = (EventBasedGateway) o;
    return instantiate == that.instantiate;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(super.hashCode(), instantiate);
  }
}
