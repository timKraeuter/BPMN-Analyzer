package behavior.bpmn.events;

public abstract class CatchEvent extends Event {
    protected CatchEvent(String id, String name, EventDefinition eventDefinition) {
        super(id, name, eventDefinition);
    }
}
