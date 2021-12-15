package behavior;

import behavior.activity.ActivityDiagram;
import behavior.bpmn.BPMNProcessModel;
import behavior.fsm.FiniteStateMachine;
import behavior.petriNet.PetriNet;
import behavior.piCalculus.NamedPiProcess;

public interface BehaviorVisitor {
    void handle(FiniteStateMachine finiteStateMachine);

    void handle(PetriNet petriNet);

    void handle(BPMNProcessModel bpmnProcessModel);

    void handle(NamedPiProcess piProcess);

    void handle(ActivityDiagram activityDiagram);
}
