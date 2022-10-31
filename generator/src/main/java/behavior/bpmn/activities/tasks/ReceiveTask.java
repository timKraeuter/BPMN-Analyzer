package behavior.bpmn.activities.tasks;

import behavior.bpmn.auxiliary.visitors.ActivityVisitor;
import behavior.bpmn.auxiliary.visitors.FlowNodeVisitor;
import com.google.common.base.Objects;

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
    if (!(o instanceof ReceiveTask)) {
      return false;
    }
    if (!super.equals(o)) {
      return false;
    }
    ReceiveTask that = (ReceiveTask) o;
    return instantiate == that.instantiate;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(super.hashCode(), instantiate);
  }
}
