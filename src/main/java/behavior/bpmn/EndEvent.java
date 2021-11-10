package behavior.bpmn;

import behavior.bpmn.auxiliary.ControleFlowNodeVisitor;

public class EndEvent extends ControleFlowNode {

    public EndEvent(String name) {
        super(name);
    }

    @Override
    void accept(ControleFlowNodeVisitor visitor) {
        visitor.handle(this);
    }
}
