package behavior.bpmn.events;

import com.google.common.base.Objects;

/** Normally has subtypes for each kind of event (see Figure 10.73 in the BPMN spec) */
public class EventDefinition {
  // For signal events
  private final String globalSignalName;

  public EventDefinition(String globalSignalName) {
    this.globalSignalName = globalSignalName;
  }

  public static EventDefinition empty() {
    return new EventDefinition("");
  }

  public String getGlobalSignalName() {
    return globalSignalName;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof EventDefinition)) return false;
    EventDefinition that = (EventDefinition) o;
    return Objects.equal(globalSignalName, that.globalSignalName);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(globalSignalName);
  }
}
