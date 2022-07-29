package maude.behaviortransformer.bpmn;

import behavior.bpmn.Process;
import behavior.bpmn.*;
import behavior.bpmn.auxiliary.exceptions.BPMNRuntimeException;
import behavior.bpmn.auxiliary.exceptions.ShouldNotHappenRuntimeException;
import behavior.bpmn.events.StartEvent;
import maude.generation.MaudeObject;
import maude.generation.MaudeObjectBuilder;
import maude.generation.MaudeRuleBuilder;

import java.util.Set;
import java.util.stream.Collectors;

import static groove.behaviortransformer.bpmn.BPMNToGrooveTransformerConstants.*;
import static groove.behaviortransformer.bpmn.BPMNToGrooveTransformerHelper.isAfterInstantiateEventBasedGateway;

public interface BPMNToMaudeTransformerHelper {
    String ANY_TOKENS = "T";
    String ANY_SUBPROCESSES = "S";
    String ANY_MESSAGES = "M";
    String ANY_OTHER_TOKENS = " " + ANY_TOKENS;
    String ANY_OTHER_SUBPROCESSES = " " + ANY_SUBPROCESSES;
    String ANY_OTHER_MESSAGES = " " + ANY_MESSAGES;
    String NONE = "none";

    String RULE_NAME_NAME_ID_FORMAT = "%s_%s";
    String RULE_NAME_ID_FORMAT = "%s";
    String TOKEN_FORMAT = "\"%s (%s)\""; // Name of the FlowElement followed by id.
    String TOKEN_FORMAT_ONLY_ID = "\"%s\""; // Name of the FlowElement followed by id.
    String ENQUOTE_FORMAT = "\"%s\""; // Id of the FlowElement.
    String BRACKET_FORMAT = "(%s)";
    String RUNNING = "Running";
    String TERMINATED = "Terminated";

    MaudeRuleBuilder getRuleBuilder();

    MaudeObjectBuilder getObjectBuilder();

    default String getFlowNodeRuleNameWithIncFlow(FlowNode taskOrCallActivity, String incomingFlowId) {
        if (taskOrCallActivity.getIncomingFlows().count() > 1) {
            return String.format(RULE_NAME_NAME_ID_FORMAT, getFlowNodeRuleName(taskOrCallActivity), incomingFlowId);
        }
        return getFlowNodeRuleName(taskOrCallActivity);
    }

    default String getFlowNodeRuleName(FlowNode flowNode) {
        if (flowNode.getName() == null || flowNode.getName().isBlank()) {
            return String.format(RULE_NAME_ID_FORMAT, flowNode.getId());
        }
        return String.format(RULE_NAME_NAME_ID_FORMAT, flowNode.getName(), flowNode.getId());
    }

    default String getTokenForFlowNode(FlowNode flowNode) {
        if (flowNode.getName() == null || flowNode.getName().isBlank()) {
            return String.format(TOKEN_FORMAT_ONLY_ID, flowNode.getId());
        }
        return String.format(TOKEN_FORMAT, flowNode.getName(), flowNode.getId());
    }

    default String getStartEventTokenName(StartEvent event) {
        return String.format(TOKEN_FORMAT, event.getName(), event.getId());
    }

    default String getOutgoingTokensForFlowNode(FlowNode flowNode) {
        return flowNode.getOutgoingFlows().map(this::getTokenForSequenceFlow).collect(Collectors.joining(
                " "));
    }

    default String getTokenForSequenceFlow(SequenceFlow sequenceFlow) {
        String nameOrDescriptiveName = sequenceFlow.getName() == null ||
                                       sequenceFlow.getName().isBlank() ? sequenceFlow.getDescriptiveName() :
                sequenceFlow.getName();
        return String.format(TOKEN_FORMAT, nameOrDescriptiveName, sequenceFlow.getId());
    }

    default MaudeObject createTerminatedProcessSnapshot(AbstractProcess process) {
        return createProcessSnapshotObject(process, NONE, NONE, NONE, TERMINATED);
    }


    default MaudeObject createProcessSnapshotObjectNoSubProcessAndMessages(AbstractProcess process,
                                                                           String tokens) {
        return createProcessSnapshotObject(process, NONE, tokens, NONE, RUNNING);
    }

