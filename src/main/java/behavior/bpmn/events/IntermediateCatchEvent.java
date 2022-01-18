package behavior.bpmn.events;

import behavior.bpmn.auxiliary.FlowNodeVisitor;

public class IntermediateCatchEvent extends IntermediateEvent {

    public IntermediateCatchEvent(String name, IntermediateEventType type) {
        super(name, type);
    }

    @Override
    public void accept(FlowNodeVisitor visitor) {
        visitor.handle(this);
    }
}
