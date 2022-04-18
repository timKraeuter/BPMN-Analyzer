package behavior.bpmn.events;

public abstract class ThrowEvent extends Event {
    public ThrowEvent(String name, EventDefinition eventDefinition) {
        super(name, eventDefinition);
    }
}
