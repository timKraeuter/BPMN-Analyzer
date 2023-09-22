package no.tk.behavior.bpmn.events;

public enum BoundaryEventType implements EventType {
  TIMER,
  MESSAGE,
  SIGNAL,
  NONE,
  ERROR,
  ESCALATION,
  // Compensation,
  // Conditional
}
