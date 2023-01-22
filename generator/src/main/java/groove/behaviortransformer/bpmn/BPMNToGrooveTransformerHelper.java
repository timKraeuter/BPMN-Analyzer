package groove.behaviortransformer.bpmn;

import static groove.behaviortransformer.GrooveTransformer.*;
import static groove.behaviortransformer.GrooveTransformerHelper.createStringNodeLabel;
import static groove.behaviortransformer.bpmn.BPMNToGrooveTransformerConstants.*;

import behavior.bpmn.*;
import behavior.bpmn.activities.CallActivity;
import behavior.bpmn.activities.tasks.ReceiveTask;
import behavior.bpmn.activities.tasks.SendTask;
import behavior.bpmn.activities.tasks.Task;
import behavior.bpmn.auxiliary.exceptions.BPMNRuntimeException;
import behavior.bpmn.auxiliary.visitors.FlowNodeVisitor;
import behavior.bpmn.events.EndEvent;
import behavior.bpmn.events.IntermediateCatchEvent;
import behavior.bpmn.events.IntermediateCatchEventType;
import behavior.bpmn.events.IntermediateThrowEvent;
import behavior.bpmn.events.StartEvent;
import behavior.bpmn.gateways.EventBasedGateway;
import behavior.bpmn.gateways.ExclusiveGateway;
import behavior.bpmn.gateways.InclusiveGateway;
import behavior.bpmn.gateways.ParallelGateway;
import groove.behaviortransformer.GrooveTransformer;
import groove.graph.GrooveNode;
import groove.graph.rule.GrooveRuleBuilder;
import util.ValueWrapper;

public class BPMNToGrooveTransformerHelper {

  private BPMNToGrooveTransformerHelper() {
    // Only helper class with static methods.
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
    GrooveNode processInstance = ruleBuilder.contextNode(TYPE_PROCESS_SNAPSHOT);
    ruleBuilder.contextEdge(
        NAME, processInstance, ruleBuilder.contextNode(createStringNodeLabel(process.getName())));
    return processInstance;
  }

  public static void addOutgoingTokensForFlowNodeToProcessInstance(
      FlowNode flowNode,
      GrooveRuleBuilder ruleBuilder,
      GrooveNode processInstance,
      boolean useSFId) {
    flowNode
        .getOutgoingFlows()
        .forEach(
            sequenceFlow ->
                addTokenWithPosition(
                    ruleBuilder,
                    processInstance,
                    getSequenceFlowIdOrDescriptiveName(sequenceFlow, useSFId)));
  }

  public static void addOutgoingTokensForFlowNodeToProcessInstanceWithQuantifier(
      FlowNode flowNode,
      GrooveRuleBuilder ruleBuilder,
      GrooveNode processInstance,
      GrooveNode quantifier,
      boolean useSFId) {
    flowNode
        .getOutgoingFlows()
        .forEach(
            sequenceFlow -> {
              GrooveNode addedToken =
                  addTokenWithPosition(
                      ruleBuilder,
                      processInstance,
                      getSequenceFlowIdOrDescriptiveName(sequenceFlow, useSFId));
              ruleBuilder.contextEdge(AT, addedToken, quantifier);
            });
  }

  public static GrooveNode contextTokenWithPosition(
      GrooveRuleBuilder ruleBuilder, GrooveNode processInstance, String position) {
    GrooveNode token = ruleBuilder.contextNode(TYPE_TOKEN);
    ruleBuilder.contextEdge(TOKENS, processInstance, token);
    ruleBuilder.contextEdge(
        POSITION, token, ruleBuilder.contextNode(createStringNodeLabel(position)));
    return token;
  }

  public static GrooveNode deleteTokenWithPosition(
      GrooveRuleBuilder ruleBuilder, GrooveNode processInstance, String position) {
    GrooveNode token = ruleBuilder.deleteNode(TYPE_TOKEN);
    ruleBuilder.deleteEdge(TOKENS, processInstance, token);
    ruleBuilder.deleteEdge(
        POSITION, token, ruleBuilder.contextNode(createStringNodeLabel(position)));

    return token;
  }

  public static GrooveNode addTokenWithPosition(
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
      FlowNode producingMessageFlowNode,
      boolean useSFId) {
    collaboration
        .outgoingMessageFlows(producingMessageFlowNode)
        .forEach(
            messageFlow -> {
              if (messageFlow.getTarget().isInstantiateFlowNode()) {
                addMessageFlowInstantiateFlowNodeBehavior(
                    collaboration, ruleBuilder, messageFlow, useSFId);
              } else if (isAfterInstantiateEventBasedGateway(messageFlow.getTarget())) {
                instantiateMessageFlowReceiverProcess(
                    collaboration, ruleBuilder, messageFlow, useSFId);
              } else {
                addMessageSendBehaviorIfProcessExists(
                    collaboration, ruleBuilder, messageFlow, useSFId);
              }
            });
  }

