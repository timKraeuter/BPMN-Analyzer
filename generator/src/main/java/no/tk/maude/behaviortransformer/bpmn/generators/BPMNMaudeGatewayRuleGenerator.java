package no.tk.maude.behaviortransformer.bpmn.generators;

import static no.tk.maude.behaviortransformer.bpmn.BPMNToMaudeTransformerConstants.ANY_OTHER_TOKENS;

import no.tk.behavior.bpmn.AbstractBPMNProcess;
import no.tk.behavior.bpmn.BPMNCollaboration;
import no.tk.behavior.bpmn.SequenceFlow;
import no.tk.behavior.bpmn.gateways.EventBasedGateway;
import no.tk.behavior.bpmn.gateways.ExclusiveGateway;
import no.tk.behavior.bpmn.gateways.ParallelGateway;
import java.util.stream.Collectors;
import no.tk.maude.behaviortransformer.bpmn.BPMNMaudeRuleGenerator;
import no.tk.maude.behaviortransformer.bpmn.BPMNToMaudeTransformerHelper;
import no.tk.maude.behaviortransformer.bpmn.settings.MaudeBPMNGenerationSettings;
import no.tk.maude.generation.BPMNMaudeRuleBuilder;
import no.tk.maude.generation.MaudeObjectBuilder;

public class BPMNMaudeGatewayRuleGenerator implements BPMNToMaudeTransformerHelper {
  private final BPMNMaudeRuleGenerator ruleGenerator;
  private final BPMNMaudeRuleBuilder ruleBuilder;
  private final MaudeObjectBuilder objectBuilder;

  public BPMNMaudeGatewayRuleGenerator(
      BPMNMaudeRuleGenerator ruleGenerator, BPMNMaudeRuleBuilder ruleBuilder) {
    this.ruleGenerator = ruleGenerator;
    this.ruleBuilder = ruleBuilder;
    objectBuilder = new MaudeObjectBuilder();
  }

  public void createExclusiveGatewayRule(
          AbstractBPMNProcess process, ExclusiveGateway exclusiveGateway) {
    exclusiveGateway
        .getIncomingFlows()
        .forEach(
            incomingFlow ->
                exclusiveGateway
                    .getOutgoingFlows()
                    .forEach(
                        outgoingFlow ->
                            createExclusiveGatewayRule(
                                process,
                                exclusiveGateway,
                                getTokenForSequenceFlow(incomingFlow),
                                outgoingFlow)));

    // No incoming flows means we expect a token sitting at the gateway.
    if (exclusiveGateway.getIncomingFlows().findAny().isEmpty()) {
      exclusiveGateway
          .getOutgoingFlows()
          .forEach(
              outgoingFlow ->
                  createExclusiveGatewayRule(
                      process,
                      exclusiveGateway,
                      getTokenForFlowNode(exclusiveGateway),
                      outgoingFlow));
    }
  }

  private void createExclusiveGatewayRule(
      AbstractBPMNProcess process,
      ExclusiveGateway exclusiveGateway,
      String preToken,
      SequenceFlow outgoingFlow) {
    ruleBuilder.startRule(getFlowNodeRuleName(exclusiveGateway));

    String preTokens = preToken + ANY_OTHER_TOKENS;
    ruleBuilder.addPreObject(
        createProcessSnapshotObjectAnySubProcessAndSignals(process, preTokens));

    String postTokens = getTokenForSequenceFlow(outgoingFlow) + ANY_OTHER_TOKENS;
    ruleBuilder.addPostObject(
        createProcessSnapshotObjectAnySubProcessAndNoSignals(process, postTokens));

    ruleBuilder.buildRule();
  }

  public void createParallelGatewayRule(
      AbstractBPMNProcess process, ParallelGateway parallelGateway) {
    ruleBuilder.startRule(getFlowNodeRuleName(parallelGateway));

    String preTokens = getPreTokensForParallelGateway(parallelGateway) + ANY_OTHER_TOKENS;
    ruleBuilder.addPreObject(
        createProcessSnapshotObjectAnySubProcessAndSignals(process, preTokens));

    String postTokens = getOutgoingTokensForFlowNode(parallelGateway) + ANY_OTHER_TOKENS;
    ruleBuilder.addPostObject(
        createProcessSnapshotObjectAnySubProcessAndNoSignals(process, postTokens));

    ruleBuilder.buildRule();
  }

  private String getPreTokensForParallelGateway(ParallelGateway parallelGateway) {
    if (parallelGateway.getIncomingFlows().findAny().isEmpty()) {
      return getTokenForFlowNode(parallelGateway);
    }
    return parallelGateway
        .getIncomingFlows()
        .map(this::getTokenForSequenceFlow)
        .collect(Collectors.joining(" "));
  }

  public void createEventBasedGatewayRule(
      AbstractBPMNProcess process, EventBasedGateway eventBasedGateway) {
    eventBasedGateway
        .getIncomingFlows()
        .forEach(
            inFlow -> {
              ruleBuilder.startRule(
                  getFlowNodeRuleNameWithIncFlow(eventBasedGateway, inFlow.getId()));

              String preTokens = getTokenForSequenceFlow(inFlow) + ANY_OTHER_TOKENS;
              ruleBuilder.addPreObject(
                  createProcessSnapshotObjectAnySubProcessAndSignals(process, preTokens));

              String postTokens = getTokenForFlowNode(eventBasedGateway) + ANY_OTHER_TOKENS;
              ruleBuilder.addPostObject(
                  createProcessSnapshotObjectAnySubProcessAndNoSignals(process, postTokens));

              ruleBuilder.buildRule();
            });
    // Effects the rules of the subsequent flow nodes!
    // Possible subsequent nodes: Message catch, Receive task, Signal catch, timer and condition.
    // We currently only implemented the first three.
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
