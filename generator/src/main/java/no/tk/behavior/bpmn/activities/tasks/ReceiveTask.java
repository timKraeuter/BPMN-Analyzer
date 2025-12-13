package no.tk.behavior.bpmn.activities.tasks;

import com.google.common.base.Objects;
import no.tk.behavior.bpmn.auxiliary.visitors.ActivityVisitor;
import no.tk.behavior.bpmn.auxiliary.visitors.FlowElementVisitor;
import no.tk.behavior.bpmn.auxiliary.visitors.FlowNodeVisitor;

/** Represents a ReceiveTask. Inheritance hierarchy similar to the BPMN spec (Fig.10.10). */
public class ReceiveTask extends AbstractTask {
  private final boolean instantiate;

  public ReceiveTask(String id, String name) {
    this(id, name, false);
  }

  @Override
  public void accept(ActivityVisitor visitor) {
    visitor.handle(this);
  }

  public ReceiveTask(String id, String name, boolean instantiate) {
    super(id, name);
    this.instantiate = instantiate;
  }

  public boolean isInstantiate() {
    return instantiate;
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
  public boolean isInstantiateFlowNode() {
    return isInstantiate();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof ReceiveTask that)) {
      return false;
    }
    if (!super.equals(o)) {
      return false;
    }
    return instantiate == that.instantiate;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(super.hashCode(), instantiate);
  }
}
