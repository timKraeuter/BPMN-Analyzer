package behavior.bpmn;

import behavior.bpmn.auxiliary.ControlFlowNodeVisitor;

public class ExclusiveGateway extends ControlFlowNode {
    public ExclusiveGateway(String name) {
        super(name);
    }

    @Override
    public void accept(ControlFlowNodeVisitor visitor) {
        visitor.handle(this);
    }

    @Override
    public boolean isParallelGateway() {
        return false;
    }
}
