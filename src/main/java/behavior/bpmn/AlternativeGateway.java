package behavior.bpmn;

import behavior.bpmn.auxiliary.ControleFlowNodeVisitor;

public class AlternativeGateway extends ControleFlowNode {
    public AlternativeGateway(String name) {
        super(name);
    }

    @Override
    void accept(ControleFlowNodeVisitor visitor) {
        visitor.handle(this);
    }
}
