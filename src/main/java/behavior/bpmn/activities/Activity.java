package behavior.bpmn.activities;

import behavior.bpmn.FlowNode;
import behavior.bpmn.events.BoundaryEvent;

import java.util.LinkedHashSet;
import java.util.Set;

public abstract class Activity extends FlowNode {
    private final Set<BoundaryEvent> boundaryEvents;

    public Activity(String name) {
        super(name);
        boundaryEvents = new LinkedHashSet<>();
    }

    @Override
    public boolean isInclusiveGateway() {
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

    public void attachBoundaryEvent(BoundaryEvent boundaryEvent) {
        boundaryEvents.add(boundaryEvent);
    }

    public Set<BoundaryEvent> getBoundaryEvents() {
        return boundaryEvents;
    }
}
