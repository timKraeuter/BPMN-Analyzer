package no.tk.maude.behaviortransformer.bpmn.generators;

import static no.tk.behavior.bpmn.events.BoundaryEventType.*;
import static no.tk.maude.behaviortransformer.bpmn.BPMNToMaudeTransformerConstants.*;
import static no.tk.maude.behaviortransformer.bpmn.BPMNToMaudeTransformerConstants.NONE;

import no.tk.behavior.bpmn.AbstractBPMNProcess;
import no.tk.behavior.bpmn.BPMNCollaboration;
import no.tk.behavior.bpmn.SequenceFlow;
import no.tk.behavior.bpmn.activities.CallActivity;
import no.tk.behavior.bpmn.events.BoundaryEvent;
import no.tk.behavior.bpmn.events.StartEventType;
import no.tk.groove.behaviortransformer.bpmn.generators.BPMNSubprocessRuleGenerator;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import no.tk.maude.behaviortransformer.bpmn.BPMNMaudeRuleGenerator;
import no.tk.maude.behaviortransformer.bpmn.BPMNToMaudeTransformerHelper;
import no.tk.maude.behaviortransformer.bpmn.settings.MaudeBPMNGenerationSettings;
import no.tk.maude.generation.BPMNMaudeRuleBuilder;
import no.tk.maude.generation.MaudeObject;
import no.tk.maude.generation.MaudeObjectBuilder;
import no.tk.groove.behaviortransformer.bpmn.BPMNToGrooveTransformerConstants;

