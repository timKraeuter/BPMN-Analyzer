package no.tk.behavior.bpmn.events.definitions;

/** See Figure 10.73 in the BPMN spec */
public sealed interface EventDefinition
    permits ErrorEventDefinition,
        EscalationEventDefinition,
        LinkEventDefinition,
        NoEventDefinition,
        SignalEventDefinition {
  static EventDefinition empty() {
    return NoEventDefinition.create();
  }
}
