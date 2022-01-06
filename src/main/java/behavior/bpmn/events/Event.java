package behavior.bpmn.events;

import behavior.bpmn.ControlFlowNode;

public abstract class Event extends ControlFlowNode {

    public Event(String name) {
        super(name);
    }
}
