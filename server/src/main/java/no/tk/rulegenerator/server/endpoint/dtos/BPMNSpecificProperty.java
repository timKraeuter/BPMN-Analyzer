package no.tk.rulegenerator.server.endpoint.dtos;

import com.fasterxml.jackson.annotation.JsonValue;

public enum BPMNSpecificProperty {
  SAFENESS("Safeness"),
  OPTION_TO_COMPLETE("Option to complete"),
  PROPER_COMPLETION("Proper completion"),
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
    return switch (this) {
      case SAFENESS -> 0;
      case OPTION_TO_COMPLETE -> 1;
      case PROPER_COMPLETION -> 2;
      case NO_DEAD_ACTIVITIES -> 3;
    };
  }
}
