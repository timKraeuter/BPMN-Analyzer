package no.tk.behavior.bpmn.auxiliary.visitors;

import no.tk.behavior.bpmn.events.EndEvent;
import no.tk.behavior.bpmn.events.IntermediateCatchEvent;
import no.tk.behavior.bpmn.events.IntermediateThrowEvent;
import no.tk.behavior.bpmn.events.StartEvent;

public interface EventVisitor {

  void handle(StartEvent startEvent);

  void handle(IntermediateThrowEvent intermediateThrowEvent);

  void handle(IntermediateCatchEvent intermediateCatchEvent);

  void handle(EndEvent endEvent);
}
