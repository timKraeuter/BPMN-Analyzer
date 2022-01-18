package behavior.bpmn.activities;

import behavior.bpmn.BPMNProcess;
import behavior.bpmn.auxiliary.FlowNodeVisitor;

public class CallActivity extends Activity {

    private final BPMNProcess subProcessModel;

    public CallActivity(BPMNProcess subProcessModel) {
        super(subProcessModel.getName());
        this.subProcessModel = subProcessModel;
    }

    public BPMNProcess getSubProcessModel() {
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
