package no.tk.maude.behaviortransformer.bpmn.generators;

import static no.tk.maude.behaviortransformer.bpmn.BPMNToMaudeTransformerConstants.ANY_OTHER_TOKENS;
import static no.tk.maude.behaviortransformer.bpmn.BPMNToMaudeTransformerConstants.WHITE_SPACE;

import java.util.function.Consumer;
import no.tk.behavior.bpmn.AbstractBPMNProcess;
import no.tk.behavior.bpmn.BPMNCollaboration;
import no.tk.behavior.bpmn.SequenceFlow;
import no.tk.behavior.bpmn.activities.tasks.AbstractTask;
import no.tk.behavior.bpmn.activities.tasks.ReceiveTask;
import no.tk.behavior.bpmn.activities.tasks.SendTask;
import no.tk.behavior.bpmn.auxiliary.exceptions.BPMNRuntimeException;
import no.tk.behavior.bpmn.events.BoundaryEvent;
import no.tk.groove.behaviortransformer.bpmn.BPMNToGrooveTransformerConstants;
import no.tk.maude.behaviortransformer.bpmn.BPMNMaudeRuleGenerator;
import no.tk.maude.behaviortransformer.bpmn.BPMNToMaudeTransformerHelper;
import no.tk.maude.behaviortransformer.bpmn.settings.MaudeBPMNGenerationSettings;
import no.tk.maude.generation.BPMNMaudeRuleBuilder;
import no.tk.maude.generation.MaudeObjectBuilder;

public class BPMNMaudeTaskRuleGenerator implements BPMNToMaudeTransformerHelper {
  private final BPMNMaudeRuleGenerator ruleGenerator;
  private final BPMNMaudeRuleBuilder ruleBuilder;
  private final MaudeObjectBuilder objectBuilder;

  public BPMNMaudeTaskRuleGenerator(
      BPMNMaudeRuleGenerator ruleGenerator, BPMNMaudeRuleBuilder ruleBuilder) {
    this.ruleGenerator = ruleGenerator;
    this.ruleBuilder = ruleBuilder;
    this.objectBuilder = new MaudeObjectBuilder();
  }

  public void createTaskRulesForProcess(
      AbstractBPMNProcess process,
      AbstractTask task,
      Consumer<BPMNMaudeRuleBuilder> endTaskRuleAdditions) {
    // Rules for starting the task
    task.getIncomingFlows()
        .forEach(incomingFlow -> createStartTaskRule(process, task, incomingFlow));
    // Rule for ending the task
    createEndTaskRule(process, task, endTaskRuleAdditions);

    // Generate rules for boundary events
    this.createBoundaryEventRules(process, task);
  }

  private void createBoundaryEventRules(AbstractBPMNProcess process, AbstractTask task) {
    task.getBoundaryEvents()
        .forEach(
            boundaryEvent -> {
              switch (boundaryEvent.getType()) {
                case NONE, TIMER ->
                    createTaskBoundaryEventRule(process, task, boundaryEvent, _ -> {}); // NOOP
                case MESSAGE -> createTaskMessageBoundaryEventRule(process, task, boundaryEvent);
                case SIGNAL -> {
                  // Handled in the throw rule part.
                }
                default ->
                    throw new IllegalStateException("Unexpected value: " + boundaryEvent.getType());
              }
            });
  }

  private void createTaskMessageBoundaryEventRule(
      AbstractBPMNProcess process, AbstractTask task, BoundaryEvent boundaryEvent) {
    getCollaboration()
        .getIncomingMessageFlows(boundaryEvent)
        .forEach(
            messageFlow ->
                createTaskBoundaryEventRule(
                    process, task, boundaryEvent, _ -> addMessageConsumption(messageFlow)));
  }

