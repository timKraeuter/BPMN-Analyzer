package maude.behaviortransformer.bpmn;

import behavior.bpmn.AbstractProcess;
import behavior.bpmn.activities.CallActivity;
import behavior.bpmn.activities.tasks.ReceiveTask;
import behavior.bpmn.activities.tasks.SendTask;
import behavior.bpmn.activities.tasks.Task;
import behavior.bpmn.auxiliary.FlowNodeVisitor;
import behavior.bpmn.events.EndEvent;
import behavior.bpmn.events.IntermediateCatchEvent;
import behavior.bpmn.events.IntermediateThrowEvent;
import behavior.bpmn.events.StartEvent;
import behavior.bpmn.gateways.EventBasedGateway;
import behavior.bpmn.gateways.ExclusiveGateway;
import behavior.bpmn.gateways.InclusiveGateway;
import behavior.bpmn.gateways.ParallelGateway;

public class MaudeRuleGenerationFlowNodeVisitor implements FlowNodeVisitor {
    private final BPMNMaudeRuleGenerator generator;
    private final AbstractProcess process;

    public MaudeRuleGenerationFlowNodeVisitor(BPMNMaudeRuleGenerator generator, AbstractProcess process) {
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
        generator.getGatewayRuleGenerator().createExclusiveGatewayRule(process, exclusiveGateway);
    }

    @Override
    public void handle(ParallelGateway parallelGateway) {
        generator.getGatewayRuleGenerator().createParallelGatewayRule(process, parallelGateway);
    }

    @Override
    public void handle(InclusiveGateway inclusiveGateway) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void handle(EndEvent endEvent) {
        generator.getEventRuleGenerator().createEndEventRule(process, endEvent);
    }

    @Override
    public void handle(EventBasedGateway eventBasedGateway) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void handle(IntermediateThrowEvent intermediateThrowEvent) {
        generator.getEventRuleGenerator().createIntermediateThrowEventRule(process, intermediateThrowEvent);
    }

    @Override
    public void handle(IntermediateCatchEvent intermediateCatchEvent) {
        generator.getEventRuleGenerator().createIntermediateCatchEventRule(process, intermediateCatchEvent);
    }
}