public class BPMNMaudeSubprocessRuleGenerator
    implements BPMNSubprocessRuleGenerator, BPMNToMaudeTransformerHelper {
  private final BPMNMaudeRuleGenerator ruleGenerator;
  private final BPMNMaudeRuleBuilder ruleBuilder;
  private final MaudeObjectBuilder objectBuilder;

  public BPMNMaudeSubprocessRuleGenerator(
      BPMNMaudeRuleGenerator ruleGenerator, BPMNMaudeRuleBuilder ruleBuilder) {
    this.ruleGenerator = ruleGenerator;
    this.ruleBuilder = ruleBuilder;
    objectBuilder = new MaudeObjectBuilder();
  }

  @Override
  public void createCallActivityRulesForProcess(
          AbstractBPMNProcess process, CallActivity callActivity) {
    // Rules for instantiating a subprocess
    callActivity
        .getIncomingFlows()
        .forEach(
            incomingFlow ->
                this.createSubProcessInstantiationRule(process, callActivity, incomingFlow));

    // Rule for terminating a subprocess
    this.createTerminateSubProcessRule(process, callActivity);

    // Generate rules for the sub process
    this.createRulesForExecutingTheSubProcess(callActivity);

    // Generate rules for boundary events
    this.createBoundaryEventRules(process, callActivity, ruleGenerator.getCollaboration());
  }

  private void createBoundaryEventRules(
      AbstractBPMNProcess process, CallActivity callActivity, BPMNCollaboration collaboration) {
    callActivity
        .getBoundaryEvents()
        .forEach(
            boundaryEvent -> {
              switch (boundaryEvent.getType()) {
                case NONE, TIMER:
                  createSubProcessBoundaryEventRule(
                      process, callActivity, boundaryEvent, rb -> {}); // NOOP
                  break;
                case MESSAGE:
                  createSubProcessMessageBoundaryEventRule(
                      process, callActivity, boundaryEvent, collaboration);
                  break;
                case SIGNAL:
                  // Handled in the throw rule part.
                  break;
                default:
                  throw new IllegalStateException("Unexpected value: " + boundaryEvent.getType());
              }
            });
  }

  private void createSubProcessMessageBoundaryEventRule(
      AbstractBPMNProcess process,
      CallActivity callActivity,
      BoundaryEvent boundaryEvent,
      BPMNCollaboration collaboration) {
    collaboration
        .getIncomingMessageFlows(boundaryEvent)
        .forEach(
            messageFlow ->
                createSubProcessBoundaryEventRule(
                    process,
                    callActivity,
                    boundaryEvent,
                    maudeRuleBuilder -> addMessageConsumption(messageFlow)));
  }

  private void createSubProcessBoundaryEventRule(
      AbstractBPMNProcess process,
      CallActivity callActivity,
      BoundaryEvent boundaryEvent,
      Consumer<BPMNMaudeRuleBuilder> ruleAdditions) {
    ruleBuilder.startRule(getFlowNodeRuleName(boundaryEvent));
    ruleAdditions.accept(getRuleBuilder());

    // Setup vars
    String oid = BPMNToGrooveTransformerConstants.O + 1;
    String anyOtherTokens1 = ANY_TOKENS + "1";
    String anyOtherSignals1 = ANY_SIGNALS + "1";
    String anyOtherSubprocesses1 = ANY_SUBPROCESSES + "1";
    String anyOtherSubprocesses2 = ANY_SUBPROCESSES + "2";
    ruleBuilder.addVar(BPMNToGrooveTransformerConstants.OIDS, BPMNToGrooveTransformerConstants.OID, oid);
    ruleBuilder.addVar(BPMNToGrooveTransformerConstants.TOKENS, BPMNToGrooveTransformerConstants.MSET, anyOtherTokens1);
    ruleBuilder.addVar(BPMNToGrooveTransformerConstants.SIGNALS, BPMNToGrooveTransformerConstants.MSET, anyOtherSignals1);
    ruleBuilder.addVar(BPMNToGrooveTransformerConstants.SUBPROCESSES, BPMNToGrooveTransformerConstants.CONFIGURATION, anyOtherSubprocesses1);
    ruleBuilder.addVar(BPMNToGrooveTransformerConstants.SUBPROCESSES, BPMNToGrooveTransformerConstants.CONFIGURATION, anyOtherSubprocesses2);

    // Setup pre
    // Subprocess must be running
    String subprocesses =
        createProcessSnapshotObjectWithoutParents(
                    callActivity.getSubProcessModel(),
                    oid,
                    anyOtherSubprocesses1,
                    anyOtherTokens1,
                    anyOtherSignals1,
                    RUNNING)
                .generateObjectString()
            + " "
            + anyOtherSubprocesses2;
    ruleBuilder.addPreObject(
        createProcessSnapshotObjectRunning(process, subprocesses, ANY_TOKENS, ANY_SIGNALS));
    String postTokens = getOutgoingTokensForFlowNode(boundaryEvent) + ANY_OTHER_TOKENS;
    if (boundaryEvent.isInterrupt()) {
      // Interrupt removes subprocesses
      // Add outgoing tokens
      ruleBuilder.addPostObject(
          createProcessSnapshotObjectRunning(process, anyOtherSubprocesses2, postTokens, NONE));
    } else {
      // Add outgoing tokens
      ruleBuilder.addPostObject(
          createProcessSnapshotObjectRunning(process, subprocesses, postTokens, NONE));
    }
    ruleBuilder.buildRule();
  }

  private void createSubProcessInstantiationRule(
      AbstractBPMNProcess process, CallActivity callActivity, SequenceFlow incomingFlow) {

    ruleBuilder.startRule(
        getFlowNodeRuleNameWithIncFlow(callActivity, incomingFlow.getId()) + BPMNToGrooveTransformerConstants.START);

    String preTokens = getTokenForSequenceFlow(incomingFlow) + ANY_OTHER_TOKENS;
    ruleBuilder.addPreObject(
        createProcessSnapshotObjectAnySubProcessAndSignals(process, preTokens));

    String subProcessTokens;
    if (subprocessHasStartEvents(callActivity)) {
      // Subprocess has start events which get tokens on outgoing sequence flows!
      subProcessTokens =
          callActivity.getSubProcessModel().getStartEvents().stream()
              .filter(startEvent -> startEvent.getType() == StartEventType.NONE)
              .map(this::getOutgoingTokensForFlowNode)
              .collect(Collectors.joining(" "));
    } else {
      // All activities and gateways without incoming sequence flows get a token.
      subProcessTokens =
          callActivity
              .getSubProcessModel()
              .getFlowNodes()
              .filter(flowNode -> flowNode.isTask() || flowNode.isGateway())
              .filter(flowNode -> flowNode.getIncomingFlows().findAny().isEmpty())
              .map(this::getTokenForFlowNode)
              .collect(Collectors.joining(" "));
    }
    MaudeObject subProcess =
        createProcessSnapshotObjectWithoutParents(
            callActivity.getSubProcessModel(),
            String.format(ENQUOTE_FORMAT, callActivity.getSubProcessModel().getName()),
            NONE,
            subProcessTokens,
            NONE,
            RUNNING);
    ruleBuilder.addPostObject(
        createProcessSnapshotObjectRunning(
            process, subProcess.generateObjectString() + ANY_OTHER_SUBPROCESSES, ANY_TOKENS, NONE));

    ruleBuilder.buildRule();
  }

  private void createTerminateSubProcessRule(
      AbstractBPMNProcess process, CallActivity callActivity) {
    ruleBuilder.startRule(getFlowNodeRuleName(callActivity) + BPMNToGrooveTransformerConstants.END);

    MaudeObject subProcess = createTerminatedProcessSnapshot(callActivity.getSubProcessModel());
    ruleBuilder.addPreObject(
        createProcessSnapshotObjectRunning(
            process,
            subProcess.generateObjectString() + ANY_OTHER_SUBPROCESSES,
            ANY_TOKENS,
            ANY_SIGNALS));

    // Add outgoing tokens
    String postTokens = getOutgoingTokensForFlowNode(callActivity) + ANY_OTHER_TOKENS;

    // Subprocess is deleted (since it is not in the post object).
    ruleBuilder.addPostObject(
        createProcessSnapshotObjectAnySubProcessAndNoSignals(process, postTokens));

    ruleBuilder.buildRule();
  }

  private void createRulesForExecutingTheSubProcess(CallActivity callActivity) {
    if (ruleGenerator.getVisitedProcessModels().contains(callActivity.getSubProcessModel())) {
      return;
    }
    ruleGenerator.getVisitedProcessModels().add(callActivity.getSubProcessModel());
    ruleGenerator.generateRulesForProcess(callActivity.getSubProcessModel());
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
