package behavior.bpmn.events.definitions;

import java.util.Objects;

public class EscalationEventDefinition implements EventDefinition {

  private final String escalationCode;

  public EscalationEventDefinition(String escalationCode) {
    this.escalationCode = escalationCode;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof EscalationEventDefinition)) {
      return false;
    }
    EscalationEventDefinition that = (EscalationEventDefinition) o;
    return Objects.equals(escalationCode, that.escalationCode);
  }

  @Override
  public int hashCode() {
    return Objects.hash(escalationCode);
  }
}
