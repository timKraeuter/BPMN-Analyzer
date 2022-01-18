package behavior.bpmn.events;

import behavior.bpmn.auxiliary.FlowNodeVisitor;

public class StartEvent extends Event {
    public StartEvent(String name) {
        super(name);
    }

    @Override
    public void accept(FlowNodeVisitor visitor) {
        visitor.handle(this);
    }

}
