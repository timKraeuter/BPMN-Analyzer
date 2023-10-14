package no.tk.groove.behaviortransformer.bpmn;

import static no.tk.groove.behaviortransformer.GrooveTransformer.AT;
import static no.tk.groove.behaviortransformer.GrooveTransformer.EXISTS;
import static no.tk.groove.behaviortransformer.GrooveTransformer.EXISTS_OPTIONAL;
import static no.tk.groove.behaviortransformer.GrooveTransformer.FORALL;
import static no.tk.groove.behaviortransformer.GrooveTransformer.IN;
import static no.tk.groove.behaviortransformer.GrooveTransformerHelper.createStringNodeLabel;
import static no.tk.groove.behaviortransformer.bpmn.BPMNToGrooveTransformerConstants.MESSAGES;
import static no.tk.groove.behaviortransformer.bpmn.BPMNToGrooveTransformerConstants.NAME;
import static no.tk.groove.behaviortransformer.bpmn.BPMNToGrooveTransformerConstants.POSITION;
import static no.tk.groove.behaviortransformer.bpmn.BPMNToGrooveTransformerConstants.STATE;
import static no.tk.groove.behaviortransformer.bpmn.BPMNToGrooveTransformerConstants.SUBPROCESS;
import static no.tk.groove.behaviortransformer.bpmn.BPMNToGrooveTransformerConstants.TOKENS;
import static no.tk.groove.behaviortransformer.bpmn.BPMNToGrooveTransformerConstants.TYPE_MESSAGE;
import static no.tk.groove.behaviortransformer.bpmn.BPMNToGrooveTransformerConstants.TYPE_PROCESS_SNAPSHOT;
import static no.tk.groove.behaviortransformer.bpmn.BPMNToGrooveTransformerConstants.TYPE_RUNNING;
import static no.tk.groove.behaviortransformer.bpmn.BPMNToGrooveTransformerConstants.TYPE_TERMINATED;
import static no.tk.groove.behaviortransformer.bpmn.BPMNToGrooveTransformerConstants.TYPE_TOKEN;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import no.tk.behavior.bpmn.AbstractBPMNProcess;
import no.tk.behavior.bpmn.BPMNCollaboration;
import no.tk.behavior.bpmn.FlowNode;
import no.tk.behavior.bpmn.MessageFlow;
import no.tk.behavior.bpmn.SequenceFlow;
import no.tk.behavior.bpmn.auxiliary.exceptions.BPMNRuntimeException;
import no.tk.behavior.bpmn.auxiliary.visitors.DoNothingFlowNodeVisitor;
import no.tk.behavior.bpmn.events.IntermediateCatchEvent;
import no.tk.behavior.bpmn.events.IntermediateCatchEventType;
import no.tk.behavior.bpmn.events.IntermediateThrowEvent;
import no.tk.groove.behaviortransformer.GrooveTransformer;
import no.tk.groove.graph.GrooveNode;
import no.tk.groove.graph.rule.GrooveRuleBuilder;
import no.tk.util.ValueWrapper;

public class BPMNToGrooveTransformerHelper {

  private BPMNToGrooveTransformerHelper() {
    // Only helper class with static methods.
  }

  public static String getFlowNodePosition(FlowNode flowNode) {
    return String.format("%s (%s)", flowNode.getName(), flowNode.getId());
  }

