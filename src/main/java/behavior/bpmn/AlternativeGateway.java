package behavior.bpmn;

import behavior.bpmn.auxiliary.ControlFlowNodeVisitor;

public class AlternativeGateway extends ControlFlowNode {
    public AlternativeGateway(String name) {
        super(name);
    }

    @Override
    public void accept(ControlFlowNodeVisitor visitor) {
        visitor.handle(this);
    }
}
