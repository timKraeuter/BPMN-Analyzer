package groove.behaviorTransformer.bpmn;

import behavior.bpmn.Process;
import behavior.bpmn.*;
import groove.behaviorTransformer.GrooveTransformer;
import groove.graph.GrooveNode;
import groove.graph.rule.GrooveRuleBuilder;

import static groove.behaviorTransformer.GrooveTransformer.*;
import static groove.behaviorTransformer.GrooveTransformerHelper.createStringNodeLabel;
import static groove.behaviorTransformer.bpmn.BPMNToGrooveTransformerConstants.*;

public class BPMNToGrooveTransformerHelper {

    private BPMNToGrooveTransformerHelper() {
        // Only helper class with static methods.
    }

    static void updateTokenPositionForProcessInstance(String oldPosition,
                                                      String newPosition,
                                                      GrooveRuleBuilder ruleBuilder,
                                                      GrooveNode processInstance) {
        GrooveNode token = ruleBuilder.contextNode(TYPE_TOKEN);
        ruleBuilder.contextEdge(TOKENS, processInstance, token);
        GrooveNode oldTokenPosition = ruleBuilder.contextNode(createStringNodeLabel(oldPosition));
        ruleBuilder.deleteEdge(POSITION, token, oldTokenPosition);

        GrooveNode newTokenPosition = ruleBuilder.contextNode(createStringNodeLabel(newPosition));
        ruleBuilder.addEdge(POSITION, token, newTokenPosition);
    }

    public static void updateTokenPositionWhenRunning(AbstractProcess process,
                                                      String oldPosition,
                                                      String newPosition,
                                                      GrooveRuleBuilder ruleBuilder) {
        // Process instance has to be running
        GrooveNode processInstance = contextProcessInstance(process, ruleBuilder);

        // Update tokens
        updateTokenPositionForProcessInstance(oldPosition, newPosition, ruleBuilder, processInstance);
    }

    public static GrooveNode contextProcessInstance(AbstractProcess process, GrooveRuleBuilder ruleBuilder) {
        GrooveNode processInstance = contextProcessInstanceWithName(process, ruleBuilder);
        GrooveNode running = ruleBuilder.contextNode(TYPE_RUNNING);
        ruleBuilder.contextEdge(STATE, processInstance, running);
        return processInstance;
    }

    public static GrooveNode contextExistsOptionalProcessInstance(AbstractProcess processForEvent,
                                                                  GrooveRuleBuilder ruleBuilder,
                                                                  GrooveNode existsOptional) {
        GrooveNode processInstance;
        processInstance = contextProcessInstanceWithName(processForEvent, ruleBuilder);
        GrooveNode running = ruleBuilder.contextNode(TYPE_RUNNING);
        ruleBuilder.contextEdge(STATE, processInstance, running);
        ruleBuilder.contextEdge(GrooveTransformer.AT, processInstance, existsOptional);
        ruleBuilder.contextEdge(GrooveTransformer.AT, running, existsOptional);
        return processInstance;
    }

    public static GrooveNode addProcessInstance(GrooveRuleBuilder ruleBuilder, String processName) {
        GrooveNode processInstance = addProcessInstanceWithName(ruleBuilder, processName);
        ruleBuilder.addEdge(STATE, processInstance, ruleBuilder.addNode(TYPE_RUNNING));
        return processInstance;
    }

    private static GrooveNode addProcessInstanceWithName(GrooveRuleBuilder ruleBuilder, String processName) {
        GrooveNode processInstance = ruleBuilder.addNode(TYPE_PROCESS_SNAPSHOT);
        ruleBuilder.addEdge(NAME, processInstance, ruleBuilder.contextNode(createStringNodeLabel(processName)));
        return processInstance;
    }

    public static GrooveNode addExistsOptionalProcessInstance(GrooveRuleBuilder ruleBuilder,
                                                              String processName,
                                                              GrooveNode existsOptional) {
        GrooveNode processInstance = addProcessInstanceWithName(ruleBuilder, processName);
        GrooveNode running = ruleBuilder.addNode(TYPE_RUNNING);
        ruleBuilder.addEdge(STATE, processInstance, running);
        ruleBuilder.contextEdge(GrooveTransformer.AT, processInstance, existsOptional);
        ruleBuilder.contextEdge(GrooveTransformer.AT, running, existsOptional);
        return processInstance;
    }

    public static GrooveNode contextProcessInstanceWithName(AbstractProcess process, GrooveRuleBuilder ruleBuilder) {
        GrooveNode processInstance = ruleBuilder.contextNode(TYPE_PROCESS_SNAPSHOT);
        ruleBuilder.contextEdge(NAME,
                                processInstance,
                                ruleBuilder.contextNode(createStringNodeLabel(process.getName())));
        return processInstance;
    }

    public static void addOutgoingTokensForFlowNodeToProcessInstance(FlowNode flowNode,
                                                                     GrooveRuleBuilder ruleBuilder,
                                                                     GrooveNode processInstance) {
        flowNode.getOutgoingFlows().forEach(sequenceFlow -> addTokenWithPosition(ruleBuilder,
                                                                                 processInstance,
                                                                                 sequenceFlow.getID()));
    }

    public static void deleteTokenWithPosition(GrooveRuleBuilder ruleBuilder,
                                               GrooveNode processInstance,
                                               String position) {
        GrooveNode token = ruleBuilder.deleteNode(TYPE_TOKEN);
        ruleBuilder.deleteEdge(TOKENS, processInstance, token);
        ruleBuilder.deleteEdge(POSITION, token, ruleBuilder.contextNode(createStringNodeLabel(position)));
    }

