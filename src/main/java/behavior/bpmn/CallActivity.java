package behavior.bpmn;

import behavior.bpmn.auxiliary.FlowNodeVisitor;

public class CallActivity extends FlowNode {

    private final BPMNProcessModel subProcessModel;

    public CallActivity(BPMNProcessModel subProcessModel) {
        super(subProcessModel.getName());
        this.subProcessModel = subProcessModel;
    }

    public BPMNProcessModel getSubProcessModel() {
        return subProcessModel;
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
    public boolean isTask() {
        return false;
    }

    @Override
    public boolean isGateway() {
        return false;
    }
}
