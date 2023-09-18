package no.tk.behavior;

import no.tk.behavior.activity.ActivityDiagram;
import no.tk.behavior.bpmn.BPMNCollaboration;
import no.tk.behavior.fsm.FiniteStateMachine;
import no.tk.behavior.petrinet.PetriNet;
import no.tk.behavior.picalculus.NamedPiProcess;

public interface BehaviorVisitor {
  void handle(FiniteStateMachine finiteStateMachine);

  void handle(PetriNet petriNet);

  void handle(BPMNCollaboration bpmnProcess);

  void handle(NamedPiProcess piProcess);

  void handle(ActivityDiagram activityDiagram);
}
