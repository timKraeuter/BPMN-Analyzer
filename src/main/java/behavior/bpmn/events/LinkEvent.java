package behavior.bpmn.events;

import behavior.bpmn.auxiliary.ControlFlowNodeVisitor;

public class LinkEvent extends Event {
    private final LinkEventType type;

    public LinkEvent(String name, LinkEventType type) {
        super(name);
        this.type = type;
    }


    @Override
    public void accept(ControlFlowNodeVisitor visitor) {
        visitor.handle(this);
    }

    public LinkEventType getType() {
        return this.type;
    }
}
