package behavior.bpmn.events;

public abstract class CatchEvent extends Event {
    protected CatchEvent(String name, EventDefinition eventDefinition) {
        super(name, eventDefinition);
    }
}
