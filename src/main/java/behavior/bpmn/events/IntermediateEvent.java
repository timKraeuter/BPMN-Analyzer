package behavior.bpmn.events;

public abstract class IntermediateEvent extends Event {
    private final IntermediateEventType type;

    public IntermediateEvent(String name, IntermediateEventType type) {
        super(name);
        this.type = type;
    }

    public IntermediateEventType getType() {
        return this.type;
    }
}
