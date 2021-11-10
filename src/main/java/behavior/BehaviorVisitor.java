package behavior;

import behavior.bpmn.BPMNProcessModel;
import behavior.fsm.FiniteStateMachine;
import behavior.petriNet.PetriNet;

public interface BehaviorVisitor {
    void handle(FiniteStateMachine finiteStateMachine);

    void handle(PetriNet petriNet);

    void handle(BPMNProcessModel bpmnProcessModel);
}
