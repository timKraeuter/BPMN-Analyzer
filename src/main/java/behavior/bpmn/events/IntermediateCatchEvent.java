package behavior.bpmn.events;

import behavior.bpmn.auxiliary.ControlFlowNodeVisitor;

public class IntermediateCatchEvent extends IntermediateEvent {

    public IntermediateCatchEvent(String name, IntermediateEventType type) {
        super(name, type);
    }

    @Override
    public void accept(ControlFlowNodeVisitor visitor) {
        visitor.handle(this);
    }
}
