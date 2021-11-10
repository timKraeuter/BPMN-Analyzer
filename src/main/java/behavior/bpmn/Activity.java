package behavior.bpmn;

import behavior.bpmn.auxiliary.ControleFlowNodeVisitor;

public class Activity extends ControleFlowNode {
    public Activity(String name) {
        super(name);
    }

    @Override
    void accept(ControleFlowNodeVisitor visitor) {
        visitor.handle(this);
    }
}
