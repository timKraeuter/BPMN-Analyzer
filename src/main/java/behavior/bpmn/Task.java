package behavior.bpmn;

import behavior.bpmn.auxiliary.ControlFlowNodeVisitor;

public class Task extends ControlFlowNode {
    public Task(String name) {
        super(name);
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
        return true;
    }

    @Override
    public boolean isGateway() {
        return false;
    }

}