  static void updateTokenPositionForProcessInstance(
      String oldPosition,
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

  public static void updateTokenPositionWhenRunning(
      AbstractBPMNProcess process,
      String oldPosition,
      String newPosition,
      GrooveRuleBuilder ruleBuilder) {
    // Process instance has to be running
    GrooveNode processInstance = contextProcessInstance(process, ruleBuilder);

    // Update tokens
    updateTokenPositionForProcessInstance(oldPosition, newPosition, ruleBuilder, processInstance);
  }

  public static GrooveNode contextProcessInstance(
      AbstractBPMNProcess process, GrooveRuleBuilder ruleBuilder) {
    GrooveNode processInstance = contextProcessInstanceWithOnlyName(process, ruleBuilder);
    GrooveNode running = ruleBuilder.contextNode(TYPE_RUNNING);
    ruleBuilder.contextEdge(STATE, processInstance, running);
    return processInstance;
  }

  public static GrooveNode contextProcessInstanceWithQuantifier(
      AbstractBPMNProcess process, GrooveRuleBuilder ruleBuilder, GrooveNode quantifier) {
    GrooveNode processInstance;
    processInstance = contextProcessInstanceWithOnlyName(process, ruleBuilder);
    GrooveNode running = ruleBuilder.contextNode(TYPE_RUNNING);
    ruleBuilder.contextEdge(STATE, processInstance, running);
    ruleBuilder.contextEdge(GrooveTransformer.AT, processInstance, quantifier);
    ruleBuilder.contextEdge(GrooveTransformer.AT, running, quantifier);
    return processInstance;
  }

  public static GrooveNode addProcessInstance(GrooveRuleBuilder ruleBuilder, String processName) {
    GrooveNode processInstance = addProcessInstanceWithName(ruleBuilder, processName);
    ruleBuilder.addEdge(STATE, processInstance, ruleBuilder.addNode(TYPE_RUNNING));
    return processInstance;
  }

  private static GrooveNode addProcessInstanceWithName(
      GrooveRuleBuilder ruleBuilder, String processName) {
    GrooveNode processInstance = ruleBuilder.addNode(TYPE_PROCESS_SNAPSHOT);
    ruleBuilder.addEdge(
        NAME, processInstance, ruleBuilder.contextNode(createStringNodeLabel(processName)));
    return processInstance;
  }

  public static GrooveNode addProcessInstanceWithQuantifier(
      GrooveRuleBuilder ruleBuilder, String processName, GrooveNode quantifier) {
    GrooveNode processInstance = addProcessInstanceWithName(ruleBuilder, processName);
    GrooveNode running = ruleBuilder.addNode(TYPE_RUNNING);
    ruleBuilder.addEdge(STATE, processInstance, running);
    ruleBuilder.contextEdge(GrooveTransformer.AT, processInstance, quantifier);
    ruleBuilder.contextEdge(GrooveTransformer.AT, running, quantifier);
    return processInstance;
  }

  public static GrooveNode contextProcessInstanceWithOnlyName(
      AbstractBPMNProcess process, GrooveRuleBuilder ruleBuilder) {
    return contextProcessInstanceWithOnlyName(process.getName(), ruleBuilder);
  }

  public static GrooveNode contextProcessInstanceWithOnlyName(
      String name, GrooveRuleBuilder ruleBuilder) {
    GrooveNode processInstance = ruleBuilder.contextNode(TYPE_PROCESS_SNAPSHOT);
    ruleBuilder.contextEdge(
        NAME, processInstance, ruleBuilder.contextNode(createStringNodeLabel(name)));
    return processInstance;
  }

  public static void addOutgoingTokensForFlowNodeToProcessInstance(
      FlowNode flowNode, GrooveRuleBuilder ruleBuilder, GrooveNode processInstance) {
    flowNode
        .getOutgoingFlows()
        .forEach(sf -> addSequenceFlowToken(ruleBuilder, processInstance, sf));
  }

  public static void addOutgoingTokensForFlowNodeToProcessInstanceWithQuantifier(
      FlowNode flowNode,
      GrooveRuleBuilder ruleBuilder,
      GrooveNode processInstance,
      GrooveNode quantifier) {
    flowNode
        .getOutgoingFlows()
        .forEach(
            sf -> {
              GrooveNode addedToken = addSequenceFlowToken(ruleBuilder, processInstance, sf);
              ruleBuilder.contextEdge(AT, addedToken, quantifier);
            });
  }

  public static GrooveNode contextFlowNodeToken(
      GrooveRuleBuilder ruleBuilder, GrooveNode processInstance, FlowNode flowNode) {
    return contextTokenWithPosition(ruleBuilder, processInstance, getFlowNodePosition(flowNode));
  }

  public static GrooveNode contextTokenWithPosition(
      GrooveRuleBuilder ruleBuilder, GrooveNode processInstance, String position) {
    GrooveNode token = ruleBuilder.contextNode(TYPE_TOKEN);
    connectToProcessInstanceAndAddPosition(ruleBuilder, processInstance, position, token);
    return token;
  }

  public static GrooveNode nacTokenWithPosition(
      GrooveRuleBuilder ruleBuilder, GrooveNode processInstance, String position) {
    GrooveNode token = ruleBuilder.nacNode(TYPE_TOKEN);
    connectToProcessInstanceAndAddPosition(ruleBuilder, processInstance, position, token);
    return token;
  }

  private static void connectToProcessInstanceAndAddPosition(
      GrooveRuleBuilder ruleBuilder,
      GrooveNode processInstance,
      String position,
      GrooveNode token) {
    ruleBuilder.contextEdge(TOKENS, processInstance, token);
    ruleBuilder.contextEdge(
        POSITION, token, ruleBuilder.contextNode(createStringNodeLabel(position)));
  }

  public static GrooveNode deleteSequenceFlowToken(
      GrooveRuleBuilder ruleBuilder, GrooveNode processInstance, SequenceFlow sf) {
    return deleteTokenWithPosition(ruleBuilder, processInstance, sf.getDescriptiveName());
  }

  public static GrooveNode deleteFlowNodeToken(
      GrooveRuleBuilder ruleBuilder, GrooveNode processInstance, FlowNode flowNode) {
    return deleteTokenWithPosition(ruleBuilder, processInstance, getFlowNodePosition(flowNode));
  }

  static GrooveNode deleteTokenWithPosition(
      GrooveRuleBuilder ruleBuilder, GrooveNode processInstance, String position) {
    GrooveNode token = ruleBuilder.deleteNode(TYPE_TOKEN);
    ruleBuilder.deleteEdge(TOKENS, processInstance, token);
    ruleBuilder.deleteEdge(
        POSITION, token, ruleBuilder.contextNode(createStringNodeLabel(position)));

    return token;
  }

  public static GrooveNode addSequenceFlowToken(
      GrooveRuleBuilder ruleBuilder, GrooveNode processInstance, SequenceFlow sf) {
    return addTokenWithPosition(ruleBuilder, processInstance, sf.getDescriptiveName());
  }

  public static void addFlowNodeToken(
      GrooveRuleBuilder ruleBuilder, GrooveNode processInstance, FlowNode flowNode) {
    addTokenWithPosition(ruleBuilder, processInstance, getFlowNodePosition(flowNode));
  }

  static GrooveNode addTokenWithPosition(
      GrooveRuleBuilder ruleBuilder, GrooveNode processInstance, String position) {
    GrooveNode newToken = ruleBuilder.addNode(TYPE_TOKEN);
    ruleBuilder.addEdge(TOKENS, processInstance, newToken);
    ruleBuilder.addEdge(
        POSITION, newToken, ruleBuilder.contextNode(createStringNodeLabel(position)));
    return newToken;
  }

  public static GrooveNode deleteMessageToProcessInstanceWithPosition(
      GrooveRuleBuilder ruleBuilder, GrooveNode processInstance, String position) {
    GrooveNode message = ruleBuilder.deleteNode(TYPE_MESSAGE);
    ruleBuilder.deleteEdge(MESSAGES, processInstance, message);
    ruleBuilder.deleteEdge(
        POSITION, message, ruleBuilder.contextNode(createStringNodeLabel(position)));
    return message;
  }

  static void addExistentialMessageWithPosition(
      GrooveRuleBuilder ruleBuilder,
      GrooveNode processInstance,
      String position,
      GrooveNode existsOptional) {
    GrooveNode newMessage = ruleBuilder.addNode(TYPE_MESSAGE);
    ruleBuilder.contextEdge(GrooveTransformer.AT, newMessage, existsOptional);
    ruleBuilder.addEdge(MESSAGES, processInstance, newMessage);
    ruleBuilder.addEdge(
        POSITION, newMessage, ruleBuilder.contextNode(createStringNodeLabel(position)));
  }

  public static void addSendMessageBehaviorForFlowNode(
      BPMNCollaboration collaboration,
      GrooveRuleBuilder ruleBuilder,
      FlowNode producingMessageFlowNode) {
    collaboration
        .outgoingMessageFlows(producingMessageFlowNode)
        .forEach(
            messageFlow -> {
              if (messageFlow.getTarget().isInstantiateFlowNode()) {
                addMessageFlowInstantiateFlowNodeBehavior(collaboration, ruleBuilder, messageFlow);
              } else if (isAfterInstantiateEventBasedGateway(messageFlow.getTarget())) {
                instantiateMessageFlowReceiverProcess(collaboration, ruleBuilder, messageFlow);
              } else {
                addMessageSendBehaviorIfProcessExists(collaboration, ruleBuilder, messageFlow);
              }
            });
  }

  private static void addMessageSendBehaviorIfProcessExists(
      BPMNCollaboration collaboration, GrooveRuleBuilder ruleBuilder, MessageFlow messageFlow) {
    AbstractBPMNProcess messageFlowReceiver =
        collaboration.getMessageFlowReceiverProcess(messageFlow);
    // If a process instance exists, send a message.
    GrooveNode existsOptional = ruleBuilder.contextNode(EXISTS_OPTIONAL);
    GrooveNode receiverInstance =
        contextProcessInstanceWithQuantifier(messageFlowReceiver, ruleBuilder, existsOptional);
    addExistentialMessageWithPosition(
        ruleBuilder, receiverInstance, messageFlow.getNameOrDescriptiveName(), existsOptional);
    // We assume a message receiver can only have one incoming sequence flow if any.
    messageFlow
        .getTarget()
        .getIncomingFlows()
        .forEach(
            sequenceFlow -> {
              GrooveNode token = ruleBuilder.contextNode(TYPE_TOKEN);
              ruleBuilder.contextEdge(TOKENS, receiverInstance, token);
              String tokenPosition;
              if (sequenceFlow.getSource().isExclusiveEventBasedGateway()) {
                tokenPosition = getFlowNodePosition(sequenceFlow.getSource());
              } else {
                tokenPosition = sequenceFlow.getDescriptiveName();
              }
              ruleBuilder.contextEdge(
                  POSITION, token, ruleBuilder.contextNode(createStringNodeLabel(tokenPosition)));
              ruleBuilder.contextEdge(AT, token, existsOptional);
            });
  }

  private static void addMessageFlowInstantiateFlowNodeBehavior(
      BPMNCollaboration collaboration, GrooveRuleBuilder ruleBuilder, MessageFlow messageFlow) {
    AbstractBPMNProcess messageFlowReceiverProcess =
        collaboration.getMessageFlowReceiverProcess(messageFlow);
    if (messageFlowReceiverProcess.isEventSubprocess()) {
      // Event subprocess consumes the message and starts a process accordingly maybe interrupting
      // something.
      GrooveNode newMessage = ruleBuilder.addNode(TYPE_MESSAGE);
      ruleBuilder.addEdge(
          POSITION,
          newMessage,
          ruleBuilder.contextNode(createStringNodeLabel(messageFlow.getNameOrDescriptiveName())));
    } else {
      instantiateMessageFlowReceiverProcess(collaboration, ruleBuilder, messageFlow);
    }
  }

  private static void instantiateMessageFlowReceiverProcess(
      BPMNCollaboration collaboration, GrooveRuleBuilder ruleBuilder, MessageFlow messageFlow) {
    AbstractBPMNProcess receiverProcess =
        collaboration.findProcessForFlowNode(messageFlow.getTarget());
    GrooveNode newReceiverProcessInstance =
        addProcessInstance(ruleBuilder, receiverProcess.getName());
    if (messageFlow.getTarget().isTask()) {
      // Instantiate tasks get a token on the task.
      addFlowNodeToken(ruleBuilder, newReceiverProcessInstance, messageFlow.getTarget());
    } else {
      // Message start events get outgoing tokens
      addOutgoingTokensForFlowNodeToProcessInstance(
          messageFlow.getTarget(), ruleBuilder, newReceiverProcessInstance);
    }
  }

  public static boolean isAfterInstantiateEventBasedGateway(FlowNode target) {
    boolean isAfterInstantiateEVGateway =
        target
            .getIncomingFlows()
            .anyMatch(
                sequenceFlow ->
                    sequenceFlow.getSource().isExclusiveEventBasedGateway()
                        && sequenceFlow.getSource().isInstantiateFlowNode());
    if (isAfterInstantiateEVGateway && target.getIncomingFlows().count() > 1) {
      throw new BPMNRuntimeException(
          "Multiple incoming sequence flows into a message event after an instantiate event based gateway! "
              + "Only the sequence flow from the event based gateway is allowed.");
    }
    return isAfterInstantiateEVGateway;
  }

  public static GrooveNode addTokensForOutgoingFlowsToRunningInstance(
      FlowNode flowNode, AbstractBPMNProcess process, GrooveRuleBuilder ruleBuilder) {
    GrooveNode processInstance = contextProcessInstance(process, ruleBuilder);
    addOutgoingTokensForFlowNodeToProcessInstance(flowNode, ruleBuilder, processInstance);
    return processInstance;
  }

  public static GrooveNode addTokensForOutgoingFlowsToRunningInstanceWithQuantifier(
      FlowNode flowNode,
      AbstractBPMNProcess process,
      GrooveRuleBuilder ruleBuilder,
      GrooveNode quantifier) {
    GrooveNode processInstance =
        contextProcessInstanceWithQuantifier(process, ruleBuilder, quantifier);
    addOutgoingTokensForFlowNodeToProcessInstanceWithQuantifier(
        flowNode, ruleBuilder, processInstance, quantifier);
    return processInstance;
  }

  public static GrooveNode deleteAllTokensForProcess(
      GrooveRuleBuilder ruleBuilder, GrooveNode parentProcessInstance) {
    GrooveNode forAll = ruleBuilder.contextNode(FORALL);
    GrooveNode parentToken = ruleBuilder.deleteNode(TYPE_TOKEN);
    ruleBuilder.deleteEdge(TOKENS, parentProcessInstance, parentToken);
    ruleBuilder.contextEdge(AT, parentToken, forAll);
    return forAll;
  }

  public static GrooveNode interruptSubprocess(
      GrooveRuleBuilder ruleBuilder,
      AbstractBPMNProcess specificSubprocessIfExists,
      GrooveNode processInstance,
      boolean interruptAllSubprocesses,
      GrooveNode forAllRoot) {
    // Delete/Terminate subprocess
    GrooveNode subprocessInstance = ruleBuilder.deleteNode(TYPE_PROCESS_SNAPSHOT);
    ruleBuilder.deleteEdge(SUBPROCESS, processInstance, subprocessInstance);
    GrooveNode subprocessRunning = ruleBuilder.deleteNode(TYPE_RUNNING);
    ruleBuilder.deleteEdge(STATE, subprocessInstance, subprocessRunning);
    if (specificSubprocessIfExists != null) {
      String subprocessName = specificSubprocessIfExists.getName();
      ruleBuilder.deleteEdge(
          NAME, subprocessInstance, ruleBuilder.contextNode(createStringNodeLabel(subprocessName)));
    }

    // Delete all tokens or messages if any exist
    GrooveNode exists = ruleBuilder.contextNode(EXISTS);
    ruleBuilder.contextEdge(IN, exists, forAllRoot);

    if (interruptAllSubprocesses) { // for terminate and signal
      ruleBuilder.contextEdge(AT, subprocessInstance, forAllRoot);

      GrooveNode subprocessTerminated = ruleBuilder.deleteNode(TYPE_TERMINATED);
      ruleBuilder.deleteEdge(STATE, subprocessInstance, subprocessTerminated);
      GrooveNode existsOptionalTerminated = ruleBuilder.contextNode(EXISTS_OPTIONAL);
      ruleBuilder.contextEdge(AT, subprocessTerminated, existsOptionalTerminated);
      ruleBuilder.contextEdge(IN, existsOptionalTerminated, exists);

      GrooveNode existsOptionalRunning = ruleBuilder.contextNode(EXISTS_OPTIONAL);
      ruleBuilder.contextEdge(AT, subprocessRunning, existsOptionalRunning);
      ruleBuilder.contextEdge(IN, existsOptionalRunning, exists);
    }

    // Delete all tokens
    GrooveNode forAllTokens = ruleBuilder.contextNode(FORALL);
    GrooveNode token = ruleBuilder.deleteNode(TYPE_TOKEN);
    ruleBuilder.deleteEdge(TOKENS, subprocessInstance, token);
    ruleBuilder.contextEdge(AT, token, forAllTokens);
    ruleBuilder.contextEdge(IN, forAllTokens, exists);

    // Delete all messages
    GrooveNode forAllMessages = ruleBuilder.contextNode(FORALL);
    GrooveNode message = ruleBuilder.deleteNode(TYPE_MESSAGE);
    ruleBuilder.deleteEdge(MESSAGES, subprocessInstance, message);
    ruleBuilder.contextEdge(AT, message, forAllMessages);
    ruleBuilder.contextEdge(IN, forAllMessages, exists);

    return subprocessInstance;
  }

  public static GrooveNode interruptSubprocess(
      GrooveRuleBuilder ruleBuilder,
      AbstractBPMNProcess specificSubprocess,
      GrooveNode processInstance,
      boolean forAllDeleteSubProcess) {
    GrooveNode forAllRoot = ruleBuilder.contextNode(FORALL);
    return interruptSubprocess(
        ruleBuilder, specificSubprocess, processInstance, forAllDeleteSubProcess, forAllRoot);
  }

  public static boolean matchesLinkThrowEvent(
      IntermediateThrowEvent intermediateThrowEvent, FlowNode flowNode) {
    ValueWrapper<Boolean> resultWrapper = new ValueWrapper<>();
    resultWrapper.setValue(false);
    flowNode.accept(
        new DoNothingFlowNodeVisitor() {
          @Override
          public void handle(IntermediateCatchEvent intermediateCatchEvent) {
            if (intermediateCatchEvent.getType() == IntermediateCatchEventType.LINK
                && intermediateThrowEvent
                    .getEventDefinition()
                    .equals(intermediateCatchEvent.getEventDefinition())) {
              resultWrapper.setValue(true);
            }
          }
        });
    return resultWrapper.getValueIfExists();
  }

  public static String transformToQualifiedGrooveNameIfNeeded(String name) {
    String transformedName =
        name.replaceAll("[\\\\/:*?\"<>|]", "") // Remove unallowed characters for windows filenames.
            //
            .replace("\u00a0", "_") // Replace non-breaking whitespaces with _
            .replaceAll("\\s+", "_"); // Replace whitespaces with _
    if (!transformedName.isEmpty() && Character.isDigit(transformedName.charAt(0))) {
      // Prefix the name with a number to make it a qualified name in Groove.
      return "_" + transformedName;
    }
    return transformedName;
  }

  public static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
    Set<Object> seen = ConcurrentHashMap.newKeySet();
    return t -> seen.add(keyExtractor.apply(t));
  }

  public static String getFlowNodeRuleName(
      FlowNode flowNode, String incomingFlowId) {
    if (flowNode.getIncomingFlows().count() > 1) {
      return flowNode.getName() + "_" + incomingFlowId;
    }
    return flowNode.getName();
  }
}
