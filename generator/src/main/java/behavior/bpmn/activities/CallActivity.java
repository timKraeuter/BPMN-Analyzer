package behavior.bpmn.activities;

import behavior.bpmn.Process;
import behavior.bpmn.auxiliary.ActivityVisitor;
import behavior.bpmn.auxiliary.FlowNodeVisitor;
import com.google.common.base.Objects;

public class CallActivity extends Activity {

    private final Process subProcessModel;

    public CallActivity(String id, Process subProcessModel) {
        super(id, subProcessModel.getName());
        this.subProcessModel = subProcessModel;
    }

    public Process getSubProcessModel() {
        return subProcessModel;
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
        if (o == null || getClass() != o.getClass()) {
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
