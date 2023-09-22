package no.tk.behavior.bpmn.activities;

import com.google.common.base.Objects;
import no.tk.behavior.bpmn.BPMNProcess;
import no.tk.behavior.bpmn.auxiliary.visitors.ActivityVisitor;
import no.tk.behavior.bpmn.auxiliary.visitors.FlowElementVisitor;
import no.tk.behavior.bpmn.auxiliary.visitors.FlowNodeVisitor;

public class CallActivity extends Activity {

  private final BPMNProcess subProcessModel;

  public CallActivity(String id, BPMNProcess subProcessModel) {
    super(id, subProcessModel.getName());
    this.subProcessModel = subProcessModel;
    subProcessModel.setCallActivity(this);
  }

  public BPMNProcess getSubProcessModel() {
    return subProcessModel;
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
    return false;
  }

  @Override
  public boolean isTask() {
    return false;
  }

  @Override
  public void accept(ActivityVisitor visitor) {
    visitor.handle(this);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof CallActivity)) {
      return false;
    }
    if (!super.equals(o)) {
      return false;
    }
    CallActivity that = (CallActivity) o;
    return Objects.equal(subProcessModel, that.subProcessModel);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(super.hashCode(), subProcessModel);
  }
}
