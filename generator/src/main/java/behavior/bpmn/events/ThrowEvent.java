package behavior.bpmn.events;

public abstract class ThrowEvent extends Event {
    protected ThrowEvent(String id, String name, EventDefinition eventDefinition) {
        super(id, name, eventDefinition);
    }
    @Override
    public boolean isInstantiateFlowNode() {
        return false;
    }
}
