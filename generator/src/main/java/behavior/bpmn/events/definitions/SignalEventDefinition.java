package behavior.bpmn.events.definitions;

import com.google.common.base.Objects;

public class SignalEventDefinition implements EventDefinition {
  // For signal events
  private final String globalSignalName;

  public SignalEventDefinition(String globalSignalName) {
    this.globalSignalName = globalSignalName;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof SignalEventDefinition)) return false;
    SignalEventDefinition that = (SignalEventDefinition) o;
    return Objects.equal(globalSignalName, that.globalSignalName);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(globalSignalName);
  }
}
