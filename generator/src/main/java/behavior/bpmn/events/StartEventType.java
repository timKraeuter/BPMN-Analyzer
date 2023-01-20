package behavior.bpmn.events;

public enum StartEventType {
  NONE, // Default
  MESSAGE,
  MESSAGE_NON_INTERRUPTING,
  SIGNAL,
  ERROR,
  ESCALATION,
  SIGNAL_NON_INTERRUPTING
}
