package maude.behaviortransformer.bpmn;

import static groove.behaviortransformer.bpmn.BPMNToGrooveTransformerConstants.*;
import static groove.behaviortransformer.bpmn.BPMNToGrooveTransformerHelper.isAfterInstantiateEventBasedGateway;
import static maude.behaviortransformer.bpmn.BPMNToMaudeTransformerConstants.*;

import behavior.bpmn.*;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;
import maude.behaviortransformer.bpmn.settings.MaudeBPMNGenerationSettings;
import maude.generation.BPMNMaudeRuleBuilder;
import maude.generation.MaudeObject;
import maude.generation.MaudeObjectBuilder;

public interface BPMNToMaudeTransformerHelper {
  BPMNMaudeRuleBuilder getRuleBuilder();

  MaudeObjectBuilder getObjectBuilder();

  BPMNCollaboration getCollaboration();

  MaudeBPMNGenerationSettings getSettings();

  default String getFlowNodeRuleNameWithIncFlow(FlowNode flowNode, String incomingFlowId) {
    if (flowNode.getIncomingFlows().count() > 1) {
      return String.format(RULE_NAME_NAME_ID_FORMAT, getFlowNodeRuleName(flowNode), incomingFlowId);
    }
    return getFlowNodeRuleName(flowNode);
  }

  default String getFlowNodeRuleName(FlowNode flowNode) {
    if (flowNode.getName() == null || flowNode.getName().isBlank()) {
      return String.format(RULE_NAME_ID_FORMAT, flowNode.getId());
    }
    return String.format(RULE_NAME_NAME_ID_FORMAT, flowNode.getName(), flowNode.getId());
  }

  default String getTokenForFlowNode(FlowNode flowNode) {
    return getTokenOrSignalOccurrenceForFlowNode(flowNode, TOKEN_FORMAT_ONLY_ID, TOKEN_FORMAT);
  }

  default String getSignalOccurrenceForFlowNode(FlowNode flowNode) {
    return getTokenOrSignalOccurrenceForFlowNode(
        flowNode, SIGNAL_OCCURRENCE_FORMAT_ONLY_ID, SIGNAL_OCCURRENCE_FORMAT);
  }

  private String getTokenOrSignalOccurrenceForFlowNode(
      FlowNode flowNode, String formatOnlyId, String format) {
    if (flowNode.getName() == null || flowNode.getName().isBlank()) {
      return String.format(formatOnlyId, flowNode.getId());
    }
    return String.format(format, flowNode.getName(), flowNode.getId());
  }

  default String getSignalOccurrenceForSequenceFlow(SequenceFlow sequenceFlow) {
    return getTokenOrSignalOccurrenceForSequenceFlow(sequenceFlow, SIGNAL_OCCURRENCE_FORMAT);
  }

  private String getTokenOrSignalOccurrenceForSequenceFlow(
      SequenceFlow sequenceFlow, String signalOccurenceFormat) {
    String nameOrDescriptiveName =
        sequenceFlow.getName() == null || sequenceFlow.getName().isBlank()
            ? sequenceFlow.getDescriptiveName()
            : sequenceFlow.getName();
    return String.format(signalOccurenceFormat, nameOrDescriptiveName, sequenceFlow.getId());
  }

  default String getOutgoingTokensForFlowNode(FlowNode flowNode) {
    return flowNode
        .getOutgoingFlows()
        .map(this::getTokenForSequenceFlow)
        .collect(Collectors.joining(WHITE_SPACE));
  }

  default String getTokenForSequenceFlow(SequenceFlow sequenceFlow) {
    return getTokenOrSignalOccurrenceForSequenceFlow(sequenceFlow, TOKEN_FORMAT);
  }

  default MaudeObject createTerminatedProcessSnapshot(AbstractBPMNProcess process) {
    String oid = "OT";
    getRuleBuilder().addVar(OIDS, OID, oid);
    // Is a subprocess. Thus, should not have parents!
    return createProcessSnapshotObjectWithoutParents(process, oid, NONE, NONE, NONE, TERMINATED);
  }

  default MaudeObject createProcessSnapshotObjectNoSubProcessAndSignals(
      AbstractBPMNProcess process, String oid, String tokens) {
    return createProcessSnapshotObject(process, oid, NONE, tokens, NONE, RUNNING);
  }

  default MaudeObject createProcessSnapshotObjectNoSubProcessAndSignals(
      AbstractBPMNProcess process, String tokens) {
    return createProcessSnapshotObjectRunning(process, NONE, tokens, NONE);
  }

  default MaudeObject createProcessSnapshotObjectAnySubProcessAndSignals(
      AbstractBPMNProcess process, String tokens) {
    return createProcessSnapshotObjectRunning(process, ANY_SUBPROCESSES, tokens, ANY_SIGNALS);
  }

