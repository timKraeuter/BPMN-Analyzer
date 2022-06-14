package behavior.bpmn.events;

public abstract class ThrowEvent extends Event {
    protected ThrowEvent(String name, EventDefinition eventDefinition) {
        super(name, eventDefinition);
    }
}
