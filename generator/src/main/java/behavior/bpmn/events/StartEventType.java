package behavior.bpmn.events;

public enum StartEventType {
  NONE, // Default
  MESSAGE,
  MESSAGE_NON_INTERRUPTING,
  SIGNAL,
  ERROR,
  SIGNAL_NON_INTERRUPTING
}
