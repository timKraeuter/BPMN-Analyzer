package behavior;

import behavior.fsm.FiniteStateMachine;

public interface BehaviorVisitor {
    void accept(FiniteStateMachine finiteStateMachine);
}