    public static void addTokenWithPosition(GrooveRuleBuilder ruleBuilder,
                                            GrooveNode processInstance,
                                            String position) {
        GrooveNode newToken = ruleBuilder.addNode(TYPE_TOKEN);
        ruleBuilder.addEdge(TOKENS, processInstance, newToken);
        ruleBuilder.addEdge(POSITION, newToken, ruleBuilder.contextNode(createStringNodeLabel(position)));
    }

    public static GrooveNode deleteMessageToProcessInstanceWithPosition(GrooveRuleBuilder ruleBuilder,
                                                                        GrooveNode processInstance,
                                                                        String position) {
        GrooveNode message = ruleBuilder.deleteNode(TYPE_MESSAGE);
        ruleBuilder.deleteEdge(MESSAGES, processInstance, message);
        ruleBuilder.deleteEdge(POSITION, message, ruleBuilder.contextNode(createStringNodeLabel(position)));
        return message;
    }

    static void addExistentialMessageWithPosition(GrooveRuleBuilder ruleBuilder,
                                                  GrooveNode processInstance,
                                                  String position,
                                                  GrooveNode existsOptional) {
        GrooveNode newMessage = ruleBuilder.addNode(TYPE_MESSAGE);
        ruleBuilder.contextEdge(GrooveTransformer.AT, newMessage, existsOptional);
        ruleBuilder.addEdge(MESSAGES, processInstance, newMessage);
        ruleBuilder.addEdge(POSITION, newMessage, ruleBuilder.contextNode(createStringNodeLabel(position)));
    }

    public static void addOutgoingMessagesForFlowNode(BPMNCollaboration collaboration,
                                                      GrooveRuleBuilder ruleBuilder,
                                                      FlowNode producingMessageFlowNode) {
        collaboration.getMessageFlows().stream().filter(messageFlow -> messageFlow.getSource() == producingMessageFlowNode).forEach(
                messageFlow -> {
                    if (messageFlow.getTarget().isInstantiateFlowNode()) {
                        // In case of instantiate receive tasks or start events with trigger and active process
                        // instance does
                        // not exist!
                        GrooveNode newMessage = ruleBuilder.addNode(TYPE_MESSAGE);
                        ruleBuilder.addEdge(POSITION,
                                            newMessage,
                                            ruleBuilder.contextNode(createStringNodeLabel(messageFlow.getName())));
                    } else {
                        Process messageFlowReceiver = collaboration.getMessageFlowReceiver(messageFlow);
                        // If a process instance exists, send a message.
                        GrooveNode existsOptional = ruleBuilder.contextNode(EXISTS_OPTIONAL);
                        GrooveNode receiverInstance = contextExistsOptionalProcessInstance(messageFlowReceiver,
                                                                                           ruleBuilder,
                                                                                           existsOptional);
                        addExistentialMessageWithPosition(ruleBuilder,
                                                          receiverInstance,
                                                          messageFlow.getName(),
                                                          existsOptional);
                        // We assume a message receiver can only have one incoming flow if any.
                        messageFlow.getTarget().getIncomingFlows().forEach(sequenceFlow -> {
                            GrooveNode token = ruleBuilder.contextNode(TYPE_TOKEN);
                            ruleBuilder.contextEdge(TOKENS, receiverInstance, token);
                            String tokenPosition;
                            if (sequenceFlow.getSource().isExclusiveEventBasedGateway()) {
                                tokenPosition = sequenceFlow.getSource().getName();
                            } else {
                                tokenPosition = sequenceFlow.getID();
                            }
                            ruleBuilder.contextEdge(POSITION,
                                                    token,
                                                    ruleBuilder.contextNode(createStringNodeLabel(tokenPosition)));
                            ruleBuilder.contextEdge(AT, token, existsOptional);
                        });
                        // TODO: Afterwards remove deleting messages from terminate rule.
                    }
                });
    }

    public static GrooveNode deleteIncomingMessageAndCreateProcessInstance(MessageFlow incomingMessageFlow,
                                                                           BPMNCollaboration collaboration,
                                                                           GrooveRuleBuilder ruleBuilder) {
        Process receiverProcess = collaboration.getMessageFlowReceiver(incomingMessageFlow);
        GrooveNode message = ruleBuilder.deleteNode(TYPE_MESSAGE);
        ruleBuilder.deleteEdge(POSITION,
                               message,
                               ruleBuilder.contextNode(createStringNodeLabel(incomingMessageFlow.getName())));

        return addProcessInstance(ruleBuilder, receiverProcess.getName());
    }

    public static GrooveNode addTokensForOutgoingFlowsToRunningInstance(FlowNode flowNode,
                                                                        AbstractProcess process,
                                                                        GrooveRuleBuilder ruleBuilder) {
        GrooveNode processInstance = contextProcessInstance(process, ruleBuilder);
        addOutgoingTokensForFlowNodeToProcessInstance(flowNode, ruleBuilder, processInstance);
        return processInstance;
    }

    public static GrooveNode deleteAllTokensForProcess(GrooveRuleBuilder ruleBuilder,
                                                       GrooveNode parentProcessInstance) {
        GrooveNode forAll = ruleBuilder.contextNode(FORALL);
        GrooveNode parentToken = ruleBuilder.deleteNode(TYPE_TOKEN);
        ruleBuilder.deleteEdge(TOKENS, parentProcessInstance, parentToken);
        ruleBuilder.contextEdge(AT, parentToken, forAll);
        return forAll;
    }
}
