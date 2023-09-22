package no.tk.behavior.bpmn.events;

public interface EventType {
  default boolean isAny(EventType... types) {
    for (EventType type : types) {
      if (type == this) {
        return true;
      }
    }
    return false;
  }
}