  default MaudeObject createProcessSnapshotObjectAnySubProcessAndNoSignals(
      AbstractBPMNProcess process, String tokens) {
    return createProcessSnapshotObjectRunning(process, ANY_SUBPROCESSES, tokens, NONE);
  }

  default MaudeObject createProcessSnapshotObjectRunning(
      AbstractBPMNProcess process, String subprocesses, String tokens, String signals) {
    return createProcessSnapshotObject(process, subprocesses, tokens, signals, RUNNING);
  }

  default MaudeObject createProcessSnapshotObjectWithParents(
      AbstractBPMNProcess process, String subprocesses, String tokens) {
    MaudeObject processObject =
        createProcessSnapshotObjectWithoutParents(
            process, O + 0, subprocesses, tokens, ANY_SIGNALS, RUNNING);
    return wrapInParentIfNeeded(process, processObject, 1);
  }

  default MaudeObject wrapInParentIfNeeded(
      AbstractBPMNProcess process, MaudeObject processObject, int counter) {
    AbstractBPMNProcess parentProcess = getCollaboration().getParentProcess(process);
    if (parentProcess.equals(process)) {
      return processObject;
    }
    String oid = O + counter;
    String anySubprocesses = ANY_SUBPROCESSES + counter;
    String anyTokens = ANY_TOKENS + counter;
    String anySignals = ANY_SIGNALS + counter;
    getRuleBuilder().addVar(OIDS, OID, oid);
    getRuleBuilder().addVar(TOKENS, MSET, anyTokens);
    getRuleBuilder().addVar(SIGNALS, MSET, anySignals);
    getRuleBuilder().addVar(SUBPROCESSES, CONFIGURATION, anySubprocesses);

    MaudeObject parentProcessObject =
        createProcessSnapshotObjectWithoutParents(
            parentProcess,
            oid,
            processObject.generateObjectString() + WHITE_SPACE + anySubprocesses,
            anyTokens,
            anySignals,
            RUNNING);
    return wrapInParentIfNeeded(parentProcess, parentProcessObject, counter + 1);
  }

  default MaudeObject createProcessSnapshotObject(
      AbstractBPMNProcess process,
      String subprocesses,
      String tokens,
      String signals,
      String state) {
    return createProcessSnapshotObject(process, O + 0, subprocesses, tokens, signals, state);
  }

  default MaudeObject createProcessSnapshotObject(
      AbstractBPMNProcess process,
      String oid,
      String subprocesses,
      String tokens,
      String signals,
      String state) {

    MaudeObject processObject =
        getObjectBuilder()
            .oid(oid)
            .oidType("ProcessSnapshot")
            .addAttributeValue("name", String.format(ENQUOTE_FORMAT, process.getName()))
            .addAttributeValue("tokens", String.format(BRACKET_FORMAT, tokens))
            .addAttributeValue("signals", String.format(BRACKET_FORMAT, signals))
            .addAttributeValue("subprocesses", String.format(BRACKET_FORMAT, subprocesses))
            .addAttributeValue("state", state)
            .build();
    if (getSettings().isPersistentMessages()) {
      return processObject;
    }
    // Non-persistent means messages are removed. Thus, we need parents for processes for the
    // correct match.
    return wrapInParentIfNeeded(process, processObject, 1);
  }

  default MaudeObject createProcessSnapshotObjectWithoutParents(
      AbstractBPMNProcess process,
      String oid,
      String subprocesses,
      String tokens,
      String signals,
      String state) {
    return getObjectBuilder()
        .oid(oid)
        .oidType("ProcessSnapshot")
        .addAttributeValue("name", String.format(ENQUOTE_FORMAT, process.getName()))
        .addAttributeValue("tokens", String.format(BRACKET_FORMAT, tokens))
        .addAttributeValue("signals", String.format(BRACKET_FORMAT, signals))
        .addAttributeValue("subprocesses", String.format(BRACKET_FORMAT, subprocesses))
        .addAttributeValue("state", state)
        .build();
  }

  default String getMessageForFlow(MessageFlow messageFlow) {
    return String.format(TOKEN_FORMAT, messageFlow.getNameOrDescriptiveName(), messageFlow.getId());
  }

  default void addSendMessageBehaviorForFlowNode(FlowNode messageSource) {
    Set<MessageFlow> nonInstantiateMessageFlows = new LinkedHashSet<>();
    for (MessageFlow messageFlow : getCollaboration().outgoingMessageFlows(messageSource)) {
      if (messageFlow.getTarget().isInstantiateFlowNode()
          || isAfterInstantiateEventBasedGateway(messageFlow.getTarget())) {
        addMessageFlowInstantiateFlowNodeBehavior(getCollaboration(), messageFlow);
      } else {
        nonInstantiateMessageFlows.add(messageFlow);
      }
    }
    addMessagesCreation(nonInstantiateMessageFlows);
  }

