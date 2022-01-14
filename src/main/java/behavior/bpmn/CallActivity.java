package behavior.bpmn;

import behavior.bpmn.auxiliary.ControlFlowNodeVisitor;

public class CallActivity extends ControlFlowNode {

    private final BPMNProcessModel subProcessModel;

    public CallActivity(BPMNProcessModel subProcessModel) {
        super(subProcessModel.getName());
        this.subProcessModel = subProcessModel;
    }

    public BPMNProcessModel getSubProcessModel() {
        return subProcessModel;
    }

    @Override
    public void accept(ControlFlowNodeVisitor visitor) {
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
