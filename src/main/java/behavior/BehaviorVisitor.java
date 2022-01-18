package behavior;

import behavior.activity.ActivityDiagram;
import behavior.bpmn.BPMNProcess;
import behavior.fsm.FiniteStateMachine;
import behavior.petriNet.PetriNet;
import behavior.piCalculus.NamedPiProcess;

public interface BehaviorVisitor {
    void handle(FiniteStateMachine finiteStateMachine);

    void handle(PetriNet petriNet);

    void handle(BPMNProcess bpmnProcess);

    void handle(NamedPiProcess piProcess);

    void handle(ActivityDiagram activityDiagram);
}
