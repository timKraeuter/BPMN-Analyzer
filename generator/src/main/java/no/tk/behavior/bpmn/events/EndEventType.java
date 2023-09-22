package no.tk.behavior.bpmn.events;

public enum EndEventType implements EventType {
  NONE, // Default
  TERMINATION,
  MESSAGE,
  ERROR,
  ESCALATION,
  SIGNAL;
}
