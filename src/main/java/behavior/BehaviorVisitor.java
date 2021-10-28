package behavior;

import behavior.fsm.FiniteStateMachine;
import behavior.petriNet.PetriNet;

public interface BehaviorVisitor {
    void accept(FiniteStateMachine finiteStateMachine);

    void accept(PetriNet petriNet);
}
