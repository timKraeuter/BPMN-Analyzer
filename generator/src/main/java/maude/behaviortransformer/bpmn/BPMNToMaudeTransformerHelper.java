package maude.behaviortransformer.bpmn;

import behavior.bpmn.*;
import behavior.bpmn.events.StartEvent;
import maude.generation.BPMNMaudeRuleBuilder;
import maude.generation.MaudeObject;
import maude.generation.MaudeObjectBuilder;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static groove.behaviortransformer.bpmn.BPMNToGrooveTransformerConstants.*;
import static groove.behaviortransformer.bpmn.BPMNToGrooveTransformerHelper.isAfterInstantiateEventBasedGateway;

public interface BPMNToMaudeTransformerHelper {
    String PROCESSES = "processes";
    String ANY_PROCESS = "P";
    String ANY_TOKENS = "T";
    String ANY_SIGNALS = "SIG";
    String ANY_SUBPROCESSES = "S";
    String ANY_MESSAGES = "M";
    String WHITE_SPACE = " ";
    String ANY_OTHER_TOKENS = WHITE_SPACE + ANY_TOKENS;
    String ANY_OTHER_SIGNALS = WHITE_SPACE + ANY_SIGNALS;
    String ANY_OTHER_SUBPROCESSES = WHITE_SPACE + ANY_SUBPROCESSES;
    String ANY_OTHER_MESSAGES = WHITE_SPACE + ANY_MESSAGES;
    String ANY_OTHER_PROCESSES = WHITE_SPACE + ANY_PROCESS;

    String NONE = "none";

    String RULE_NAME_NAME_ID_FORMAT = "%s_%s";
    String RULE_NAME_ID_FORMAT = "%s";
    String TOKEN_FORMAT = "\"%s (%s)\""; // Name of the FlowElement followed by id.
    String SIGNAL_OCCURENCE_FORMAT = "\"%s (%s)_signal\"";
    String TOKEN_FORMAT_ONLY_ID = "\"%s\""; // Name of the FlowElement followed by id.
    String SIGNAL_OCCURENCE_FORMAT_ONLY_ID = "\"%s_signal\""; // Name of the FlowElement followed by id.
    String ENQUOTE_FORMAT = "\"%s\""; // Id of the FlowElement.
    String BRACKET_FORMAT = "(%s)";
    String RUNNING = "Running";
    String TERMINATED = "Terminated";

    BPMNMaudeRuleBuilder getRuleBuilder();

    MaudeObjectBuilder getObjectBuilder();