  private static void addMessageSendBehaviorIfProcessExists(
      BPMNCollaboration collaboration,
      GrooveRuleBuilder ruleBuilder,
      MessageFlow messageFlow,
      boolean useSFId) {
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
                tokenPosition = sequenceFlow.getSource().getName();
              } else {
                tokenPosition = getSequenceFlowIdOrDescriptiveName(sequenceFlow, useSFId);
              }
              ruleBuilder.contextEdge(
                  POSITION, token, ruleBuilder.contextNode(createStringNodeLabel(tokenPosition)));
              ruleBuilder.contextEdge(AT, token, existsOptional);
            });
  }

  private static void addMessageFlowInstantiateFlowNodeBehavior(
      BPMNCollaboration collaboration,
      GrooveRuleBuilder ruleBuilder,
      MessageFlow messageFlow,
      boolean useSFId) {
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
      instantiateMessageFlowReceiverProcess(collaboration, ruleBuilder, messageFlow, useSFId);
    }
  }

  private static void instantiateMessageFlowReceiverProcess(
      BPMNCollaboration collaboration,
      GrooveRuleBuilder ruleBuilder,
      MessageFlow messageFlow,
      boolean useSFId) {
    AbstractBPMNProcess receiverProcess =
        collaboration.findProcessForFlowNode(messageFlow.getTarget());
    GrooveNode newReceiverProcessInstance =
        addProcessInstance(ruleBuilder, receiverProcess.getName());
    if (messageFlow.getTarget().isTask()) {
      // Instantiate tasks get a token on the task.
      addTokenWithPosition(
          ruleBuilder, newReceiverProcessInstance, messageFlow.getTarget().getName());
    } else {
      // Message start events get outgoing tokens
      addOutgoingTokensForFlowNodeToProcessInstance(
          messageFlow.getTarget(), ruleBuilder, newReceiverProcessInstance, useSFId);
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
      FlowNode flowNode,
      AbstractBPMNProcess process,
      GrooveRuleBuilder ruleBuilder,
      boolean useSFId) {
    GrooveNode processInstance = contextProcessInstance(process, ruleBuilder);
    addOutgoingTokensForFlowNodeToProcessInstance(flowNode, ruleBuilder, processInstance, useSFId);
    return processInstance;
  }

  public static GrooveNode addTokensForOutgoingFlowsToRunningInstanceWithQuantifier(
      FlowNode flowNode,
      AbstractBPMNProcess process,
      GrooveRuleBuilder ruleBuilder,
      GrooveNode quantifier,
      boolean useSFId) {
    GrooveNode processInstance =
        contextProcessInstanceWithQuantifier(process, ruleBuilder, quantifier);
    addOutgoingTokensForFlowNodeToProcessInstanceWithQuantifier(
        flowNode, ruleBuilder, processInstance, quantifier, useSFId);
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
      boolean forAllDeleteSubProcess,
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
    if (forAllDeleteSubProcess) { // for terminate and signal
      ruleBuilder.contextEdge(AT, subprocessInstance, forAllRoot);
      ruleBuilder.contextEdge(AT, subprocessRunning, forAllRoot);
    }

    // Delete all tokens or messages if any exist
    GrooveNode exists = ruleBuilder.contextNode(EXISTS);
    ruleBuilder.contextEdge(IN, exists, forAllRoot);

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

  public static String getStartEventTokenName(BPMNProcess process, StartEvent event) {
    return process.getName() + "_" + event.getName();
  }

  public static String getSequenceFlowIdOrDescriptiveName(SequenceFlow flow, boolean useID) {
    return useID ? flow.getId() : flow.getDescriptiveName();
  }

  public static boolean matchesLinkThrowEvent(
      IntermediateThrowEvent intermediateThrowEvent, FlowNode flowNode) {
    return flowNode.getName().equals(intermediateThrowEvent.getName())
        && isLinkCatchEvent(flowNode);
  }

  private static boolean isLinkCatchEvent(FlowNode flowNode) {
    ValueWrapper<Boolean> resultWrapper = new ValueWrapper<>();
    resultWrapper.setValue(false);
    flowNode.accept(
        new FlowNodeVisitor() {
          @Override
          public void handle(ExclusiveGateway exclusiveGateway) {
            // default is false
          }

          @Override
          public void handle(ParallelGateway parallelGateway) {
            // default is false
          }

          @Override
          public void handle(InclusiveGateway inclusiveGateway) {
            // default is false
          }

          @Override
          public void handle(EventBasedGateway eventBasedGateway) {
            // default is false
          }

          @Override
          public void handle(Task task) {
            // default is false
          }

          @Override
          public void handle(SendTask task) {
            // default is false
          }

          @Override
          public void handle(ReceiveTask task) {
            // default is false
          }

          @Override
          public void handle(CallActivity callActivity) {
            // default is false
          }

          @Override
          public void handle(StartEvent startEvent) {
            // default is false
          }

          @Override
          public void handle(IntermediateThrowEvent intermediateThrowEvent) {
            // default is false
          }

          @Override
          public void handle(IntermediateCatchEvent intermediateCatchEvent) {
            if (intermediateCatchEvent.getType() == IntermediateCatchEventType.LINK) {
              resultWrapper.setValue(true);
            }
          }

          @Override
          public void handle(EndEvent endEvent) {
            // default is false
          }
        });
    return resultWrapper.getValueIfExists();
  }
}
