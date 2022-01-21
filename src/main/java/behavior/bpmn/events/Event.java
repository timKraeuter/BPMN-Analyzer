package behavior.bpmn.events;

import behavior.bpmn.FlowNode;

public abstract class Event extends FlowNode {

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

    @Override
    public boolean isInstantiateReceiveTask() {
        return false;
    }

    @Override
    public boolean isExclusiveEventBasedGateway() {
        return false;
    }
}
