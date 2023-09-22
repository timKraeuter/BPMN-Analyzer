package no.tk.behavior.bpmn.auxiliary.visitors;

import no.tk.behavior.bpmn.SequenceFlow;

public class DoNothingFlowElementVisitor extends DoNothingFlowNodeVisitor
    implements FlowElementVisitor {
  @Override
  public void handle(SequenceFlow sequenceFlow) {
    // Do nothing. Override if you want to do anything.
  }
}
