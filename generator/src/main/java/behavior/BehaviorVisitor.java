package behavior;

import behavior.activity.ActivityDiagram;
import behavior.bpmn.BPMNCollaboration;
import behavior.fsm.FiniteStateMachine;
import behavior.petrinet.PetriNet;
import behavior.picalculus.NamedPiProcess;

public interface BehaviorVisitor {
    void handle(FiniteStateMachine finiteStateMachine);

    void handle(PetriNet petriNet);

    void handle(BPMNCollaboration bpmnProcess);

    void handle(NamedPiProcess piProcess);

    void handle(ActivityDiagram activityDiagram);
}
