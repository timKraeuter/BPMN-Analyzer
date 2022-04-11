package groove.behaviorTransformer.bpmn.generators;

import behavior.bpmn.*;
import behavior.bpmn.activities.tasks.AbstractTask;
import behavior.bpmn.activities.tasks.ReceiveTask;
import behavior.bpmn.activities.tasks.SendTask;
import behavior.bpmn.activities.tasks.Task;
import groove.behaviorTransformer.bpmn.BPMNRuleGenerator;
import groove.behaviorTransformer.bpmn.BPMNToGrooveTransformerHelper;
import groove.graph.GrooveNode;
import groove.graph.rule.GrooveRuleBuilder;

import java.util.Set;
import java.util.function.Consumer;

import static groove.behaviorTransformer.GrooveTransformerHelper.createStringNodeLabel;
import static groove.behaviorTransformer.bpmn.BPMNToGrooveTransformerConstants.*;
import static groove.behaviorTransformer.bpmn.BPMNToGrooveTransformerHelper.*;

public class BPMNTaskRuleGeneratorImpl implements BPMNTaskRuleGenerator {

    private final BPMNCollaboration collaboration;
    private final GrooveRuleBuilder ruleBuilder;

    public BPMNTaskRuleGeneratorImpl(BPMNCollaboration collaboration,
                                     GrooveRuleBuilder ruleBuilder) {
        this.collaboration = collaboration;
        this.ruleBuilder = ruleBuilder;
    }

    @Override
    public void createTaskRulesForProcess(AbstractProcess process, Task task) {
        createTaskRulesForProcess(process, task, (noop) -> {});
    }

    @Override
    public void createSendTaskRulesForProcess(AbstractProcess process, SendTask sendTask) {
        createTaskRulesForProcess(process,
                                  sendTask,
                                  (ruleBuilder) -> addOutgoingMessagesForFlowNode(collaboration,
                                                                                  ruleBuilder,
                                                                                  sendTask));

    }

    @Override
    public void createReceiveTaskRulesForProcess(AbstractProcess process, ReceiveTask receiveTask) {
        if (receiveTask.isInstantiate()) {
            if (receiveTask.getIncomingFlows().findAny().isPresent()) {
                throw new RuntimeException("Instantiate receive tasks should not have incoming sequence " + "flows!");
            }
            this.createInstantiateReceiveTaskRule(process, receiveTask);
            return;
        }
        // Create start task rules.
        receiveTask.getIncomingFlows().forEach(incomingFlow -> this.createReceiveTaskStartRule(process,
                                                                                               receiveTask,
                                                                                               incomingFlow));
        // End task rule is standard.
        this.createEndTaskRule(process, receiveTask, (noop) -> {
        });

    }

    void createReceiveTaskStartRule(AbstractProcess process, ReceiveTask receiveTask, SequenceFlow incomingFlow) {
        if (incomingFlow.getSource().isExclusiveEventBasedGateway()) {
            createEventBasedGatewayStartTaskRule(process, receiveTask, incomingFlow);
        } else {
            // Should only be able to start when one message is present.
            collaboration.getIncomingMessageFlows(receiveTask).forEach(messageFlow -> {
                // TODO: We need to make the rule name unique here, if there are multiple incoming messages!
                // TODO: Delete all other possible messages!
                createStartTaskRule(process,
                                    receiveTask,
                                    incomingFlow,
                                    (processInstance) -> deleteMessageToProcessInstanceWithPosition(
                                            ruleBuilder,
                                            processInstance,
                                            messageFlow.getName()));

            });
        }
    }

    private void createEventBasedGatewayStartTaskRule(AbstractProcess process,
                                                      ReceiveTask receiveTask,
                                                      SequenceFlow incomingFlow) {
        collaboration.getIncomingMessageFlows(receiveTask).forEach(messageFlow -> {
            final String incomingFlowId = incomingFlow.getID();
            // TODO: We need to make the rule name unique here, if there are multiple incoming messages!
            // TODO: Delete all other possible messages!
            ruleBuilder.startRule(this.getTaskOrCallActivityRuleName(receiveTask, incomingFlowId) + START);
            GrooveNode processInstance = BPMNToGrooveTransformerHelper.createContextRunningProcessInstance(process,
                                                                                                           ruleBuilder);
            BPMNToGrooveTransformerHelper.deleteMessageToProcessInstanceWithPosition(ruleBuilder,
                                                                                     processInstance,
                                                                                     messageFlow.getName());
            BPMNToGrooveTransformerHelper.addTokenWithPosition(ruleBuilder, processInstance, receiveTask.getName());
            // Consume the token at the event-based gateway.
            BPMNToGrooveTransformerHelper.deleteTokenWithPosition(ruleBuilder,
                                                                  processInstance,
                                                                  incomingFlow.getSource().getName());
            ruleBuilder.buildRule();
        });
    }

