package no.tk.behavior.bpmn.events;

public enum IntermediateThrowEventType implements EventType {
  NONE,
  LINK,
  MESSAGE,
  ESCALATION,
  SIGNAL
}
