package behavior.bpmn.activities.tasks;

import behavior.bpmn.auxiliary.FlowNodeVisitor;

public class SendTask extends AbstractTask {
    public SendTask(String name) {
        super(name);
    }

    @Override
    public void accept(FlowNodeVisitor visitor) {
        visitor.handle(this);
    }

    @Override
    public boolean isInstantiateFlowNode() {
        return false;
    }
}
