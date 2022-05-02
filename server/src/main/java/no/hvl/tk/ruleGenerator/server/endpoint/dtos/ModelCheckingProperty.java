package no.hvl.tk.ruleGenerator.server.endpoint.dtos;

public enum ModelCheckingProperty {

    SAFENESS("SAFENESS"),
    OPTION_TO_COMPLETE("OPTION_TO_COMPLETE"),
    NO_DEAD_ACTIVITIES("NO_DEAD_ACTIVITIES"),
    ;
    private final String name;
    ModelCheckingProperty(String name) {
        this.name = name;
    }

}
