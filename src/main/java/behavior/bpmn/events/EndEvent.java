package behavior.bpmn.events;

import behavior.bpmn.ControlFlowNode;
import behavior.bpmn.auxiliary.ControlFlowNodeVisitor;

public class EndEvent extends ControlFlowNode {

    public EndEvent(String name) {
        super(name);
    }

    @Override
    public void accept(ControlFlowNodeVisitor visitor) {
        visitor.handle(this);
    }

}
