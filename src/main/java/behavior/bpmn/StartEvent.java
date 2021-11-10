package behavior.bpmn;

import behavior.bpmn.auxiliary.ControleFlowNodeVisitor;

public class StartEvent extends ControleFlowNode {
    public StartEvent(String name) {
        super(name);
    }

    @Override
    void accept(ControleFlowNodeVisitor visitor) {
        visitor.handle(this);
    }
}
