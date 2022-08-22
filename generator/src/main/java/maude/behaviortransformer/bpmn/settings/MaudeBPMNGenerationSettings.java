package maude.behaviortransformer.bpmn.settings;

public class MaudeBPMNGenerationSettings {

    // Could add more settings such as synch/asynch signal and message semantics.
    private final MessagePersistence messagePersistence;

    public MaudeBPMNGenerationSettings(MessagePersistence messagePersistence) {
        this.messagePersistence = messagePersistence;
    }
    public boolean isPersistentMessages() {
        return this.messagePersistence == MessagePersistence.PERSISTENT;
    }
}
