package behavior.bpmn.events.definitions;

/** See Figure 10.73 in the BPMN spec */
public interface EventDefinition {
  static EventDefinition empty() {
    return NoEventDefinition.create();
  }
}
