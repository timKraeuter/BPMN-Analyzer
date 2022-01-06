package behavior.bpmn.events;

import behavior.bpmn.auxiliary.ControlFlowNodeVisitor;

public class EndEvent extends Event {

    public EndEvent(String name) {
        super(name);
    }

    @Override
    public void accept(ControlFlowNodeVisitor visitor) {
        visitor.handle(this);
    }

}
