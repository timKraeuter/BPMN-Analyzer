package no.tk.behavior.fsm;

public class FSMStateAtomicProposition {
  private final String name;

  private final State state;

  public FSMStateAtomicProposition(String name, State state) {
    this.name = name;
    this.state = state;
  }

  public FSMStateAtomicProposition(State state) {
    this.name = state.getName();
    this.state = state;
  }

  public String getName() {
    return name;
  }

  public State getState() {
    return state;
  }
}
