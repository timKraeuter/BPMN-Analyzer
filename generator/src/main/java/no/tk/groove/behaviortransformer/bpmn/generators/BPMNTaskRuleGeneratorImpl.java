package no.tk.groove.behaviortransformer.bpmn.generators;

import static no.tk.groove.behaviortransformer.bpmn.BPMNToGrooveTransformerConstants.END;
import static no.tk.groove.behaviortransformer.bpmn.BPMNToGrooveTransformerConstants.START;
import static no.tk.groove.behaviortransformer.bpmn.BPMNToGrooveTransformerHelper.*;
import static no.tk.groove.behaviortransformer.bpmn.BPMNToGrooveTransformerHelper.getFlowNodeRuleName;

import io.github.timkraeuter.groove.graph.GrooveNode;
import io.github.timkraeuter.groove.rule.GrooveRuleBuilder;
import java.util.function.Consumer;
import no.tk.behavior.bpmn.AbstractBPMNProcess;
import no.tk.behavior.bpmn.BPMNCollaboration;
import no.tk.behavior.bpmn.SequenceFlow;
import no.tk.behavior.bpmn.activities.tasks.AbstractTask;
import no.tk.behavior.bpmn.activities.tasks.ReceiveTask;
import no.tk.behavior.bpmn.activities.tasks.SendTask;
import no.tk.behavior.bpmn.activities.tasks.Task;
import no.tk.behavior.bpmn.events.BoundaryEvent;
import no.tk.groove.behaviortransformer.bpmn.BPMNToGrooveTransformerHelper;

public class BPMNTaskRuleGeneratorImpl implements BPMNTaskRuleGenerator {

  private final BPMNCollaboration collaboration;
  private final GrooveRuleBuilder ruleBuilder;

  public BPMNTaskRuleGeneratorImpl(BPMNCollaboration collaboration, GrooveRuleBuilder ruleBuilder) {
    this.collaboration = collaboration;
    this.ruleBuilder = ruleBuilder;
  }

  @Override
  public void createTaskRulesForProcess(AbstractBPMNProcess process, Task task) {
    createTaskRulesForProcess(process, task, _ -> {});
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
    this.createEndTaskRule(process, receiveTask, _ -> {});
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
              ruleBuilder.startRule(
                  getFlowNodeRuleName(receiveTask, incomingFlow.getNameOrIDIfEmpty()) + START);
              GrooveNode processInstance = contextProcessInstance(process, ruleBuilder);
              deleteMessageToProcessInstanceWithPosition(
                  ruleBuilder, processInstance, messageFlow.getNameOrDescriptiveName());
              addFlowNodeToken(ruleBuilder, processInstance, receiveTask);
              // Consume the token at the event-based gateway.
              deleteFlowNodeToken(ruleBuilder, processInstance, incomingFlow.getSource());
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
            incomingFlow -> createStartTaskRule(process, task, incomingFlow, _ -> {}));
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
                case NONE, TIMER -> createBoundaryEventRule(process, boundaryEvent, task, _ -> {
                });
                case MESSAGE -> collaboration
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
                case SIGNAL -> {
                  // Handled in the throw rule part.
                }
                default ->
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
      deleteFlowNodeToken(ruleBuilder, processInstance, task);
    } else {
      contextFlowNodeToken(ruleBuilder, processInstance, task);
    }
    // WARNING: If not interrupt the state space grows indefinitely!
    ruleBuilder.buildRule();
  }

  void createStartTaskRule(
      AbstractBPMNProcess process,
      AbstractTask task,
      SequenceFlow incomingFlow,
      Consumer<GrooveNode> startTaskRuleAdditions) {
    ruleBuilder.startRule(getFlowNodeRuleName(task, incomingFlow.getNameOrIDIfEmpty()) + START);
    GrooveNode processInstance = contextProcessInstance(process, ruleBuilder);
    deleteSequenceFlowToken(ruleBuilder, processInstance, incomingFlow);
    BPMNToGrooveTransformerHelper.addFlowNodeToken(ruleBuilder, processInstance, task);
    startTaskRuleAdditions.accept(processInstance);
    ruleBuilder.buildRule();
  }

  void createEndTaskRule(
      AbstractBPMNProcess process,
      AbstractTask task,
      Consumer<GrooveRuleBuilder> endTaskRuleAdditions) {
    ruleBuilder.startRule(task.getName() + END);
    GrooveNode processInstance = contextProcessInstance(process, ruleBuilder);
    BPMNToGrooveTransformerHelper.deleteFlowNodeToken(ruleBuilder, processInstance, task);

    task.getOutgoingFlows()
        .forEach(outFlow -> addSequenceFlowToken(ruleBuilder, processInstance, outFlow));
    endTaskRuleAdditions.accept(ruleBuilder);
    ruleBuilder.buildRule();
  }
}
