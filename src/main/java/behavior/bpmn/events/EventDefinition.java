package behavior.bpmn.events;

/**
 * Normally has subtypes for each kind of event (see Figure 10.73 in the BPMN spec)
 */
public class EventDefinition {
    // For signal events
    private String globalSignalName;

    public static EventDefinition empty() {
        return new EventDefinition("");
    }

    public EventDefinition(String globalSignalName) {
        this.globalSignalName = globalSignalName;
    }

    public String getGlobalSignalName() {
        return globalSignalName;
    }
}
