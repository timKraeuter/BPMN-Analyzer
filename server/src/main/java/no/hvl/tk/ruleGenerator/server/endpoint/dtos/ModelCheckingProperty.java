package no.hvl.tk.ruleGenerator.server.endpoint.dtos;

import com.fasterxml.jackson.annotation.JsonValue;

public enum ModelCheckingProperty {

    SAFENESS("Safeness"),
    OPTION_TO_COMPLETE("Option to complete"),
    NO_DEAD_ACTIVITIES("No dead activities"),
    ;

    private final String name;
    ModelCheckingProperty(String name) {
        this.name = name;
    }

    @JsonValue
    public String getName() {
        return name;
    }
}
