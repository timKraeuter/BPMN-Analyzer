package groove.behaviorTransformer.bpmn;

import behavior.bpmn.AbstractProcess;
import behavior.bpmn.EventSubprocess;
import behavior.bpmn.Process;
import behavior.bpmn.activities.CallActivity;
import behavior.bpmn.activities.tasks.ReceiveTask;
import behavior.bpmn.activities.tasks.SendTask;
import behavior.bpmn.activities.tasks.Task;
import behavior.bpmn.auxiliary.AbstractProcessVisitor;
import behavior.bpmn.auxiliary.FlowNodeVisitor;
import behavior.bpmn.events.EndEvent;
import behavior.bpmn.events.IntermediateCatchEvent;
import behavior.bpmn.events.IntermediateThrowEvent;
import behavior.bpmn.events.StartEvent;
import behavior.bpmn.gateways.EventBasedGateway;
import behavior.bpmn.gateways.ExclusiveGateway;
import behavior.bpmn.gateways.InclusiveGateway;
import behavior.bpmn.gateways.ParallelGateway;

public class RuleGenerationFlowNodeVisitor implements FlowNodeVisitor {
    private final BPMNRuleGenerator generator;
    private final AbstractProcess process;

    public RuleGenerationFlowNodeVisitor(BPMNRuleGenerator generator, AbstractProcess process) {
        this.generator = generator;
        this.process = process;
    }

    @Override
    public void handle(StartEvent startEvent) {
        process.accept(new AbstractProcessVisitor() {
            @Override
            public void handle(EventSubprocess eventSubprocess) {
                // Handled elsewhere for event subprocesses.
            }

            @Override
            public void handle(Process process) {
                generator.createStartEventRule(startEvent, process);
            }
        });
    }

    @Override
    public void handle(Task task) {
        generator.createTaskRules(process, task);
    }

    @Override
    public void handle(SendTask sendTask) {
        generator.createTaskRules(process,
                                  sendTask,
                                  (ruleBuilder) -> generator.addOutgoingMessagesForFlowNode(sendTask));
    }

    @Override
    public void handle(ReceiveTask receiveTask) {
        if (receiveTask.isInstantiate()) {
            if (receiveTask.getIncomingFlows().findAny().isPresent()) {
                throw new RuntimeException("Instantiate receive tasks should not have incoming sequence " +
                                                   "flows!");
            }
            generator.createInstantiateReceiveTaskRule(process, receiveTask);
            return;
        }
        // Create start task rules.
        receiveTask.getIncomingFlows().forEach(incomingFlow -> generator.createReceiveTaskStartRule(process,
                                                                                                    receiveTask,
                                                                                                    incomingFlow));
        // End task rule is standard.
        generator.createEndTaskRule(process, receiveTask, (noop) -> {
        });
    }

    @Override
    public void handle(CallActivity callActivity) {
        // Rules for instantiating a subprocess
        callActivity.getIncomingFlows().forEach(incomingFlow -> generator.createSubProcessInstantiationRule(process,
                                                                                                            callActivity,
                                                                                                            incomingFlow));

        // Rule for terminating a subprocess
        generator.createTerminateSubProcessRule(process, callActivity);

        // Generate rules for the sub process
        generator.createRulesForExecutingTheSubProcess(callActivity);

        generator.createBoundaryEventRules(process, callActivity);
    }

    @Override
    public void handle(ExclusiveGateway exclusiveGateway) {
        generator.createExclusiveGatewayRules(process, exclusiveGateway);
    }

    @Override
    public void handle(ParallelGateway parallelGateway) {
        generator.createParallelGatewayRule(process, parallelGateway);
    }

    @Override
    public void handle(InclusiveGateway inclusiveGateway) {
        generator.createInclusiveGatewayRules(process, inclusiveGateway);
    }

    @Override
    public void handle(EndEvent endEvent) {
        generator.createEndEventRule(process, endEvent);
    }

    @Override
    public void handle(EventBasedGateway eventBasedGateway) {
        generator.createEventBasedGatewayRule(eventBasedGateway, process);
    }

    @Override
    public void handle(IntermediateThrowEvent intermediateThrowEvent) {
        generator.createIntermediateThrowEventRule(intermediateThrowEvent, process);
    }

    @Override
    public void handle(IntermediateCatchEvent intermediateCatchEvent) {
        generator.createIntermediateCatchEventRule(intermediateCatchEvent, process);
    }
}
