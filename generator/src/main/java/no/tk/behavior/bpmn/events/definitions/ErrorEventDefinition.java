package no.tk.behavior.bpmn.events.definitions;

import java.util.Objects;

public class ErrorEventDefinition implements EventDefinition {

  private final String errorCode;

  public ErrorEventDefinition(String errorCode) {
    this.errorCode = errorCode;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof ErrorEventDefinition that)) {
      return false;
    }
    return Objects.equals(errorCode, that.errorCode);
  }

  @Override
  public int hashCode() {
    return Objects.hash(errorCode);
  }
}
