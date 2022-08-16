package groove.behaviortransformer.bpmn.generators;

import behavior.bpmn.*;
import behavior.bpmn.activities.tasks.AbstractTask;
import behavior.bpmn.activities.tasks.ReceiveTask;
import behavior.bpmn.activities.tasks.SendTask;
import behavior.bpmn.activities.tasks.Task;
import behavior.bpmn.auxiliary.exceptions.BPMNRuntimeException;
import behavior.bpmn.events.BoundaryEvent;
import groove.behaviortransformer.bpmn.BPMNToGrooveTransformerHelper;
import groove.graph.GrooveNode;
import groove.graph.rule.GrooveRuleBuilder;

import java.util.Set;
import java.util.function.Consumer;

import static groove.behaviortransformer.GrooveTransformerHelper.createStringNodeLabel;
import static groove.behaviortransformer.bpmn.BPMNToGrooveTransformerConstants.*;
import static groove.behaviortransformer.bpmn.BPMNToGrooveTransformerHelper.*;

public class BPMNTaskRuleGeneratorImpl implements BPMNTaskRuleGenerator {

    private final BPMNCollaboration collaboration;
    private final GrooveRuleBuilder ruleBuilder;
    private final boolean useSFId;

    public BPMNTaskRuleGeneratorImpl(BPMNCollaboration collaboration, GrooveRuleBuilder ruleBuilder, boolean useSFIds) {
        this.collaboration = collaboration;
        this.ruleBuilder = ruleBuilder;
        this.useSFId = useSFIds;
    }

    @Override
    public void createTaskRulesForProcess(AbstractProcess process, Task task) {
        createTaskRulesForProcess(process, task, noop -> {
        });
    }

    @Override
    public void createSendTaskRulesForProcess(AbstractProcess process, SendTask sendTask) {
        createTaskRulesForProcess(process,
                                  sendTask,
                                  builder -> addSendMessageBehaviorForFlowNode(collaboration,
                                                                               builder,
                                                                               sendTask,
                                                                               this.useSFId));

    }

    @Override
    public void createReceiveTaskRulesForProcess(AbstractProcess process, ReceiveTask receiveTask) {
        // Create boundary event rules
        createBoundaryEventRules(process, receiveTask);

        if (receiveTask.isInstantiate()) {
            if (receiveTask.getIncomingFlows().findAny().isPresent()) {
                throw new BPMNRuntimeException("Instantiate receive tasks should not have incoming sequence " +
                                               "flows!");
            }
            this.createInstantiateReceiveTaskRule(process, receiveTask);
            return;
        }
        if (isAfterInstantiateEventBasedGateway(receiveTask)) {
            // Process instantiation is handled in the throw rule in this case, analog to catch events.
            // We could make a distinction here if wanted, such that the task end rules are executed.
            return;
        }
        // Create start task rules.
        receiveTask.getIncomingFlows().forEach(incomingFlow -> this.createReceiveTaskStartRule(process,
                                                                                               receiveTask,
                                                                                               incomingFlow));
        // End task rule is standard.
        this.createEndTaskRule(process, receiveTask, noop -> {
        });

    }

    void createReceiveTaskStartRule(AbstractProcess process, ReceiveTask receiveTask, SequenceFlow incomingFlow) {
        if (incomingFlow.getSource().isExclusiveEventBasedGateway()) {
            createEventBasedGatewayStartTaskRule(process, receiveTask, incomingFlow);
        } else {
            // Should only be able to start when one message is present.
            collaboration.getIncomingMessageFlows(receiveTask).forEach(messageFlow -> createStartTaskRule(process,
                                                                                                          receiveTask,
                                                                                                          incomingFlow,
                                                                                                          // TODO: Delete all other possible messages!
                                                                                                          processInstance -> deleteMessageToProcessInstanceWithPosition(
                                                                                                                  ruleBuilder,
                                                                                                                  processInstance,
                                                                                                                  messageFlow.getName())));
        }
    }

