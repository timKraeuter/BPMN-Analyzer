package no.tk.behavior.fsm;

public class Transition {
  private final String name;
  private final State source;
  private final State target;

  public Transition(String name, State source, State target) {
    this.name = name;
    this.source = source;
    this.target = target;
  }

  public String getName() {
    return this.name;
  }

  public State getSource() {
    return this.source;
  }

  public State getTarget() {
    return this.target;
  }
}
