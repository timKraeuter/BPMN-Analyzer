package no.hvl.tk.rulegenerator.server.endpoint.dtos;

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
}
