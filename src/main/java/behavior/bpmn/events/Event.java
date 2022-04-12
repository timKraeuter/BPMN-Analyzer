package behavior.bpmn.events;

import behavior.bpmn.FlowNode;
import behavior.bpmn.auxiliary.EventVisitor;

public abstract class Event extends FlowNode {

    private final EventDefinition eventDefinition;

    public Event(String name, EventDefinition eventDefinition) {
        super(name);
        this.eventDefinition = eventDefinition;
    }

    public abstract void accept(EventVisitor eventVisitor);

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
    public boolean isExclusiveEventBasedGateway() {
        return false;
    }

    public EventDefinition getEventDefinition() {
        return eventDefinition;
    }
}
