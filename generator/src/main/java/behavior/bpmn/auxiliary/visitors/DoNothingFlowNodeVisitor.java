package behavior.bpmn.auxiliary.visitors;

import behavior.bpmn.activities.CallActivity;
import behavior.bpmn.activities.tasks.ReceiveTask;
import behavior.bpmn.activities.tasks.SendTask;
import behavior.bpmn.activities.tasks.Task;
import behavior.bpmn.events.EndEvent;
import behavior.bpmn.events.IntermediateCatchEvent;
import behavior.bpmn.events.IntermediateThrowEvent;
import behavior.bpmn.events.StartEvent;
import behavior.bpmn.gateways.EventBasedGateway;
import behavior.bpmn.gateways.ExclusiveGateway;
import behavior.bpmn.gateways.InclusiveGateway;
import behavior.bpmn.gateways.ParallelGateway;

public class DoNothingFlowNodeVisitor implements FlowNodeVisitor {
  @Override
  public void handle(Task task) {
    // Do nothing. Override if you want to do anything.
  }

  @Override
  public void handle(SendTask task) {
    // Do nothing. Override if you want to do anything.
  }

  @Override
  public void handle(ReceiveTask task) {
    // Do nothing. Override if you want to do anything.
  }

  @Override
  public void handle(CallActivity callActivity) {
    // Do nothing. Override if you want to do anything.
  }

  @Override
  public void handle(StartEvent startEvent) {
    // Do nothing. Override if you want to do anything.
  }

  @Override
  public void handle(IntermediateThrowEvent intermediateThrowEvent) {
    // Do nothing. Override if you want to do anything.
  }

  @Override
  public void handle(IntermediateCatchEvent intermediateCatchEvent) {
    // Do nothing. Override if you want to do anything.
  }

  @Override
  public void handle(EndEvent endEvent) {
    // Do nothing. Override if you want to do anything.
  }

  @Override
  public void handle(ExclusiveGateway exclusiveGateway) {
    // Do nothing. Override if you want to do anything.
  }

  @Override
  public void handle(ParallelGateway parallelGateway) {
    // Do nothing. Override if you want to do anything.
  }

  @Override
  public void handle(InclusiveGateway inclusiveGateway) {
    // Do nothing. Override if you want to do anything.
  }

  @Override
  public void handle(EventBasedGateway eventBasedGateway) {
    // Do nothing. Override if you want to do anything.
  }
}
