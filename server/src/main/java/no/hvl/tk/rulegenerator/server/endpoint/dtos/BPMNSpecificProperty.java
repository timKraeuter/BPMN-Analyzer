package no.hvl.tk.rulegenerator.server.endpoint.dtos;

import behavior.bpmn.auxiliary.exceptions.ShouldNotHappenRuntimeException;
import com.fasterxml.jackson.annotation.JsonValue;

public enum BPMNSpecificProperty {
  SAFENESS("Safeness"),
  OPTION_TO_COMPLETE("Option to complete"),
  NO_DEAD_ACTIVITIES("No dead activities"),
  ;

  private final String name;

  BPMNSpecificProperty(String name) {
    this.name = name;
  }

  @JsonValue
  public String getName() {
    return name;
  }

  public int getOrdering() {
    switch (this) {
      case SAFENESS:
        return 1;
      case OPTION_TO_COMPLETE:
        return 2;
      case NO_DEAD_ACTIVITIES:
        return 3;
    }
    throw new ShouldNotHappenRuntimeException("Unknown BPMN specific property!");
  }
}
