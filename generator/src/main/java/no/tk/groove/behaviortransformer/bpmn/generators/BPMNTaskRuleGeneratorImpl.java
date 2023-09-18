package no.tk.groove.behaviortransformer.bpmn.generators;

import static no.tk.behavior.bpmn.events.BoundaryEventType.*;
import static no.tk.groove.behaviortransformer.bpmn.BPMNToGrooveTransformerConstants.END;
import static no.tk.groove.behaviortransformer.bpmn.BPMNToGrooveTransformerConstants.START;
import static no.tk.groove.behaviortransformer.bpmn.BPMNToGrooveTransformerHelper.*;

import no.tk.behavior.bpmn.AbstractBPMNProcess;
import no.tk.behavior.bpmn.BPMNCollaboration;
import no.tk.behavior.bpmn.FlowNode;
import no.tk.behavior.bpmn.SequenceFlow;
import no.tk.behavior.bpmn.activities.tasks.AbstractTask;
import no.tk.behavior.bpmn.activities.tasks.ReceiveTask;
import no.tk.behavior.bpmn.activities.tasks.SendTask;
import no.tk.behavior.bpmn.activities.tasks.Task;
import no.tk.behavior.bpmn.events.BoundaryEvent;
import no.tk.groove.behaviortransformer.bpmn.BPMNToGrooveTransformerHelper;
import no.tk.groove.graph.GrooveNode;
import no.tk.groove.graph.rule.GrooveRuleBuilder;
import no.tk.behavior.bpmn.events.BoundaryEventType;

import java.util.function.Consumer;

public class BPMNTaskRuleGeneratorImpl implements BPMNTaskRuleGenerator {

  private final BPMNCollaboration collaboration;
  private final GrooveRuleBuilder ruleBuilder;

  public BPMNTaskRuleGeneratorImpl(BPMNCollaboration collaboration, GrooveRuleBuilder ruleBuilder) {
    this.collaboration = collaboration;
    this.ruleBuilder = ruleBuilder;
  }

  @Override
  public void createTaskRulesForProcess(AbstractBPMNProcess process, Task task) {
    createTaskRulesForProcess(process, task, noop -> {});
  }

  @Override
  public void createSendTaskRulesForProcess(AbstractBPMNProcess process, SendTask sendTask) {
    createTaskRulesForProcess(
        process,
        sendTask,
        builder -> addSendMessageBehaviorForFlowNode(collaboration, builder, sendTask));
  }

  @Override
  public void createReceiveTaskRulesForProcess(
      AbstractBPMNProcess process, ReceiveTask receiveTask) {
    // Create boundary event rules
    createBoundaryEventRules(process, receiveTask);
    if (!receiveTask.isInstantiate() && !isAfterInstantiateEventBasedGateway(receiveTask)) {
      //  Start tasks rules not needed for instantiate receive tasks/ receive tasks after
      // instantiate gateways.
      receiveTask
          .getIncomingFlows()
          .forEach(
              incomingFlow -> this.createReceiveTaskStartRule(process, receiveTask, incomingFlow));
    }
    // End task rule is standard.
    this.createEndTaskRule(process, receiveTask, noop -> {});
  }

  void createReceiveTaskStartRule(
      AbstractBPMNProcess process, ReceiveTask receiveTask, SequenceFlow incomingFlow) {
    if (incomingFlow.getSource().isExclusiveEventBasedGateway()) {
      createEventBasedGatewayStartTaskRule(process, receiveTask, incomingFlow);
    } else {
      // Should only be able to start when one message is present.
      collaboration
          .getIncomingMessageFlows(receiveTask)
          .forEach(
              messageFlow ->
                  createStartTaskRule(
                      process,
                      receiveTask,
                      incomingFlow,
                      processInstance ->
                          deleteMessageToProcessInstanceWithPosition(
                              ruleBuilder,
                              processInstance,
                              messageFlow.getNameOrDescriptiveName())));
    }
  }

  private void createEventBasedGatewayStartTaskRule(
      AbstractBPMNProcess process, ReceiveTask receiveTask, SequenceFlow incomingFlow) {
    collaboration
        .getIncomingMessageFlows(receiveTask)
        .forEach(
            messageFlow -> {
              final String incomingFlowId = getSequenceFlowIdOrDescriptiveName(incomingFlow);
              ruleBuilder.startRule(
                  this.getTaskOrCallActivityRuleName(receiveTask, incomingFlowId) + START);
              GrooveNode processInstance =
                  BPMNToGrooveTransformerHelper.contextProcessInstance(process, ruleBuilder);
              BPMNToGrooveTransformerHelper.deleteMessageToProcessInstanceWithPosition(
                  ruleBuilder, processInstance, messageFlow.getNameOrDescriptiveName());
              BPMNToGrooveTransformerHelper.addTokenWithPosition(
                  ruleBuilder, processInstance, receiveTask.getName());
              // Consume the token at the event-based gateway.
              BPMNToGrooveTransformerHelper.deleteTokenWithPosition(
                  ruleBuilder, processInstance, incomingFlow.getSource().getName());
              ruleBuilder.buildRule();
            });
  }