    void createInstantiateReceiveTaskRule(AbstractProcess process, ReceiveTask receiveTask) {
        Set<MessageFlow> incomingMessageFlows = collaboration.getIncomingMessageFlows(receiveTask);
        // Each incoming message flow will instantiate the process.
        incomingMessageFlows.forEach(incomingMessageFlow -> {
            ruleBuilder.startRule(incomingMessageFlows.size() > 1 ? incomingMessageFlow.getName() :
                                          receiveTask.getName() + START);
            GrooveNode processInstance = deleteIncomingMessageAndCreateProcessInstance(incomingMessageFlow,
                                                                                       collaboration,
                                                                                       ruleBuilder);

            GrooveNode activityToken = ruleBuilder.addNode(TYPE_TOKEN);
            ruleBuilder.addEdge(POSITION,
                                activityToken,
                                ruleBuilder.contextNode(createStringNodeLabel(receiveTask.getName())));
            ruleBuilder.addEdge(TOKENS, processInstance, activityToken);
            ruleBuilder.buildRule();
        });
        // Create rules for the outgoing sequence flows.
        createTaskRulesForProcess(process, receiveTask, (noop) -> {});
    }


    void createTaskRulesForProcess(AbstractProcess process,
                                   AbstractTask task,
                                   Consumer<GrooveRuleBuilder> endTaskRuleAdditions) {
        // Rules for starting the task
        task.getIncomingFlows().forEach(incomingFlow -> createStartTaskRule(process,
                                                                            task,
                                                                            incomingFlow,
                                                                            (grooveNode) -> {
                                                                            }));
        // Rule for ending the task
        createEndTaskRule(process, task, endTaskRuleAdditions);
    }

    void createStartTaskRule(AbstractProcess process,
                             AbstractTask task,
                             SequenceFlow incomingFlow,
                             Consumer<GrooveNode> startTaskRuleAdditions) {
        final String incomingFlowId = incomingFlow.getID();
        ruleBuilder.startRule(this.getTaskOrCallActivityRuleName(task, incomingFlowId) + START);
        GrooveNode processInstance = BPMNToGrooveTransformerHelper.createContextRunningProcessInstance(process,
                                                                                                       ruleBuilder);
        BPMNToGrooveTransformerHelper.deleteTokenWithPosition(ruleBuilder, processInstance, incomingFlowId);
        BPMNToGrooveTransformerHelper.addTokenWithPosition(ruleBuilder, processInstance, task.getName());
        startTaskRuleAdditions.accept(processInstance);
        ruleBuilder.buildRule();
    }

    void createEndTaskRule(AbstractProcess process,
                           AbstractTask task,
                           Consumer<GrooveRuleBuilder> endTaskRuleAdditions) {
        ruleBuilder.startRule(task.getName() + END);
        GrooveNode processInstance = BPMNToGrooveTransformerHelper.createContextRunningProcessInstance(process,
                                                                                                       ruleBuilder);
        BPMNToGrooveTransformerHelper.deleteTokenWithPosition(ruleBuilder, processInstance, task.getName());

        task.getOutgoingFlows().forEach(outgoingFlow -> {
            final String outgoingFlowID = outgoingFlow.getID();
            BPMNToGrooveTransformerHelper.addTokenWithPosition(ruleBuilder, processInstance, outgoingFlowID);
        });
        endTaskRuleAdditions.accept(ruleBuilder);
        ruleBuilder.buildRule();
    }

    String getTaskOrCallActivityRuleName(FlowNode taskOrCallActivity, String incomingFlowId) {
        if (taskOrCallActivity.getIncomingFlows().count() > 1) {
            return taskOrCallActivity.getName() + "_" + incomingFlowId;
        }
        return taskOrCallActivity.getName();
    }
}
