package behavior.bpmn.events;

public enum EndEventType {
  NONE, // Default
  TERMINATION,
  MESSAGE,
  ERROR,
  ESCALATION,
  SIGNAL
}
