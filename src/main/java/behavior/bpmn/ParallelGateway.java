package behavior.bpmn;

import behavior.bpmn.auxiliary.ControleFlowNodeVisitor;

public class ParallelGateway extends ControleFlowNode {
    public ParallelGateway(String name) {
        super(name);
    }

    @Override
    void accept(ControleFlowNodeVisitor visitor) {
        visitor.handle(this);
    }
}
