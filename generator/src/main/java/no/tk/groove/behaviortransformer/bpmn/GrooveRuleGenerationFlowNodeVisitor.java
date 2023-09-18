package no.tk.groove.behaviortransformer.bpmn;

import no.tk.behavior.bpmn.AbstractBPMNProcess;
import no.tk.behavior.bpmn.activities.CallActivity;
import no.tk.behavior.bpmn.activities.tasks.ReceiveTask;
import no.tk.behavior.bpmn.activities.tasks.SendTask;
import no.tk.behavior.bpmn.activities.tasks.Task;
import no.tk.behavior.bpmn.auxiliary.visitors.FlowNodeVisitor;
import no.tk.behavior.bpmn.events.EndEvent;
import no.tk.behavior.bpmn.events.IntermediateCatchEvent;
import no.tk.behavior.bpmn.events.IntermediateThrowEvent;
import no.tk.behavior.bpmn.events.StartEvent;
import no.tk.behavior.bpmn.gateways.EventBasedGateway;
import no.tk.behavior.bpmn.gateways.ExclusiveGateway;
import no.tk.behavior.bpmn.gateways.InclusiveGateway;
import no.tk.behavior.bpmn.gateways.ParallelGateway;

public class GrooveRuleGenerationFlowNodeVisitor implements FlowNodeVisitor {
  private final BPMNRuleGenerator generator;
  private final AbstractBPMNProcess process;

  public GrooveRuleGenerationFlowNodeVisitor(
      BPMNRuleGenerator generator, AbstractBPMNProcess process) {
    this.generator = generator;
    this.process = process;
  }

  @Override
  public void handle(StartEvent startEvent) {
    generator.getEventRuleGenerator().createStartEventRulesForProcess(process, startEvent);
  }

  @Override
  public void handle(Task task) {
    generator.getTaskRuleGenerator().createTaskRulesForProcess(process, task);
  }

  @Override
  public void handle(SendTask sendTask) {
    generator.getTaskRuleGenerator().createSendTaskRulesForProcess(process, sendTask);
  }

  @Override
  public void handle(ReceiveTask receiveTask) {
    generator.getTaskRuleGenerator().createReceiveTaskRulesForProcess(process, receiveTask);
  }

  @Override
  public void handle(CallActivity callActivity) {
    generator.getSubprocessRuleGenerator().createCallActivityRulesForProcess(process, callActivity);
  }

  @Override
  public void handle(ExclusiveGateway exclusiveGateway) {
    generator.getGatewayRuleGenerator().createExclusiveGatewayRules(process, exclusiveGateway);
  }

  @Override
  public void handle(ParallelGateway parallelGateway) {
    generator.getGatewayRuleGenerator().createParallelGatewayRule(process, parallelGateway);
  }

  @Override
  public void handle(InclusiveGateway inclusiveGateway) {
    generator.getGatewayRuleGenerator().createInclusiveGatewayRules(process, inclusiveGateway);
  }

  @Override
  public void handle(EndEvent endEvent) {
    generator.getEventRuleGenerator().createEndEventRule(process, endEvent);
  }

  @Override
  public void handle(EventBasedGateway eventBasedGateway) {
    generator.getGatewayRuleGenerator().createEventBasedGatewayRule(eventBasedGateway, process);
  }

  @Override
  public void handle(IntermediateThrowEvent intermediateThrowEvent) {
    generator
        .getEventRuleGenerator()
        .createIntermediateThrowEventRule(process, intermediateThrowEvent);
  }

  @Override
  public void handle(IntermediateCatchEvent intermediateCatchEvent) {
    generator
        .getEventRuleGenerator()
        .createIntermediateCatchEventRule(process, intermediateCatchEvent);
  }
}
