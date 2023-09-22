package no.tk.behavior.bpmn.events;

public enum StartEventType implements EventType {
  NONE, // Default
  MESSAGE,
  SIGNAL,
  ERROR,
  ESCALATION,
}
