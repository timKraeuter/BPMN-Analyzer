package no.tk.behavior.bpmn.events.definitions;

public final class NoEventDefinition implements EventDefinition {
  private static final NoEventDefinition INSTANCE = new NoEventDefinition();

  public static NoEventDefinition create() {
    return INSTANCE;
  }

  private NoEventDefinition() {}
}
