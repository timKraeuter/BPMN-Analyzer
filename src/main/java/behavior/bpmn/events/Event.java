package behavior.bpmn.events;

import behavior.bpmn.FlowNode;

public abstract class Event extends FlowNode {

    private EventDefinition eventDefinition;

    public Event(String name, EventDefinition eventDefinition) {
        super(name);
        this.eventDefinition = eventDefinition;
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

    public EventDefinition getEventDefinition() {
        return eventDefinition;
    }
}