  private void createTaskBoundaryEventRule(
      AbstractBPMNProcess process,
      AbstractTask task,
      BoundaryEvent boundaryEvent,
      Consumer<BPMNMaudeRuleBuilder> ruleAddditions) {
    ruleBuilder.startRule(getFlowNodeRuleName(boundaryEvent));
    ruleAddditions.accept(getRuleBuilder());

    String taskToken = getTokenForFlowNode(task);
    ruleBuilder.addPreObject(
        createProcessSnapshotObjectAnySubProcessAndSignals(process, taskToken + ANY_OTHER_TOKENS));
    String postTokens;
    if (boundaryEvent.isInterrupt()) {
      // Add outgoing tokens and not the task token.
      postTokens = getOutgoingTokensForFlowNode(boundaryEvent) + ANY_OTHER_TOKENS;
    } else {
      // Add outgoing tokens alongside the task token.
      postTokens =
          getOutgoingTokensForFlowNode(boundaryEvent) + WHITE_SPACE + taskToken + ANY_OTHER_TOKENS;
    }
    ruleBuilder.addPostObject(
        createProcessSnapshotObjectAnySubProcessAndNoSignals(process, postTokens));
    ruleBuilder.buildRule();
  }

  public void createTaskRulesForProcess(AbstractBPMNProcess process, AbstractTask task) {
    createTaskRulesForProcess(
        process,
        task,
        _ -> {
          // NOOP
        });
  }

  private void createStartTaskRule(
      AbstractBPMNProcess process, AbstractTask task, SequenceFlow incomingFlow) {
    ruleBuilder.startRule(
        getFlowNodeRuleNameWithIncFlow(task, incomingFlow.getId())
            + BPMNToGrooveTransformerConstants.START);

    String preTokens = getTokenForSequenceFlow(incomingFlow) + ANY_OTHER_TOKENS;
    ruleBuilder.addPreObject(
        createProcessSnapshotObjectAnySubProcessAndSignals(process, preTokens));

    String postTokens = getTokenForFlowNode(task) + ANY_OTHER_TOKENS;
    ruleBuilder.addPostObject(
        createProcessSnapshotObjectAnySubProcessAndNoSignals(process, postTokens));

    ruleBuilder.buildRule();
  }

  private void createEndTaskRule(
      AbstractBPMNProcess process,
      AbstractTask task,
      Consumer<BPMNMaudeRuleBuilder> ruleAdditions) {
    ruleBuilder.startRule(getFlowNodeRuleName(task) + BPMNToGrooveTransformerConstants.END);

    String preTokens = getTokenForFlowNode(task) + ANY_OTHER_TOKENS;
    ruleBuilder.addPreObject(
        createProcessSnapshotObjectAnySubProcessAndSignals(process, preTokens));

    String postTokens = getOutgoingTokensForFlowNode(task) + ANY_OTHER_TOKENS;
    ruleBuilder.addPostObject(
        createProcessSnapshotObjectAnySubProcessAndNoSignals(process, postTokens));
    ruleAdditions.accept(ruleBuilder);

    ruleBuilder.buildRule();
  }

  public void createSendTaskRulesForProcess(AbstractBPMNProcess process, SendTask sendTask) {
    createTaskRulesForProcess(process, sendTask, _ -> addSendMessageBehaviorForFlowNode(sendTask));
  }

  public void createReceiveTaskRulesForProcess(
      AbstractBPMNProcess process, ReceiveTask receiveTask) {
    this.createBoundaryEventRules(process, receiveTask);

    if (receiveTask.isInstantiate() && receiveTask.getIncomingFlows().findAny().isPresent()) {
      throw new BPMNRuntimeException(
          "Instantiate receive tasks should not have incoming sequence " + "flows!");
    } else {
      // Rules for starting the task
      createStartInteractionNodeRule(receiveTask, process);
    }
    // Rule for ending the task (now consumes messages)
    createEndInteractionNodeRule(receiveTask, process);
  }

  @Override
  public BPMNMaudeRuleBuilder getRuleBuilder() {
    return ruleBuilder;
  }

  @Override
  public MaudeObjectBuilder getObjectBuilder() {
    return objectBuilder;
  }

  @Override
  public BPMNCollaboration getCollaboration() {
    return ruleGenerator.getCollaboration();
  }

  @Override
  public MaudeBPMNGenerationSettings getSettings() {
    return ruleGenerator.getSettings();
  }
}
