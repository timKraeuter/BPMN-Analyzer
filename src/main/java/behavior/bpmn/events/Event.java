package behavior.bpmn.events;

import behavior.bpmn.ControlFlowNode;

public abstract class Event extends ControlFlowNode {

    public Event(String name) {
        super(name);
    }

    @Override
    public boolean isInclusiveGateway() {
        return false;
    }

    @Override
    public boolean isTask() {
        return false;
    }

    @Override
    public boolean isGateway() {
        return false;
    }
}
