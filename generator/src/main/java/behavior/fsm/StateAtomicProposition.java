package behavior.fsm;

public class StateAtomicProposition {
    private final String name;

    private final State state;

    public StateAtomicProposition(String name, State state) {
        this.name = name;
        this.state = state;
    }

    public StateAtomicProposition(State state) {
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
