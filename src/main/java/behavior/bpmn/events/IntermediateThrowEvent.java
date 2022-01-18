package behavior.bpmn.events;

import behavior.bpmn.auxiliary.FlowNodeVisitor;

public class IntermediateThrowEvent extends IntermediateEvent {

    public IntermediateThrowEvent(String name, IntermediateEventType type) {
        super(name, type);
    }

    @Override
    public void accept(FlowNodeVisitor visitor) {
        visitor.handle(this);
    }
}