    default MaudeObject createProcessSnapshotObjectAnySubProcessAndMessages(AbstractProcess process,
                                                                            String tokens) {
        return createProcessSnapshotObject(process,
                                           ANY_SUBPROCESSES,
                                           tokens,
                                           ANY_MESSAGES,
                                           RUNNING);
    }

    default MaudeObject createProcessSnapshotObjectAnySubProcess(AbstractProcess process,
                                                                 String tokens,
                                                                 String messages) {
        return createProcessSnapshotObject(process, ANY_SUBPROCESSES, tokens, messages, RUNNING);
    }

    default MaudeObject createProcessSnapshotObjectAnyMessages(AbstractProcess process,
                                                               String subprocesses,
                                                               String tokens,
                                                               String state) {
        return createProcessSnapshotObject(process, subprocesses, tokens, ANY_MESSAGES, state);
    }

    default MaudeObject createProcessSnapshotObject(AbstractProcess process,
                                                    String subprocesses,
                                                    String tokens,
                                                    String messages,
                                                    String state) {
        return getObjectBuilder().oid(String.format(ENQUOTE_FORMAT,
                                                    process.getName())).oidType("ProcessSnapshot").addAttributeValue(
                "tokens",
                String.format(BRACKET_FORMAT, tokens)).addAttributeValue("messages",
                                                                         String.format(BRACKET_FORMAT,
                                                                                       messages)).addAttributeValue(
                "subprocesses",
                String.format(BRACKET_FORMAT, subprocesses)).addAttributeValue("state", state).build();
    }

    default String getMessageForFlow(MessageFlow messageFlow) {
        return String.format(ENQUOTE_FORMAT, messageFlow.getName());
    }


    default void addSendMessageBehaviorForFlowNode(BPMNCollaboration collaboration,
                                                   FlowNode messageSource) {
        int mFlowCounter = 1;
        for (MessageFlow messageFlow : collaboration.outgoingMessageFlows(messageSource)) {
            if (messageFlow.getTarget().isInstantiateFlowNode()) {
                addMessageFlowInstantiateFlowNodeBehavior(collaboration,
                                                          messageFlow);
            } else if (isAfterInstantiateEventBasedGateway(messageFlow.getTarget())) {
                // TODO: Implement message to instantiate EV gateway behavior.
            } else {
                addMessageSendBehaviorIfProcessExists(collaboration, messageFlow, mFlowCounter);
                mFlowCounter++;
            }
        }
    }

    default void addMessageFlowInstantiateFlowNodeBehavior(BPMNCollaboration collaboration,
                                                           MessageFlow messageFlow) {
        Process receiverProcess = collaboration.getMessageFlowReceiverProcess(messageFlow);
        FlowNode mFlowTarget = messageFlow.getTarget();
        String tokens;
        if (mFlowTarget.isTask()) {
            // Must be an instantiate receive task.
            tokens = getTokenForFlowNode(mFlowTarget) + ANY_OTHER_TOKENS;

        } else {
            // Must be message start event.
            tokens = getOutgoingTokensForFlowNode(mFlowTarget) + ANY_OTHER_TOKENS;
        }
        getRuleBuilder().addPostObject(createProcessSnapshotObjectNoSubProcessAndMessages(receiverProcess,
                                                                                          tokens));
    }

