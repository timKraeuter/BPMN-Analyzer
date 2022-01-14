package behavior.bpmn.events;

import behavior.bpmn.auxiliary.ControlFlowNodeVisitor;

public class IntermediateThrowEvent extends IntermediateEvent {

    public IntermediateThrowEvent(String name, IntermediateEventType type) {
        super(name, type);
    }

    @Override
    public void accept(ControlFlowNodeVisitor visitor) {
        visitor.handle(this);
    }
}