  default void addMessageFlowInstantiateFlowNodeBehavior(
      BPMNCollaboration collaboration, MessageFlow messageFlow) {
    AbstractBPMNProcess receiverProcess = collaboration.getMessageFlowReceiverProcess(messageFlow);
    FlowNode mFlowTarget = messageFlow.getTarget();
    String tokens = getTokenForFlowNode(mFlowTarget) + ANY_OTHER_TOKENS;
    getRuleBuilder()
        .addPostObject(createProcessSnapshotObjectNoSubProcessAndSignals(receiverProcess, tokens));
    addMessageCreation(messageFlow);
  }

  default void addMessageConsumption(MessageFlow messageFlow) {
    getRuleBuilder().addMessageConsumption(getMessageForFlow(messageFlow));
  }

  default void addMessageCreation(MessageFlow messageFlow) {
    getRuleBuilder().addMessageCreation(getMessageForFlow(messageFlow));
  }

  default void addMessagesCreation(Set<MessageFlow> messageFlows) {
    if (messageFlows.isEmpty()) {
      return;
    }
    messageFlows.forEach(this::addMessageCreation);
  }

  private boolean isAfterExclusiveEventBasedGateway(FlowNode messageFlowTarget) {
    return messageFlowTarget
        .getIncomingFlows()
        .anyMatch(sequenceFlow -> sequenceFlow.getSource().isExclusiveEventBasedGateway());
  }

  /**
   * Return the rule name of the interaction node (node which is source or target of a message
   * flow).
   */
  default String getInteractionNodeRuleName(
      FlowNode flowNode, Set<MessageFlow> incomingMessageFlows, MessageFlow messageFlow) {
    String potentialSuffix = flowNode.isTask() ? END : "";
    if (incomingMessageFlows.size() > 1) {
      return getFlowNodeRuleNameWithIncFlow(flowNode, messageFlow.getNameOrDescriptiveName())
          + potentialSuffix;
    }
    return getFlowNodeRuleName(flowNode) + potentialSuffix;
  }

  default void createStartInteractionNodeRule(
      FlowNode interactionNode, AbstractBPMNProcess process) {
    if (isAfterExclusiveEventBasedGateway(interactionNode)
        || interactionNode.isInstantiateFlowNode()) {
      return; // No start rule needed.
    }
    interactionNode
        .getIncomingFlows()
        .forEach(
            incomingFlow -> {
              getRuleBuilder()
                  .startRule(
                      getFlowNodeRuleNameWithIncFlow(interactionNode, incomingFlow.getId())
                          + START);

              String preTokens = getTokenForSequenceFlow(incomingFlow) + ANY_OTHER_TOKENS;
              getRuleBuilder()
                  .addPreObject(
                      createProcessSnapshotObjectAnySubProcessAndSignals(process, preTokens));

              String postTokens = getTokenForFlowNode(interactionNode) + ANY_OTHER_TOKENS;
              getRuleBuilder()
                  .addPostObject(
                      createProcessSnapshotObjectAnySubProcessAndNoSignals(process, postTokens));

              getRuleBuilder().buildRule();
            });
  }

  default void createEndInteractionNodeRule(FlowNode interactionNode, AbstractBPMNProcess process) {
    Set<MessageFlow> incomingMessageFlows =
        getCollaboration().getIncomingMessageFlows(interactionNode);
    incomingMessageFlows.forEach(
        messageFlow -> {
          getRuleBuilder()
              .startRule(
                  getInteractionNodeRuleName(interactionNode, incomingMessageFlows, messageFlow));
          String preTokens = getConsumedTokenForInteractionNode(interactionNode) + ANY_OTHER_TOKENS;
          getRuleBuilder()
              .addPreObject(
                  createProcessSnapshotObjectWithParents(process, ANY_SUBPROCESSES, preTokens));
          addMessageConsumption(messageFlow);

          String postTokens = getOutgoingTokensForFlowNode(interactionNode) + ANY_OTHER_TOKENS;
          getRuleBuilder()
              .addPostObject(
                  createProcessSnapshotObjectWithParents(process, ANY_SUBPROCESSES, postTokens));

          getRuleBuilder().buildRule();
        });
  }

  private String getConsumedTokenForInteractionNode(FlowNode interactionNode) {
    if (isAfterExclusiveEventBasedGateway(interactionNode)
        && !isAfterInstantiateEventBasedGateway(interactionNode)) {
      // Must exist if the method above returns true.
      FlowNode eventBasedGateway = interactionNode.getIncomingFlows().findAny().get().getSource();
      return getTokenForFlowNode(eventBasedGateway);
    }
    return getTokenForFlowNode(interactionNode);
  }
}
