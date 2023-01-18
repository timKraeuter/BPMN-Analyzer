package behavior.bpmn.events.definitions;

public class NoEventDefinition implements EventDefinition {
  private static NoEventDefinition instance;

  public static NoEventDefinition create() {
    if (instance == null) {
      instance = new NoEventDefinition();
    }
    return instance;
  }

  private NoEventDefinition() {
  }
}