    private void createEventBasedGatewayStartTaskRule(AbstractProcess process,
                                                      ReceiveTask receiveTask,
                                                      SequenceFlow incomingFlow) {
        collaboration.getIncomingMessageFlows(receiveTask).forEach(messageFlow -> {
            final String incomingFlowId = getSequenceFlowIdOrDescriptiveName(incomingFlow, this.useSFId);
            // TODO: Delete all other possible messages!
            ruleBuilder.startRule(this.getTaskOrCallActivityRuleName(receiveTask, incomingFlowId) + START);
            GrooveNode processInstance = BPMNToGrooveTransformerHelper.contextProcessInstance(process, ruleBuilder);
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
            ruleBuilder.startRule(incomingMessageFlows.size() >
                                  1 ? incomingMessageFlow.getName() : receiveTask.getName() + START);
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
        createTaskRulesForProcess(process, receiveTask, noop -> {
        });
    }


    void createTaskRulesForProcess(AbstractProcess process,
                                   AbstractTask task,
                                   Consumer<GrooveRuleBuilder> endTaskRuleAdditions) {
        // Rules for starting the task
        task.getIncomingFlows().forEach(incomingFlow -> createStartTaskRule(process, task, incomingFlow, grooveNode -> {
        }));
        // Rule for ending the task
        createEndTaskRule(process, task, endTaskRuleAdditions);

        // Create boundary event rules
        createBoundaryEventRules(process, task);
    }

    private void createBoundaryEventRules(AbstractProcess process, AbstractTask task) {
        task.getBoundaryEvents().forEach(boundaryEvent -> {
            switch (boundaryEvent.getType()) {
                case NONE:
                case TIMER:
                    createBoundaryEventRule(process, boundaryEvent, task, x -> {
                    });
                    break;
                case MESSAGE:
                    collaboration.getIncomingMessageFlows(boundaryEvent).forEach(messageFlow -> createBoundaryEventRule(
                            process,
                            boundaryEvent,
                            task,
                            processInstance -> deleteMessageToProcessInstanceWithPosition(ruleBuilder,
                                                                                          processInstance,
                                                                                          messageFlow.getName())));
                    break;
                case SIGNAL:
                    // Handled in the throw rule part.
                    break;
                default:
                    throw new IllegalStateException("Unexpected value: " + boundaryEvent.getType());
            }
        });
    }

    private void createBoundaryEventRule(AbstractProcess process,
                                         BoundaryEvent boundaryEvent,
                                         AbstractTask task,
                                         Consumer<GrooveNode> additionalActions) {
        ruleBuilder.startRule(boundaryEvent.getName());
        // Add outgoing tokens
        GrooveNode processInstance = addTokensForOutgoingFlowsToRunningInstance(boundaryEvent,
                                                                                process,
                                                                                ruleBuilder,
                                                                                useSFId);
        additionalActions.accept(processInstance);

        // Delete token in task if interrupt.
        if (boundaryEvent.isInterrupt()) {
            BPMNToGrooveTransformerHelper.deleteTokenWithPosition(ruleBuilder, processInstance, task.getName());
        } else {
            BPMNToGrooveTransformerHelper.contextTokenWithPosition(ruleBuilder, processInstance, task.getName());
        }
        // WARNING: If not interrupt the state space grows indefinitely!
        ruleBuilder.buildRule();
    }

    void createStartTaskRule(AbstractProcess process,
                             AbstractTask task,
                             SequenceFlow incomingFlow,
                             Consumer<GrooveNode> startTaskRuleAdditions) {
        final String incomingFlowId = getSequenceFlowIdOrDescriptiveName(incomingFlow, this.useSFId);
        ruleBuilder.startRule(this.getTaskOrCallActivityRuleName(task, incomingFlowId) + START);
        GrooveNode processInstance = BPMNToGrooveTransformerHelper.contextProcessInstance(process, ruleBuilder);
        BPMNToGrooveTransformerHelper.deleteTokenWithPosition(ruleBuilder, processInstance, incomingFlowId);
        BPMNToGrooveTransformerHelper.addTokenWithPosition(ruleBuilder, processInstance, task.getName());
        startTaskRuleAdditions.accept(processInstance);
        ruleBuilder.buildRule();
    }

    void createEndTaskRule(AbstractProcess process,
                           AbstractTask task,
                           Consumer<GrooveRuleBuilder> endTaskRuleAdditions) {
        ruleBuilder.startRule(task.getName() + END);
        GrooveNode processInstance = BPMNToGrooveTransformerHelper.contextProcessInstance(process, ruleBuilder);
        BPMNToGrooveTransformerHelper.deleteTokenWithPosition(ruleBuilder, processInstance, task.getName());

        task.getOutgoingFlows().forEach(outgoingFlow -> {
            final String outgoingFlowID = getSequenceFlowIdOrDescriptiveName(outgoingFlow, this.useSFId);
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
