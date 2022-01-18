package behavior;

import behavior.activity.ActivityDiagram;
import behavior.bpmn.BPMNCollaboration;
import behavior.fsm.FiniteStateMachine;
import behavior.petriNet.PetriNet;
import behavior.piCalculus.NamedPiProcess;

public interface BehaviorVisitor {
    void handle(FiniteStateMachine finiteStateMachine);

    void handle(PetriNet petriNet);

    void handle(BPMNCollaboration bpmnProcess);

    void handle(NamedPiProcess piProcess);

    void handle(ActivityDiagram activityDiagram);
}