    BPMNCollaboration getCollaboration();


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
        return getTokenOrSignalOccurrenceForFlowNode(flowNode,
                                                     SIGNAL_OCCURENCE_FORMAT_ONLY_ID,
                                                     SIGNAL_OCCURENCE_FORMAT);
    }

    private String getTokenOrSignalOccurrenceForFlowNode(FlowNode flowNode,
                                                         String formatOnlyId,
                                                         String format) {
        if (flowNode.getName() == null || flowNode.getName().isBlank()) {
            return String.format(formatOnlyId, flowNode.getId());
        }
        return String.format(format, flowNode.getName(), flowNode.getId());
    }

    default String getSignalOccurrenceForSequenceFlow(SequenceFlow sequenceFlow) {
        return getTokenOrSignalOccurenceForSequenceFlow(sequenceFlow, SIGNAL_OCCURENCE_FORMAT);
    }

    private String getTokenOrSignalOccurenceForSequenceFlow(SequenceFlow sequenceFlow, String signalOccurenceFormat) {
        String nameOrDescriptiveName = sequenceFlow.getName() == null ||
                                       sequenceFlow.getName().isBlank() ? sequenceFlow.getDescriptiveName() :
                sequenceFlow.getName();
        return String.format(signalOccurenceFormat, nameOrDescriptiveName, sequenceFlow.getId());
    }

    default String getStartEventTokenName(StartEvent event) {
        return String.format(TOKEN_FORMAT, event.getName(), event.getId());
    }

    default String getOutgoingTokensForFlowNode(FlowNode flowNode) {
        return flowNode.getOutgoingFlows().map(this::getTokenForSequenceFlow).collect(Collectors.joining(
                WHITE_SPACE));
    }

    default String getTokenForSequenceFlow(SequenceFlow sequenceFlow) {
        return getTokenOrSignalOccurenceForSequenceFlow(sequenceFlow, TOKEN_FORMAT);
    }

    default MaudeObject createTerminatedProcessSnapshot(AbstractProcess process) {
        return createProcessSnapshotObject(process, NONE, NONE, ANY_SIGNALS, TERMINATED);
    }

    default MaudeObject createProcessSnapshotObjectNoSubProcessAndSignals(AbstractProcess process,
                                                                          String tokens) {
        return createProcessSnapshotObjectRunning(process, NONE, tokens, NONE);
    }

    default MaudeObject createProcessSnapshotObjectAnySubProcessAndSignals(AbstractProcess process,
                                                                           String tokens) {
        return createProcessSnapshotObjectRunning(process,
                                                  ANY_SUBPROCESSES,
                                                  tokens,
                                                  ANY_SIGNALS);
    }

    default MaudeObject createProcessSnapshotObjectAnySubProcessAndNoSignals(AbstractProcess process,
                                                                             String tokens) {
        return createProcessSnapshotObjectRunning(process,
                                                  ANY_SUBPROCESSES,
                                                  tokens,
                                                  NONE);
    }

    default MaudeObject createProcessSnapshotObjectRunning(AbstractProcess process,
                                                           String subprocesses,
                                                           String tokens,
                                                           String signals) {
        return createProcessSnapshotObject(process, subprocesses, tokens, signals, RUNNING);
    }


    default MaudeObject createProcessSnapshotObjectWithParents(AbstractProcess process,
                                                               String subprocesses,
                                                               String tokens) {
        MaudeObject processObject = createProcessSnapshotObjectRunning(process, subprocesses, tokens, ANY_SIGNALS);
        return wrapInParentIfNeeded(process, processObject, 1);
    }

    default MaudeObject wrapInParentIfNeeded(AbstractProcess process, MaudeObject processObject, int counter) {
        AbstractProcess parentProcess = getCollaboration().getParentProcess(process);
        if (parentProcess.equals(process)) {
            return processObject;
        }
        String anySubprocesses = ANY_SUBPROCESSES + counter;
        String anyTokens = ANY_TOKENS + counter;
        String anySignals = ANY_SIGNALS + counter;
        getRuleBuilder().addVar(TOKENS, MSET, anyTokens);
        getRuleBuilder().addVar(SIGNALS, MSET, anySignals);
        getRuleBuilder().addVar(SUBPROCESSES, CONFIGURATION, anySubprocesses);

        MaudeObject parentProcessObject = createProcessSnapshotObjectRunning(parentProcess,
                                                                             processObject.generateObjectString() +
                                                                             WHITE_SPACE +
                                                                             anySubprocesses,
                                                                             anyTokens,
                                                                             anySignals);
        return wrapInParentIfNeeded(parentProcess, parentProcessObject, counter + 1);
    }

    default MaudeObject createProcessSnapshotObject(AbstractProcess process,
                                                    String subprocesses,
                                                    String tokens,
                                                    String signals,
                                                    String state) {
        return getObjectBuilder().oid(process.getName())
                                 .oidType("ProcessSnapshot")
                                 .addAttributeValue("tokens", String.format(BRACKET_FORMAT, tokens))
                                 .addAttributeValue("signals", String.format(BRACKET_FORMAT, signals))
                                 .addAttributeValue("subprocesses", String.format(BRACKET_FORMAT, subprocesses))
                                 .addAttributeValue("state", state)
                                 .build();
    }

    default String getMessageForFlow(MessageFlow messageFlow) {
        return String.format(ENQUOTE_FORMAT, messageFlow.getName());
    }


    default void addSendMessageBehaviorForFlowNode(BPMNCollaboration collaboration,
                                                   FlowNode messageSource) {
        Set<MessageFlow> nonInstantiateMessageFlows = new LinkedHashSet<>();
        for (MessageFlow messageFlow : collaboration.outgoingMessageFlows(messageSource)) {
            if (messageFlow.getTarget().isInstantiateFlowNode() ||
                isAfterInstantiateEventBasedGateway(messageFlow.getTarget())) {
                addMessageFlowInstantiateFlowNodeBehavior(collaboration,
                                                          messageFlow);
            } else {
                nonInstantiateMessageFlows.add(messageFlow);
            }
        }
        addMessagesCreation(nonInstantiateMessageFlows);
    }

    default void addMessageFlowInstantiateFlowNodeBehavior(BPMNCollaboration collaboration,
                                                           MessageFlow messageFlow) {
        AbstractProcess receiverProcess = collaboration.getMessageFlowReceiverProcess(messageFlow);
        FlowNode mFlowTarget = messageFlow.getTarget();
        String tokens = getTokenForFlowNode(mFlowTarget) + ANY_OTHER_TOKENS;
        getRuleBuilder().addPostObject(createProcessSnapshotObjectNoSubProcessAndSignals(receiverProcess,
                                                                                         tokens));
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
        return messageFlowTarget.getIncomingFlows()
                                .anyMatch(sequenceFlow -> sequenceFlow.getSource().isExclusiveEventBasedGateway());
    }

    /**
     * @return the rule name of the interaction node (node which is source or target of a message flow).
     */
    default String getInteractionNodeRuleName(FlowNode flowNode,
                                              Set<MessageFlow> incomingMessageFlows,
                                              MessageFlow messageFlow) {
        String potentialSuffix = flowNode.isTask() ? END : "";
        if (incomingMessageFlows.size() > 1) {
            return getFlowNodeRuleNameWithIncFlow(flowNode, messageFlow.getName()) + potentialSuffix;
        }
        return getFlowNodeRuleName(flowNode) + potentialSuffix;
    }

    default void createStartInteractionNodeRule(FlowNode interactionNode,
                                                AbstractProcess process) {
        if (isAfterExclusiveEventBasedGateway(interactionNode) || interactionNode.isInstantiateFlowNode()) {
            return; // No start rule needed.
        }
        interactionNode.getIncomingFlows().forEach(incomingFlow -> {
            getRuleBuilder().startRule(getFlowNodeRuleNameWithIncFlow(interactionNode, incomingFlow.getId()) + START);

            String preTokens = getTokenForSequenceFlow(incomingFlow) + ANY_OTHER_TOKENS;
            getRuleBuilder().addPreObject(createProcessSnapshotObjectAnySubProcessAndSignals(process, preTokens));

            String postTokens = getTokenForFlowNode(interactionNode) + ANY_OTHER_TOKENS;
            getRuleBuilder().addPostObject(createProcessSnapshotObjectAnySubProcessAndNoSignals(process,
                                                                                                postTokens));

            getRuleBuilder().buildRule();
        });
    }

    default void createEndInteractionNodeRule(FlowNode interactionNode,
                                              AbstractProcess process,
                                              BPMNCollaboration collaboration) {
        Set<MessageFlow> incomingMessageFlows = collaboration.getIncomingMessageFlows(interactionNode);
        incomingMessageFlows.forEach(messageFlow -> {
            getRuleBuilder().startRule(getInteractionNodeRuleName(interactionNode,
                                                                  incomingMessageFlows,
                                                                  messageFlow));
            String preTokens = getConsumedTokenForInteractionNode(interactionNode) + ANY_OTHER_TOKENS;
            getRuleBuilder().addPreObject(createProcessSnapshotObjectWithParents(process, ANY_SUBPROCESSES, preTokens));
            addMessageConsumption(messageFlow);

            String postTokens = getOutgoingTokensForFlowNode(interactionNode) + ANY_OTHER_TOKENS;
            getRuleBuilder().addPostObject(createProcessSnapshotObjectWithParents(process,
                                                                                  ANY_SUBPROCESSES,
                                                                                  postTokens));

            getRuleBuilder().buildRule();
        });
    }

    private String getConsumedTokenForInteractionNode(FlowNode interactionNode) {
        if (isAfterExclusiveEventBasedGateway(interactionNode) &&
            !isAfterInstantiateEventBasedGateway(interactionNode)) {
            // Must exist if the method above returns true.
            FlowNode eventBasedGateway = interactionNode.getIncomingFlows().findAny().get().getSource();
            return getTokenForFlowNode(eventBasedGateway);
        }
        return getTokenForFlowNode(interactionNode);
    }
}