  void createTaskRulesForProcess(
      AbstractBPMNProcess process,
      AbstractTask task,
      Consumer<GrooveRuleBuilder> endTaskRuleAdditions) {
    // Rules for starting the task
    task.getIncomingFlows()
        .forEach(
            incomingFlow -> createStartTaskRule(process, task, incomingFlow, grooveNode -> {}));
    // Rule for ending the task
    createEndTaskRule(process, task, endTaskRuleAdditions);

    // Create boundary event rules
    createBoundaryEventRules(process, task);
  }

  private void createBoundaryEventRules(AbstractBPMNProcess process, AbstractTask task) {
    task.getBoundaryEvents()
        .forEach(
            boundaryEvent -> {
              switch (boundaryEvent.getType()) {
                case NONE, TIMER:
                  createBoundaryEventRule(process, boundaryEvent, task, x -> {});
                  break;
                case MESSAGE:
                  collaboration
                      .getIncomingMessageFlows(boundaryEvent)
                      .forEach(
                          messageFlow ->
                              createBoundaryEventRule(
                                  process,
                                  boundaryEvent,
                                  task,
                                  processInstance ->
                                      deleteMessageToProcessInstanceWithPosition(
                                          ruleBuilder,
                                          processInstance,
                                          messageFlow.getNameOrDescriptiveName())));
                  break;
                case SIGNAL:
                  // Handled in the throw rule part.
                  break;
                default:
                  throw new IllegalStateException("Unexpected value: " + boundaryEvent.getType());
              }
            });
  }

  private void createBoundaryEventRule(
      AbstractBPMNProcess process,
      BoundaryEvent boundaryEvent,
      AbstractTask task,
      Consumer<GrooveNode> additionalActions) {
    ruleBuilder.startRule(boundaryEvent.getName());
    // Add outgoing tokens
    GrooveNode processInstance =
        addTokensForOutgoingFlowsToRunningInstance(boundaryEvent, process, ruleBuilder);
    additionalActions.accept(processInstance);

    // Delete token in task if interrupt.
    if (boundaryEvent.isInterrupt()) {
      BPMNToGrooveTransformerHelper.deleteTokenWithPosition(
          ruleBuilder, processInstance, getFlowNodeNameAndID(task));
    } else {
      BPMNToGrooveTransformerHelper.contextTokenWithPosition(
          ruleBuilder, processInstance, getFlowNodeNameAndID(task));
    }
    // WARNING: If not interrupt the state space grows indefinitely!
    ruleBuilder.buildRule();
  }

  void createStartTaskRule(
      AbstractBPMNProcess process,
      AbstractTask task,
      SequenceFlow incomingFlow,
      Consumer<GrooveNode> startTaskRuleAdditions) {
    final String incomingFlowId = getSequenceFlowIdOrDescriptiveName(incomingFlow);
    ruleBuilder.startRule(this.getTaskOrCallActivityRuleName(task, incomingFlowId) + START);
    GrooveNode processInstance =
        BPMNToGrooveTransformerHelper.contextProcessInstance(process, ruleBuilder);
    BPMNToGrooveTransformerHelper.deleteTokenWithPosition(
        ruleBuilder, processInstance, incomingFlowId);
    BPMNToGrooveTransformerHelper.addTokenWithPosition(
        ruleBuilder, processInstance, getFlowNodeNameAndID(task));
    startTaskRuleAdditions.accept(processInstance);
    ruleBuilder.buildRule();
  }

  void createEndTaskRule(
      AbstractBPMNProcess process,
      AbstractTask task,
      Consumer<GrooveRuleBuilder> endTaskRuleAdditions) {
    ruleBuilder.startRule(task.getName() + END);
    GrooveNode processInstance =
        BPMNToGrooveTransformerHelper.contextProcessInstance(process, ruleBuilder);
    BPMNToGrooveTransformerHelper.deleteTokenWithPosition(
        ruleBuilder, processInstance, getFlowNodeNameAndID(task));

    task.getOutgoingFlows()
        .forEach(
            outgoingFlow -> {
              final String outgoingFlowID = getSequenceFlowIdOrDescriptiveName(outgoingFlow);
              BPMNToGrooveTransformerHelper.addTokenWithPosition(
                  ruleBuilder, processInstance, outgoingFlowID);
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
