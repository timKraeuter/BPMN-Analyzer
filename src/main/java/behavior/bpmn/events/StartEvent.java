package behavior.bpmn.events;

import behavior.bpmn.auxiliary.ControlFlowNodeVisitor;

public class StartEvent extends Event {
    public StartEvent(String name) {
        super(name);
    }

    @Override
    public void accept(ControlFlowNodeVisitor visitor) {
        visitor.handle(this);
    }

}
