package behavior.bpmn.events;

public abstract class CatchEvent extends Event {
    public CatchEvent(String name, EventDefinition eventDefinition) {
        super(name, eventDefinition);
    }
}
