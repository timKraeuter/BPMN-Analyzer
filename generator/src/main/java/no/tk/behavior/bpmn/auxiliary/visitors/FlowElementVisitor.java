package no.tk.behavior.bpmn.auxiliary.visitors;

import no.tk.behavior.bpmn.SequenceFlow;

public interface FlowElementVisitor extends FlowNodeVisitor {

  void handle(SequenceFlow sequenceFlow);
}
