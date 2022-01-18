package behavior.bpmn.activities;

import behavior.bpmn.Process;
import behavior.bpmn.auxiliary.FlowNodeVisitor;

public class CallActivity extends Activity {

    private final Process subProcessModel;

    public CallActivity(Process subProcessModel) {
        super(subProcessModel.getName());
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
    public boolean isTask() {
        return false;
    }
}