    default void addMessageSendBehaviorIfProcessExists(BPMNCollaboration collaboration,
                                                       MessageFlow messageFlow,
                                                       int mFlowCounter) {
        Process messageFlowReceiver = collaboration.getMessageFlowReceiverProcess(messageFlow);

        // We assume a message receiver can only have one incoming sequence flow if any.
        FlowNode messageFlowTarget = messageFlow.getTarget();
        boolean behindExclusiveEventBasedGateway = messageFlowTarget.getIncomingFlows()
                                                                    .anyMatch(sequenceFlow -> sequenceFlow.getSource().isExclusiveEventBasedGateway());
        String token;
        if (behindExclusiveEventBasedGateway) {
            if (messageFlowTarget.getIncomingFlows().count() != 1) {
                throw new BPMNRuntimeException(
                        "Interaction nodes receiving a message with a preceding exclusive event based gateway must " +
                        "have exactly one incoming sequence flow!");
            }
            SequenceFlow inFlow = messageFlowTarget.getIncomingFlows()
                                                   .findAny()
                                                   .orElseThrow(ShouldNotHappenRuntimeException::new);
            token = getTokenForFlowNode(inFlow.getSource());
        } else {
            token = getTokenForFlowNode(messageFlowTarget);
        }
        // TODO: Implement optional message send/if exists.
        // Add message
        // Needs a different set of tokens, messages and subprocess variables
        getRuleBuilder().addVar(TOKENS, MSET, anyToken(mFlowCounter));
        getRuleBuilder().addVar(MESSAGES, MSET, anyMessage(mFlowCounter));
        getRuleBuilder().addVar(SUBPROCESSES, CONFIGURATION, anySubprocess(mFlowCounter));
        getRuleBuilder().addPreObject(createProcessSnapshotObject(messageFlowReceiver,
                                                                  anySubprocess(mFlowCounter),
                                                                  token + ANY_OTHER_TOKENS + mFlowCounter,
                                                                  anyMessage(mFlowCounter),
                                                                  RUNNING));
        getRuleBuilder().addPostObject(createProcessSnapshotObject(messageFlowReceiver,
                                                                   anySubprocess(mFlowCounter),
                                                                   token + ANY_OTHER_TOKENS + mFlowCounter,
                                                                   getMessageForFlow(messageFlow) +
                                                                   ANY_OTHER_MESSAGES + mFlowCounter,
                                                                   RUNNING));

    }

    private String anySubprocess(int mFlowCounter) {
        return ANY_SUBPROCESSES + mFlowCounter;
    }

    private String anyMessage(int mFlowCounter) {
        return ANY_MESSAGES + mFlowCounter;
    }

    private String anyToken(int mFlowCounter) {
        return ANY_TOKENS + mFlowCounter;
    }

    /**
     * @return the rule name of the interaction node (node which is source or target of a message flow).
     */
    default String getInteractionNodeRuleName(FlowNode flowNode,
                                              Set<MessageFlow> incomingMessageFlows,
                                              MessageFlow messageFlow) {
        if (incomingMessageFlows.size() > 1) {
            return getFlowNodeRuleNameWithIncFlow(flowNode, messageFlow.getName()) +
                   (flowNode.isTask() ? START : "");
        }
        return getFlowNodeRuleName(flowNode) + (flowNode.isTask() ? START : "");
    }

    default void createStartInteractionNodeRule(FlowNode interactionNode,
                                                AbstractProcess process) {
        interactionNode.getIncomingFlows().forEach(incomingFlow -> {
            getRuleBuilder().ruleName(getFlowNodeRuleNameWithIncFlow(interactionNode, incomingFlow.getId()));

            String preTokens = getTokenForSequenceFlow(incomingFlow) + ANY_OTHER_TOKENS;
            getRuleBuilder().addPreObject(createProcessSnapshotObjectAnySubProcessAndMessages(process, preTokens));

            String postTokens = getTokenForFlowNode(interactionNode) + ANY_OTHER_TOKENS;
            getRuleBuilder().addPostObject(createProcessSnapshotObjectAnySubProcessAndMessages(process,
                                                                                               postTokens));

            getRuleBuilder().build();
        });
    }

    default void createEndInteractionNodeRule(FlowNode interactionNode,
                                              AbstractProcess process,
                                              BPMNCollaboration collaboration) {
        Set<MessageFlow> incomingMessageFlows = collaboration.getIncomingMessageFlows(interactionNode);
        incomingMessageFlows.forEach(messageFlow -> {
            getRuleBuilder().ruleName(getInteractionNodeRuleName(interactionNode,
                                                                 incomingMessageFlows,
                                                                 messageFlow));

            String preTokens = getTokenForFlowNode(interactionNode) + ANY_OTHER_TOKENS;
            getRuleBuilder().addPreObject(createProcessSnapshotObjectAnySubProcess(process,
                                                                                   preTokens,
                                                                                   getMessageForFlow(messageFlow) +
                                                                                   ANY_OTHER_MESSAGES));

            String postTokens = getOutgoingTokensForFlowNode(interactionNode) + ANY_OTHER_TOKENS;
            getRuleBuilder().addPostObject(createProcessSnapshotObjectAnySubProcessAndMessages(process,
                                                                                               postTokens));

            getRuleBuilder().build();
        });
    }
}
