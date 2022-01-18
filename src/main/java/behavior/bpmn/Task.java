package behavior.bpmn;

import behavior.bpmn.auxiliary.FlowNodeVisitor;

// TODO: Split up into: ReceiveTask, SendTask and ServiceTask.
public class Task extends FlowNode {
    public Task(String name) {
        super(name);
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
        return true;
    }

    @Override
    public boolean isGateway() {
        return false;
    }

}
