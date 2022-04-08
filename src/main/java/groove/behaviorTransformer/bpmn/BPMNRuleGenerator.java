package groove.behaviorTransformer.bpmn;

import behavior.bpmn.Process;
import behavior.bpmn.*;
import behavior.bpmn.activities.CallActivity;
import behavior.bpmn.activities.tasks.AbstractTask;
import behavior.bpmn.activities.tasks.ReceiveTask;
import behavior.bpmn.activities.tasks.SendTask;
import behavior.bpmn.activities.tasks.Task;
import behavior.bpmn.auxiliary.FlowNodeVisitor;
import behavior.bpmn.events.*;
import behavior.bpmn.gateways.*;
import com.google.common.collect.Sets;
import groove.graph.GrooveNode;
import groove.graph.rule.GrooveGraphRule;
import groove.graph.rule.GrooveRuleBuilder;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static groove.behaviorTransformer.GrooveTransformer.*;
import static groove.behaviorTransformer.GrooveTransformerHelper.createStringNodeLabel;
import static groove.behaviorTransformer.bpmn.BPMNToGrooveTransformerConstants.*;

public class BPMNRuleGenerator {
    GrooveRuleBuilder ruleBuilder;
    BPMNCollaboration collaboration;
    Set<Process> visitedProcessModels;

    public BPMNRuleGenerator(GrooveRuleBuilder ruleBuilder, BPMNCollaboration collaboration) {
        this.ruleBuilder = ruleBuilder;
        this.collaboration = collaboration;
        visitedProcessModels = Sets.newHashSet();

        generateRules();
    }

    public Stream<GrooveGraphRule> getRules() {

        return ruleBuilder.getRules();
    }

    private void generateRules() {
        collaboration.getParticipants().forEach(process -> {
            if (!visitedProcessModels.contains(process)) {
                visitedProcessModels.add(process);
                generateRulesForProcess(process);
            }
        });
    }

    private void generateRulesForProcess(AbstractProcess process) {
        process.getControlFlowNodes().forEach(node -> node.accept(new RuleGenerationFlowNodeVisitor(this, process)));

        process.getEventSubprocesses().forEach(eventSubprocess -> this.generateRulesForEventSubprocess(process,
                                                                                                       eventSubprocess));
    }

    void addOutgoingMessagesForFlowNode(FlowNode producingMessageFlowNode) {
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
                        GrooveNode receiverInstance =
                                BPMNToGrooveTransformerHelper.createRunningExistsOptionalProcessInstance(
                                        ruleBuilder,
                                        messageFlowReceiver,
                                        existsOptional);
                        BPMNToGrooveTransformerHelper.addExistentialMessageWithPosition(ruleBuilder,
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

    void createReceiveTaskStartRule(AbstractProcess process, ReceiveTask receiveTask, SequenceFlow incomingFlow) {
        if (incomingFlow.getSource().isExclusiveEventBasedGateway()) {
            createEventBasedGatewayStartTaskRule(process, ruleBuilder, collaboration, receiveTask, incomingFlow);
        } else {
            // Should only be able to start when one message is present.
            collaboration.getIncomingMessageFlows(receiveTask).forEach(messageFlow -> {
                // TODO: We need to make the rule name unique here, if there are multiple incoming messages!
                // TODO: Delete all other possible messages!
                createStartTaskRule(process,
                                    ruleBuilder,
                                    receiveTask,
                                    incomingFlow,
                                    (startRuleBuilder, processInstance) -> BPMNToGrooveTransformerHelper.deleteMessageToProcessInstanceWithPosition(
                                            startRuleBuilder,
                                            processInstance,
                                            messageFlow.getName()));

            });
        }
    }

    private void createEventBasedGatewayStartTaskRule(AbstractProcess process,
                                                      GrooveRuleBuilder ruleBuilder,
                                                      BPMNCollaboration collaboration,
                                                      ReceiveTask receiveTask,
                                                      SequenceFlow incomingFlow) {
        collaboration.getIncomingMessageFlows(receiveTask).forEach(messageFlow -> {
            final String incomingFlowId = incomingFlow.getID();
            // TODO: We need to make the rule name unique here, if there are multiple incoming messages!
            // TODO: Delete all other possible messages!
            ruleBuilder.startRule(this.getTaskOrCallActivityRuleName(receiveTask, incomingFlowId) + START);
            GrooveNode processInstance = BPMNToGrooveTransformerHelper.createContextRunningProcessInstance(process,
                                                                                                           ruleBuilder);
            BPMNToGrooveTransformerHelper.deleteMessageToProcessInstanceWithPosition(ruleBuilder,
                                                                                     processInstance,
                                                                                     messageFlow.getName());
            BPMNToGrooveTransformerHelper.addTokenWithPosition(ruleBuilder, processInstance, receiveTask.getName());
            // Consume the token at the event-based gateway.
            BPMNToGrooveTransformerHelper.deleteTokenWithPosition(ruleBuilder,
                                                                  processInstance,
                                                                  incomingFlow.getSource().getName());
            ruleBuilder.buildRule();
        });
    }

    void createInstantiateReceiveTaskRule(AbstractProcess process, ReceiveTask receiveTask) {
        Set<MessageFlow> incomingMessageFlows = collaboration.getIncomingMessageFlows(receiveTask);
        // Each incoming message flow will instantiate the process.
        incomingMessageFlows.forEach(incomingMessageFlow -> {
            ruleBuilder.startRule(incomingMessageFlows.size() > 1 ? incomingMessageFlow.getName() :
                                          receiveTask.getName() + START);
            GrooveNode processInstance = deleteIncomingMessageAndCreateProcessInstance(incomingMessageFlow,
                                                                                       collaboration,
                                                                                       ruleBuilder);

            GrooveNode activityToken = ruleBuilder.addNode(TYPE_TOKEN);
            ruleBuilder.addEdge(POSITION,
                                activityToken,
                                ruleBuilder.contextNode(createStringNodeLabel(receiveTask.getName())));
            ruleBuilder.addEdge(TOKENS, processInstance, activityToken);
            ruleBuilder.buildRule();
        });
        // Create rules for the outgoing sequence flows.
        createTaskRules(process, receiveTask);
    }

    void createTaskRules(AbstractProcess process, AbstractTask task) {
        createTaskRules(process, task, (noop) -> {
        });
    }

    void createTaskRules(AbstractProcess process, AbstractTask task, Consumer<GrooveRuleBuilder> endTaskRuleAdditions) {
        // Rules for starting the task
        task.getIncomingFlows().forEach(incomingFlow -> createStartTaskRule(process,
                                                                            ruleBuilder,
                                                                            task,
                                                                            incomingFlow,
                                                                            (grooveRuleBuilder, grooveNode) -> {
                                                                            }));
        // Rule for ending the task
        createEndTaskRule(process, task, endTaskRuleAdditions);
    }

    void createStartTaskRule(AbstractProcess process,
                             GrooveRuleBuilder ruleBuilder,
                             AbstractTask task,
                             SequenceFlow incomingFlow,
                             BiConsumer<GrooveRuleBuilder, GrooveNode> startTaskRuleAdditions) {
        final String incomingFlowId = incomingFlow.getID();
        ruleBuilder.startRule(this.getTaskOrCallActivityRuleName(task, incomingFlowId) + START);
        GrooveNode processInstance = BPMNToGrooveTransformerHelper.createContextRunningProcessInstance(process,
                                                                                                       ruleBuilder);
        BPMNToGrooveTransformerHelper.deleteTokenWithPosition(ruleBuilder, processInstance, incomingFlowId);
        BPMNToGrooveTransformerHelper.addTokenWithPosition(ruleBuilder, processInstance, task.getName());
        startTaskRuleAdditions.accept(ruleBuilder, processInstance);
        ruleBuilder.buildRule();
    }

    void createEndTaskRule(AbstractProcess process,
                           AbstractTask task,
                           Consumer<GrooveRuleBuilder> endTaskRuleAdditions) {
        ruleBuilder.startRule(task.getName() + END);
        GrooveNode processInstance = BPMNToGrooveTransformerHelper.createContextRunningProcessInstance(process,
                                                                                                       ruleBuilder);
        BPMNToGrooveTransformerHelper.deleteTokenWithPosition(ruleBuilder, processInstance, task.getName());

        task.getOutgoingFlows().forEach(outgoingFlow -> {
            final String outgoingFlowID = outgoingFlow.getID();
            BPMNToGrooveTransformerHelper.addTokenWithPosition(ruleBuilder, processInstance, outgoingFlowID);
        });
        endTaskRuleAdditions.accept(ruleBuilder);
        ruleBuilder.buildRule();
    }

    void createSubProcessInstantiationRule(AbstractProcess process,
                                           CallActivity callActivity,
                                           SequenceFlow incomingFlow) {
        final String incomingFlowId = incomingFlow.getID();
        ruleBuilder.startRule(this.getTaskOrCallActivityRuleName(callActivity, incomingFlowId));
        GrooveNode processInstance = BPMNToGrooveTransformerHelper.createContextRunningProcessInstance(process,
                                                                                                       ruleBuilder);
        BPMNToGrooveTransformerHelper.deleteTokenWithPosition(ruleBuilder, processInstance, incomingFlowId);

        // TODO: reuse createNewProcessInstance
        GrooveNode subProcessInstance = ruleBuilder.addNode(TYPE_PROCESS_SNAPSHOT);
        ruleBuilder.contextEdge(NAME,
                                subProcessInstance,
                                ruleBuilder.contextNode(createStringNodeLabel(callActivity.getSubProcessModel().getName())));
        ruleBuilder.addEdge(SUBPROCESS, processInstance, subProcessInstance);
        GrooveNode running = ruleBuilder.addNode(TYPE_RUNNING);
        ruleBuilder.addEdge(STATE, subProcessInstance, running);
        if (callActivity.getSubProcessModel().getStartEvent() != null) {
            // Subprocess has a unique start event which gets a token!
            BPMNToGrooveTransformerHelper.addTokenWithPosition(ruleBuilder,
                                                               subProcessInstance,
                                                               getStartEventTokenName(callActivity.getSubProcessModel()));
        } else {
            // All activites and gateways without incoming sequence flows get a token.
            callActivity.getSubProcessModel().getControlFlowNodes().filter(controlFlowNode -> controlFlowNode.isTask() || controlFlowNode.isGateway()).forEach(
                    controlFlowNode -> BPMNToGrooveTransformerHelper.addTokenWithPosition(ruleBuilder,
                                                                                          subProcessInstance,
                                                                                          controlFlowNode.getName()));
        }

        ruleBuilder.buildRule();
    }

    private String getStartEventTokenName(Process process) {
        return process.getName() + "_" + process.getStartEvent().getName();
    }

    String getTaskOrCallActivityRuleName(FlowNode taskOrCallActivity, String incomingFlowId) {
        if (taskOrCallActivity.getIncomingFlows().count() > 1) {
            return taskOrCallActivity.getName() + "_" + incomingFlowId;
        }
        return taskOrCallActivity.getName();
    }

    void createRulesForExecutingTheSubProcess(CallActivity callActivity) {
        if (visitedProcessModels.contains(callActivity.getSubProcessModel())) {
            return;
        }
        visitedProcessModels.add(callActivity.getSubProcessModel());
        this.generateRulesForProcess(callActivity.getSubProcessModel());
    }

    void createBoundaryEventRules(AbstractProcess process, CallActivity callActivity) {
        callActivity.getBoundaryEvents().forEach(boundaryEvent -> {
            ruleBuilder.startRule(boundaryEvent.getName());
            GrooveNode processInstance = addTokensForOutgoingFlowsToRunningInstance(boundaryEvent,
                                                                                    process,
                                                                                    ruleBuilder);

            if (boundaryEvent.isCancelActivity()) {
                // Terminate subprocess and delete all its tokens.
                GrooveNode subProcess = ruleBuilder.deleteNode(TYPE_PROCESS_SNAPSHOT);
                ruleBuilder.contextEdge(SUBPROCESS, processInstance, subProcess);
                String subprocessName = callActivity.getSubProcessModel().getName();
                ruleBuilder.contextEdge(NAME,
                                        subProcess,
                                        ruleBuilder.contextNode(createStringNodeLabel(subprocessName)));
                GrooveNode subprocessRunning = ruleBuilder.deleteNode(TYPE_RUNNING);
                ruleBuilder.deleteEdge(STATE, subProcess, subprocessRunning);

                GrooveNode forAllTokens = ruleBuilder.contextNode(FORALL);
                GrooveNode arbitraryToken = ruleBuilder.deleteNode(TYPE_TOKEN);
                ruleBuilder.deleteEdge(TOKENS, subProcess, arbitraryToken);
                ruleBuilder.contextEdge(AT, arbitraryToken, forAllTokens);

                GrooveNode forAllMessages = ruleBuilder.contextNode(FORALL);
                GrooveNode arbitraryMessage = ruleBuilder.deleteNode(TYPE_MESSAGE);
                ruleBuilder.deleteEdge(MESSAGES, subProcess, arbitraryMessage);
                ruleBuilder.contextEdge(AT, arbitraryMessage, forAllMessages);
            }

            ruleBuilder.buildRule();
        });
    }

    void createTerminateSubProcessRule(AbstractProcess process, CallActivity callActivity) {
        ruleBuilder.startRule(callActivity.getName() + END);

        // Parent process is running
        // TODO: Shouldnt the parent have a name?
        GrooveNode processInstance = ruleBuilder.contextNode(TYPE_PROCESS_SNAPSHOT);
        GrooveNode running = ruleBuilder.contextNode(TYPE_RUNNING);
        ruleBuilder.contextEdge(STATE, processInstance, running);

        // Delete subprocess
        String subProcessName = callActivity.getSubProcessModel().getName();
        deleteTerminatedSubprocess(ruleBuilder, subProcessName, processInstance);

        // Add outgoing tokens
        callActivity.getOutgoingFlows().forEach(outgoingFlow -> {
            final String outgoingFlowID = outgoingFlow.getID();
            BPMNToGrooveTransformerHelper.addTokenWithPosition(ruleBuilder, processInstance, outgoingFlowID);
        });

        ruleBuilder.buildRule();
    }

    void createExclusiveGatewayRules(AbstractProcess process, ExclusiveGateway exclusiveGateway) {
        exclusiveGateway.getIncomingFlows().forEach(incomingFlow -> {
            final String incomingFlowId = incomingFlow.getID();
            exclusiveGateway.getOutgoingFlows().forEach(outFlow -> createRuleExclusiveGatewayRule(process,
                                                                                                  ruleBuilder,
                                                                                                  exclusiveGateway,
                                                                                                  incomingFlowId,
                                                                                                  outFlow.getID()));
        });
        // No incoming flows means we expect a token sitting at the gateway.
        if (exclusiveGateway.getIncomingFlows().findAny().isEmpty()) {
            exclusiveGateway.getOutgoingFlows().forEach(outFlow -> createRuleExclusiveGatewayRule(process,
                                                                                                  ruleBuilder,
                                                                                                  exclusiveGateway,
                                                                                                  exclusiveGateway.getName(),
                                                                                                  outFlow.getID()));
        }
    }

    private void createRuleExclusiveGatewayRule(AbstractProcess process,
                                                GrooveRuleBuilder ruleBuilder,
                                                ExclusiveGateway exclusiveGateway,
                                                String oldTokenPosition,
                                                String newTokenPosition) {
        ruleBuilder.startRule(this.getExclusiveGatewayName(exclusiveGateway, oldTokenPosition, newTokenPosition));
        BPMNToGrooveTransformerHelper.updateTokenPositionWhenRunning(process,
                                                                     oldTokenPosition,
                                                                     newTokenPosition,
                                                                     ruleBuilder);
        ruleBuilder.buildRule();
    }

    private String getExclusiveGatewayName(Gateway exclusiveGateway, String incomingFlowId, String outFlowID) {
        final long inCount = exclusiveGateway.getIncomingFlows().count();
        final long outCount = exclusiveGateway.getOutgoingFlows().count();
        if (inCount <= 1 && outCount == 1) {
            return exclusiveGateway.getName();
        }
        if (inCount <= 1) {
            return exclusiveGateway.getName() + "_" + outFlowID;
        }
        if (outCount == 1) {
            return exclusiveGateway.getName() + "_" + incomingFlowId;
        }
        return exclusiveGateway.getName() + "_" + incomingFlowId + "_" + outFlowID;
    }

    void createEventBasedGatewayRule(EventBasedGateway eventBasedGateway, AbstractProcess process) {
        boolean implicitExclusiveGateway = eventBasedGateway.getIncomingFlows().count() > 1;
        eventBasedGateway.getIncomingFlows().forEach(inFlow -> {
            String ruleName = implicitExclusiveGateway ? inFlow.getID() + "_" + eventBasedGateway.getName() :
                    eventBasedGateway.getName();
            ruleBuilder.startRule(ruleName);
            BPMNToGrooveTransformerHelper.updateTokenPositionWhenRunning(process,
                                                                         inFlow.getID(),
                                                                         eventBasedGateway.getName(),
                                                                         ruleBuilder);
            ruleBuilder.buildRule();
        });
        // Effects the rules of the subsequent flow nodes!
        // Possible subsequent nodes: Message catch, Receive task, Signal catch, timer and condition.
        // We currently only implemented the first three.
    }

    void createIntermediateCatchEventRule(IntermediateCatchEvent intermediateCatchEvent, AbstractProcess process) {
        String ruleName = CATCH + intermediateCatchEvent.getName();
        switch (intermediateCatchEvent.getType()) {
            case LINK:
                createIntermediateCatchLinkEventRule(intermediateCatchEvent, process, ruleName, ruleBuilder);
                break;
            case MESSAGE:
                createIntermediateCatchMessageEventRule(intermediateCatchEvent, process, ruleBuilder, collaboration);
                break;
            case SIGNAL:
                // no rule needed since it will be embedded in the throw rule.
                break;
            case TIMER:
                createIntermediateCatchTimerEventRule(intermediateCatchEvent, process, ruleBuilder);
                break;
        }
    }

    private void generateRulesForEventSubprocess(AbstractProcess process, EventSubprocess eventSubprocess) {
        // Start event rule generation is special
        generateRulesForStartEvents(process, eventSubprocess, collaboration, ruleBuilder);
        // Standard rule generation for other elements.
        generateRulesForProcess(eventSubprocess);
        // Termination rule
        generateTerminateEventSubProcessRule(process, eventSubprocess, ruleBuilder);
    }

    private void generateTerminateEventSubProcessRule(AbstractProcess process,
                                                      EventSubprocess eventSubprocess,
                                                      GrooveRuleBuilder ruleBuilder) {
        String eSubprocessName = eventSubprocess.getName();
        ruleBuilder.startRule(eSubprocessName + END);
        GrooveNode processInstance = BPMNToGrooveTransformerHelper.createProcessInstanceWithName(process, ruleBuilder);
        deleteTerminatedSubprocess(ruleBuilder, eSubprocessName, processInstance);
        ruleBuilder.buildRule();
    }

    private void deleteTerminatedSubprocess(GrooveRuleBuilder ruleBuilder,
                                            String eSubprocessName,
                                            GrooveNode parentProcessInstance) {
        GrooveNode subProcessInstance = ruleBuilder.deleteNode(TYPE_PROCESS_SNAPSHOT);
        ruleBuilder.deleteEdge(NAME,
                               subProcessInstance,
                               ruleBuilder.contextNode(createStringNodeLabel(eSubprocessName)));
        ruleBuilder.deleteEdge(SUBPROCESS, parentProcessInstance, subProcessInstance);
        GrooveNode terminated = ruleBuilder.deleteNode(TYPE_TERMINATED);
        ruleBuilder.deleteEdge(STATE, subProcessInstance, terminated);
    }

    private void generateRulesForStartEvents(AbstractProcess process,
                                             EventSubprocess eventSubprocess,
                                             BPMNCollaboration collaboration,
                                             GrooveRuleBuilder ruleBuilder) {
        eventSubprocess.getStartEvents().forEach(startEvent -> {
            switch (startEvent.getType()) {
                case NONE:
                    throw new RuntimeException("None start events in event subprocesses are useless!");
                case MESSAGE:
                    // TODO: Implement interrupting behavior.
                    break;
                case MESSAGE_NON_INTERRUPTING:
                    createStartNonInterruptingEvenSubprocessFromMessageRules(process,
                                                                             eventSubprocess,
                                                                             collaboration,
                                                                             ruleBuilder,
                                                                             startEvent);

                    break;
                case SIGNAL:
                    // TODO: Implement interrupting behavior.
                    break;
                case SIGNAL_NON_INTERRUPTING:
                    // TODO: Implement.
                    break;
                default:
                    throw new RuntimeException("Unexpected start event type encountered: " + startEvent.getType());
            }
        });
    }

    private void createStartNonInterruptingEvenSubprocessFromMessageRules(AbstractProcess process,
                                                                          EventSubprocess eventSubprocess,
                                                                          BPMNCollaboration collaboration,
                                                                          GrooveRuleBuilder ruleBuilder,
                                                                          StartEvent startEvent) {
        Set<MessageFlow> incomingMessageFlows = collaboration.getIncomingMessageFlows(startEvent);
        incomingMessageFlows.forEach(incomingMessageFlow -> {
            ruleBuilder.startRule(incomingMessageFlows.size() > 1 ? incomingMessageFlow.getName() :
                                          startEvent.getName());
            // Needs a running parent process
            GrooveNode processInstance = BPMNToGrooveTransformerHelper.createContextRunningProcessInstance(process,
                                                                                                           ruleBuilder);

            // Start new subprocess instance of process
            GrooveNode eventSubProcessInstance = createNewProcessInstance(ruleBuilder, eventSubprocess.getName());
            ruleBuilder.addEdge(SUBPROCESS, processInstance, eventSubProcessInstance);

            // Consumes the message
            GrooveNode message = ruleBuilder.deleteNode(TYPE_MESSAGE);
            ruleBuilder.deleteEdge(POSITION,
                                   message,
                                   ruleBuilder.contextNode(createStringNodeLabel(incomingMessageFlow.getName())));

            // Spawns a new token at each outgoing flow.
            BPMNToGrooveTransformerHelper.addOutgoingTokensForFlowNodeToProcessInstance(startEvent,
                                                                                        ruleBuilder,
                                                                                        eventSubProcessInstance);
            ruleBuilder.buildRule();
        });
    }

    private void createIntermediateCatchTimerEventRule(IntermediateCatchEvent intermediateCatchEvent,
                                                       AbstractProcess process,
                                                       GrooveRuleBuilder ruleBuilder) {

        ruleBuilder.startRule(intermediateCatchEvent.getName());
        final GrooveNode processInstance = BPMNToGrooveTransformerHelper.createContextRunningProcessInstance(process,
                                                                                                             ruleBuilder);

        if (intermediateCatchEvent.getIncomingFlows().count() != 1) {
            // current restriction, again we would need implicit exclusive gateway.
            throw new RuntimeException("Intermediate message catch events are only allowed to have one incoming " +
                                               "sequence flow!");
        }
        intermediateCatchEvent.getIncomingFlows().forEach(inFlow -> BPMNToGrooveTransformerHelper.deleteTokenWithPosition(
                ruleBuilder,
                processInstance,
                inFlow.getID()));

        // Add tokens on outgoing flows.
        BPMNToGrooveTransformerHelper.addOutgoingTokensForFlowNodeToProcessInstance(intermediateCatchEvent,
                                                                                    ruleBuilder,
                                                                                    processInstance);

        ruleBuilder.buildRule();
    }

    private GrooveNode deleteIncomingMessageAndCreateProcessInstance(MessageFlow incomingMessageFlow,
                                                                     BPMNCollaboration collaboration,
                                                                     GrooveRuleBuilder ruleBuilder) {
        Process receiverProcess = collaboration.getMessageFlowReceiver(incomingMessageFlow);
        GrooveNode message = ruleBuilder.deleteNode(TYPE_MESSAGE);
        ruleBuilder.deleteEdge(POSITION,
                               message,
                               ruleBuilder.contextNode(createStringNodeLabel(incomingMessageFlow.getName())));

        return createNewProcessInstance(ruleBuilder, receiverProcess.getName());
    }

    private GrooveNode createNewProcessInstance(GrooveRuleBuilder ruleBuilder, String processName) {
        GrooveNode processInstance = ruleBuilder.addNode(TYPE_PROCESS_SNAPSHOT);
        ruleBuilder.addEdge(NAME, processInstance, ruleBuilder.contextNode(createStringNodeLabel(processName)));
        ruleBuilder.addEdge(STATE, processInstance, ruleBuilder.addNode(TYPE_RUNNING));
        return processInstance;
    }

    private void createIntermediateCatchMessageEventRule(IntermediateCatchEvent intermediateCatchEvent,
                                                         AbstractProcess process,
                                                         GrooveRuleBuilder ruleBuilder,
                                                         BPMNCollaboration collaboration) {
        collaboration.getIncomingMessageFlows(intermediateCatchEvent).forEach(messageFlow -> {
            ruleBuilder.startRule(createCatchMessageRuleName(messageFlow, intermediateCatchEvent, collaboration));

            GrooveNode processInstance = addTokensForOutgoingFlowsToRunningInstance(intermediateCatchEvent,
                                                                                    process,
                                                                                    ruleBuilder);
            if (intermediateCatchEvent.getIncomingFlows().count() != 1) {
                // current restriction, again we would need implicit exclusive gateway.
                throw new RuntimeException("Intermediate message catch events are only allowed to have one incoming " + "sequence flow!");
            }
            //noinspection OptionalGetWithoutIsPresent Size of the stream must be 1.
            SequenceFlow incFlow = intermediateCatchEvent.getIncomingFlows().findFirst().get();
            // Consume incoming token
            if (incFlow.getSource().isExclusiveEventBasedGateway()) {
                BPMNToGrooveTransformerHelper.deleteTokenWithPosition(ruleBuilder,
                                                                      processInstance,
                                                                      incFlow.getSource().getName());
            } else {
                BPMNToGrooveTransformerHelper.deleteTokenWithPosition(ruleBuilder, processInstance, incFlow.getID());
            }
            // Consume incoming message.
            final GrooveNode deletedMessage = BPMNToGrooveTransformerHelper.deleteMessageToProcessInstanceWithPosition(
                    ruleBuilder,
                    processInstance,
                    messageFlow.getName());
            // Delete all other incoming messages.
            final GrooveNode forAll = ruleBuilder.contextNode(FORALL);
            GrooveNode message = ruleBuilder.deleteNode(TYPE_MESSAGE);
            ruleBuilder.deleteEdge(MESSAGES, processInstance, message);
            ruleBuilder.contextEdge(AT, message, forAll);
            ruleBuilder.contextEdge(UNEQUALS, message, deletedMessage);

            ruleBuilder.buildRule();

        });
    }

    private String createCatchMessageRuleName(MessageFlow messageFlow,
                                              IntermediateCatchEvent intermediateCatchEvent,
                                              BPMNCollaboration collaboration) {
        final int amountOfIncMessages = collaboration.getIncomingMessageFlows(intermediateCatchEvent).size();
        if (amountOfIncMessages <= 1) {
            return intermediateCatchEvent.getName();
        }
        return intermediateCatchEvent.getName() + "_" + messageFlow.getName();
    }

    private void createIntermediateCatchLinkEventRule(IntermediateCatchEvent intermediateCatchEvent,
                                                      AbstractProcess process,
                                                      String ruleName,
                                                      GrooveRuleBuilder ruleBuilder) {
        ruleBuilder.startRule(ruleName);

        GrooveNode processInstance = addTokensForOutgoingFlowsToRunningInstance(intermediateCatchEvent,
                                                                                process,
                                                                                ruleBuilder);
        if (intermediateCatchEvent.getIncomingFlows().findAny().isPresent()) {
            throw new RuntimeException("Link intermediate catch events are not allowed to have incoming sequence " +
                                               "flows!");
        }
        BPMNToGrooveTransformerHelper.deleteTokenWithPosition(ruleBuilder,
                                                              processInstance,
                                                              intermediateCatchEvent.getName());

        ruleBuilder.buildRule();
    }

    private GrooveNode addTokensForOutgoingFlowsToRunningInstance(FlowNode flowNode,
                                                                  AbstractProcess process,
                                                                  GrooveRuleBuilder ruleBuilder) {
        GrooveNode processInstance = BPMNToGrooveTransformerHelper.createContextRunningProcessInstance(process,
                                                                                                       ruleBuilder);
        BPMNToGrooveTransformerHelper.addOutgoingTokensForFlowNodeToProcessInstance(flowNode,
                                                                                    ruleBuilder,
                                                                                    processInstance);
        return processInstance;
    }

    void createIntermediateThrowEventRule(IntermediateThrowEvent intermediateThrowEvent, AbstractProcess process) {
        String ruleName = THROW + intermediateThrowEvent.getName();
        // We currently limit to one incoming token, but we could implement an implicit exclusive gateway.
        if (intermediateThrowEvent.getIncomingFlows().count() != 1) {
            throw new RuntimeException("Intermediate throw events should have exactly one incoming sequence flow!");
        }
        switch (intermediateThrowEvent.getType()) {
            case NONE:
                createIntermediateThrowNoneEventRule(intermediateThrowEvent, ruleName, ruleBuilder, process);
                break;
            case LINK:
                createIntermediateThrowLinkEventRule(intermediateThrowEvent, ruleName, ruleBuilder, process);
                break;
            case MESSAGE:
                createIntermediateThrowMessageEventRule(intermediateThrowEvent,
                                                        ruleName,
                                                        ruleBuilder,
                                                        collaboration,
                                                        process);
                break;
            case SIGNAL:
                createIntermediateThrowSignalEventRule(intermediateThrowEvent,
                                                       ruleName,
                                                       ruleBuilder,
                                                       collaboration,
                                                       process);
                break;
            default:
                throw new RuntimeException("Unexpected throw event type: " + intermediateThrowEvent.getType());
        }
    }

    private void createIntermediateThrowNoneEventRule(IntermediateThrowEvent intermediateThrowEvent,
                                                      String ruleName,
                                                      GrooveRuleBuilder ruleBuilder,
                                                      AbstractProcess process) {
        ruleBuilder.startRule(ruleName);
        GrooveNode processInstance = BPMNToGrooveTransformerHelper.createContextRunningProcessInstance(process,
                                                                                                       ruleBuilder);
        // We currently limit to one incoming token, but we could implement an implicit exclusive gateway.
        intermediateThrowEvent.getIncomingFlows().forEach(sequenceFlow -> BPMNToGrooveTransformerHelper.deleteTokenWithPosition(
                ruleBuilder,
                processInstance,
                sequenceFlow.getID()));
        BPMNToGrooveTransformerHelper.addOutgoingTokensForFlowNodeToProcessInstance(intermediateThrowEvent,
                                                                                    ruleBuilder,
                                                                                    processInstance);

        ruleBuilder.buildRule();
    }

    private void createIntermediateThrowSignalEventRule(IntermediateThrowEvent intermediateThrowEvent,
                                                        String ruleName,
                                                        GrooveRuleBuilder ruleBuilder,
                                                        BPMNCollaboration collaboration,
                                                        AbstractProcess process) {
        ruleBuilder.startRule(ruleName);
        GrooveNode processInstance = BPMNToGrooveTransformerHelper.createContextRunningProcessInstance(process,
                                                                                                       ruleBuilder);
        // We currently limit to one incoming token, but we could implement an implicit exclusive gateway.
        intermediateThrowEvent.getIncomingFlows().forEach(sequenceFlow -> BPMNToGrooveTransformerHelper.deleteTokenWithPosition(
                ruleBuilder,
                processInstance,
                sequenceFlow.getID()));
        BPMNToGrooveTransformerHelper.addOutgoingTokensForFlowNodeToProcessInstance(intermediateThrowEvent,
                                                                                    ruleBuilder,
                                                                                    processInstance);

        createSignalThrowRulePart(intermediateThrowEvent.getEventDefinition(), ruleBuilder, collaboration);
        ruleBuilder.buildRule();
    }

    private void createSignalThrowRulePart(EventDefinition eventDefinition,
                                           GrooveRuleBuilder ruleBuilder,
                                           BPMNCollaboration collaboration) {
        Set<Event> correspondingSignalCatchEvents = this.findAllCorrespondingSignalCatchEvents(collaboration,
                                                                                               eventDefinition);

        correspondingSignalCatchEvents.forEach(event -> {
            final Process processForEvent = findProcessForEvent(event, collaboration);
            if (event.isInstantiateFlowNode()) {
                // Create a new process instance.
                GrooveNode processInstance = createNewProcessInstance(ruleBuilder, processForEvent.getName());
                BPMNToGrooveTransformerHelper.addOutgoingTokensForFlowNodeToProcessInstance(event,
                                                                                            ruleBuilder,
                                                                                            processInstance);
            } else {
                // Send a signal only if the process instance exists.
                GrooveNode existsOptional = ruleBuilder.contextNode(EXISTS_OPTIONAL);
                GrooveNode processInstance = BPMNToGrooveTransformerHelper.createRunningExistsOptionalProcessInstance(
                        ruleBuilder,
                        processForEvent,
                        existsOptional);

                event.getIncomingFlows().forEach(inFlow -> {
                    String position;
                    if (inFlow.getSource().isExclusiveEventBasedGateway()) {
                        position = inFlow.getSource().getName();
                    } else {
                        position = inFlow.getID();
                    }
                    GrooveNode token = ruleBuilder.deleteNode(TYPE_TOKEN);
                    ruleBuilder.contextEdge(AT, token, existsOptional);
                    ruleBuilder.deleteEdge(TOKENS, processInstance, token);
                    ruleBuilder.deleteEdge(POSITION, token, ruleBuilder.contextNode(createStringNodeLabel(position)));

                    event.getOutgoingFlows().forEach(outFlow -> {
                        GrooveNode newToken = ruleBuilder.addNode(TYPE_TOKEN);
                        ruleBuilder.contextEdge(AT, newToken, existsOptional);
                        ruleBuilder.addEdge(TOKENS, processInstance, newToken);
                        ruleBuilder.addEdge(POSITION,
                                            newToken,
                                            ruleBuilder.contextNode(createStringNodeLabel(outFlow.getID())));
                    });
                });
            }
        });
    }

    private Process findProcessForEvent(Event event, BPMNCollaboration collaboration) {
        for (Process participant : collaboration.getParticipants()) {
            final boolean processFound = participant.getControlFlowNodes().anyMatch(flowNode -> flowNode.equals(event));
            if (processFound) {
                return participant;
            }
            Optional<Process> optionalProcess =
                    participant.getSubProcesses().filter(process -> process.getControlFlowNodes().anyMatch(
                            flowNode -> flowNode.equals(event))).findFirst();
            if (optionalProcess.isPresent()) {
                return optionalProcess.get();
            }
        }
        // Should not happen.
        throw new RuntimeException(String.format("No process for the event %s found!", event));
    }

    private Set<Event> findAllCorrespondingSignalCatchEvents(BPMNCollaboration collaboration,
                                                             EventDefinition eventDefinition) {
        Set<Event> signalCatchEvents = new LinkedHashSet<>();
        Set<Process> seenProcesses = new HashSet<>();
        collaboration.getParticipants().forEach(process -> signalCatchEvents.addAll(
                findAllCorrespondingSignalCatchEvents(process, eventDefinition, seenProcesses)));
        return signalCatchEvents;
    }

    private Set<Event> findAllCorrespondingSignalCatchEvents(Process process,
                                                             EventDefinition eventDefinition,
                                                             Set<Process> seenProcesses) {
        Set<Event> signalCatchEvents = new LinkedHashSet<>();
        if (seenProcesses.contains(process)) {
            return signalCatchEvents;
        }
        seenProcesses.add(process);

        String globalSignalName = eventDefinition.getGlobalSignalName();
        process.getControlFlowNodes().forEach(flowNode -> flowNode.accept(new FlowNodeVisitor() {
            @Override
            public void handle(Task task) {
                // not relevant
            }

            @Override
            public void handle(SendTask task) {
                // not relevant
            }

            @Override
            public void handle(ReceiveTask task) {
                // not relevant

            }

            @Override
            public void handle(CallActivity callActivity) {
                // not relevant
                signalCatchEvents.addAll(findAllCorrespondingSignalCatchEvents(callActivity.getSubProcessModel(),
                                                                               eventDefinition,
                                                                               seenProcesses));
            }

            @Override
            public void handle(ExclusiveGateway exclusiveGateway) {
                // not relevant
            }

            @Override
            public void handle(ParallelGateway parallelGateway) {
                // not relevant
            }

            @Override
            public void handle(InclusiveGateway inclusiveGateway) {
                // not relevant
            }

            @Override
            public void handle(StartEvent startEvent) {
                if (startEvent.getType() == StartEventType.SIGNAL && startEvent.getEventDefinition().getGlobalSignalName().equals(
                        globalSignalName)) {
                    signalCatchEvents.add(startEvent);
                }
            }

            @Override
            public void handle(IntermediateThrowEvent intermediateThrowEvent) {
                // not relevant
            }

            @Override
            public void handle(IntermediateCatchEvent intermediateCatchEvent) {
                if (intermediateCatchEvent.getType() == IntermediateCatchEventType.SIGNAL && intermediateCatchEvent.getEventDefinition().getGlobalSignalName().equals(
                        globalSignalName)) {
                    signalCatchEvents.add(intermediateCatchEvent);
                }
            }

            @Override
            public void handle(EndEvent endEvent) {
                // not relevant
            }

            @Override
            public void handle(EventBasedGateway eventBasedGateway) {
                // not relevant
            }
        }));
        return signalCatchEvents;
    }

    private void createIntermediateThrowMessageEventRule(IntermediateThrowEvent intermediateThrowEvent,
                                                         String ruleName,
                                                         GrooveRuleBuilder ruleBuilder,
                                                         BPMNCollaboration collaboration,
                                                         AbstractProcess process) {
        ruleBuilder.startRule(ruleName);
        GrooveNode processInstance = BPMNToGrooveTransformerHelper.createContextRunningProcessInstance(process,
                                                                                                       ruleBuilder);
        intermediateThrowEvent.getIncomingFlows().forEach(sequenceFlow -> BPMNToGrooveTransformerHelper.deleteTokenWithPosition(
                ruleBuilder,
                processInstance,
                sequenceFlow.getID()));
        BPMNToGrooveTransformerHelper.addOutgoingTokensForFlowNodeToProcessInstance(intermediateThrowEvent,
                                                                                    ruleBuilder,
                                                                                    processInstance);
        addOutgoingMessagesForFlowNode(intermediateThrowEvent);

        ruleBuilder.buildRule();
    }

    private void createIntermediateThrowLinkEventRule(IntermediateThrowEvent intermediateThrowEvent,
                                                      String ruleName,
                                                      GrooveRuleBuilder ruleBuilder,
                                                      AbstractProcess process) {
        ruleBuilder.startRule(ruleName);
        GrooveNode processInstance = BPMNToGrooveTransformerHelper.createContextRunningProcessInstance(process,
                                                                                                       ruleBuilder);
        intermediateThrowEvent.getIncomingFlows().forEach(sequenceFlow -> BPMNToGrooveTransformerHelper.deleteTokenWithPosition(
                ruleBuilder,
                processInstance,
                sequenceFlow.getID()));
        BPMNToGrooveTransformerHelper.addTokenWithPosition(ruleBuilder,
                                                           processInstance,
                                                           intermediateThrowEvent.getName());

        ruleBuilder.buildRule();
    }

    void createStartEventRule(StartEvent startEvent, Process process) {
        switch (startEvent.getType()) {
            case NONE:
                createNoneStartEventRule(startEvent, ruleBuilder, process);
                break;
            case MESSAGE:
                createMessageStartEventRule(startEvent, ruleBuilder, collaboration);
                break;
            case SIGNAL:
                // will be embedded in the throw rule.
                break;
        }
    }

    private void createNoneStartEventRule(StartEvent startEvent, GrooveRuleBuilder ruleBuilder, Process process) {
        ruleBuilder.startRule(startEvent.getName());
        GrooveNode processInstance = createProcessInstanceAndAddTokens(startEvent, ruleBuilder, process);
        BPMNToGrooveTransformerHelper.deleteTokenWithPosition(ruleBuilder,
                                                              processInstance,
                                                              getStartEventTokenName(process));
        ruleBuilder.buildRule();
    }

    private void createMessageStartEventRule(StartEvent startEvent,
                                             GrooveRuleBuilder ruleBuilder,
                                             BPMNCollaboration collaboration) {
        Set<MessageFlow> incomingMessageFlows = collaboration.getIncomingMessageFlows(startEvent);
        incomingMessageFlows.forEach(incomingMessageFlow -> {
            ruleBuilder.startRule(incomingMessageFlows.size() > 1 ? incomingMessageFlow.getName() :
                                          startEvent.getName());
            GrooveNode processInstance = deleteIncomingMessageAndCreateProcessInstance(incomingMessageFlow,
                                                                                       collaboration,
                                                                                       ruleBuilder);
            BPMNToGrooveTransformerHelper.addOutgoingTokensForFlowNodeToProcessInstance(startEvent,
                                                                                        ruleBuilder,
                                                                                        processInstance);
            ruleBuilder.buildRule();
        });
    }

    private GrooveNode createProcessInstanceAndAddTokens(StartEvent startEvent,
                                                         GrooveRuleBuilder ruleBuilder,
                                                         AbstractProcess process) {
        GrooveNode processInstance = BPMNToGrooveTransformerHelper.createContextRunningProcessInstance(process,
                                                                                                       ruleBuilder);
        BPMNToGrooveTransformerHelper.addOutgoingTokensForFlowNodeToProcessInstance(startEvent,
                                                                                    ruleBuilder,
                                                                                    processInstance);
        return processInstance;
    }

    void createInclusiveGatewayRules(AbstractProcess process, InclusiveGateway inclusiveGateway) {
        long incomingFlowCount = inclusiveGateway.getIncomingFlows().count();
        long outgoingFlowCount = inclusiveGateway.getOutgoingFlows().count();
        if (incomingFlowCount <= 1 && outgoingFlowCount > 1) {
            this.createBranchingInclusiveGatewayRules(process, ruleBuilder, inclusiveGateway);
            return;
        }
        if (incomingFlowCount > 1 && outgoingFlowCount == 1) {
            this.createMergingInclusiveGatewayRules(process, ruleBuilder, inclusiveGateway);
            return;
        }
        if (outgoingFlowCount == 0) {
            throw new RuntimeException(String.format("The inclusive gateway \"%s\" has no outgoing flows!",
                                                     inclusiveGateway.getName()));
        }
        throw new RuntimeException("Inclusive gateway should not have multiple incoming and outgoing flows.");
    }

    private void createMergingInclusiveGatewayRules(AbstractProcess process,
                                                    GrooveRuleBuilder ruleBuilder,
                                                    InclusiveGateway inclusiveGateway) {
        // Find the corresponding branching inclusive gateway
        Map<SequenceFlow, SequenceFlow> branchFlowsToInFlows = new HashMap<>();
        FlowNode branchGateway = this.findCorrespondingBranchGateway(inclusiveGateway, branchFlowsToInFlows);
        //noinspection OptionalGetWithoutIsPresent size 1 means this operation is save.
        SequenceFlow outFlow = inclusiveGateway.getOutgoingFlows().findFirst().get();
        int i = 1;
        for (Set<SequenceFlow> branchGatewayOutFlows :
                Sets.powerSet(branchGateway.getOutgoingFlows().collect(Collectors.toCollection(
                        LinkedHashSet::new)))) {
            if (branchGatewayOutFlows.size() >= 1) { // Empty set is also part of the power set.
                ruleBuilder.startRule(inclusiveGateway.getName() + "_" + i);
                GrooveNode processInstance = BPMNToGrooveTransformerHelper.createContextRunningProcessInstance(process,
                                                                                                               ruleBuilder);
                GrooveNode newToken = ruleBuilder.addNode(TYPE_TOKEN);
                ruleBuilder.addEdge(TOKENS, processInstance, newToken);
                String outFlowID = outFlow.getID();
                ruleBuilder.addEdge(POSITION, newToken, ruleBuilder.contextNode(createStringNodeLabel(outFlowID)));

                StringBuilder stringBuilder = new StringBuilder();
                branchGatewayOutFlows.forEach(branchOutFlow -> {
                    String branchOutFlowID = branchOutFlow.getID();
                    final SequenceFlow correspondingInFlow = branchFlowsToInFlows.get(branchOutFlow);
                    BPMNToGrooveTransformerHelper.deleteTokenWithPosition(ruleBuilder,
                                                                          processInstance,
                                                                          correspondingInFlow.getID());
                    stringBuilder.append(branchOutFlowID);
                });
                GrooveNode decision = ruleBuilder.deleteNode(TYPE_DECISION);
                ruleBuilder.deleteEdge(DECISIONS, processInstance, decision);
                ruleBuilder.deleteEdge(DECISION,
                                       decision,
                                       ruleBuilder.contextNode(createStringNodeLabel(stringBuilder.toString())));
                ruleBuilder.buildRule();
                i++;
            }
        }
    }

    private FlowNode findCorrespondingBranchGateway(InclusiveGateway inclusiveGateway,
                                                    Map<SequenceFlow, SequenceFlow> branchFlowsToInFlows) {
        final Set<FlowNode> iGateways = inclusiveGateway.getIncomingFlows().map(inFlow -> searchBranchingGateway(inFlow,
                                                                                                                 inFlow,
                                                                                                                 branchFlowsToInFlows)).collect(
                Collectors.toSet());
        return getSingleGatewayOrThrowException(iGateways);
    }

    private FlowNode searchBranchingGateway(SequenceFlow originalFlow,
                                            SequenceFlow currentFlow,
                                            Map<SequenceFlow, SequenceFlow> branchFlowsToInFlows) {
        final FlowNode source = currentFlow.getSource();
        if (source.isInclusiveGateway()) {
            branchFlowsToInFlows.put(currentFlow, originalFlow);
            return source;
        }
        final Set<FlowNode> iGateways = source.getIncomingFlows().map(inFlow -> searchBranchingGateway(originalFlow,
                                                                                                       inFlow,
                                                                                                       branchFlowsToInFlows)).collect(
                Collectors.toSet());
        return getSingleGatewayOrThrowException(iGateways);
    }

    private FlowNode getSingleGatewayOrThrowException(Set<FlowNode> iGateways) {
        if (iGateways.size() == 1) {
            return iGateways.iterator().next();
        } else {
            throw new RuntimeException("No matching branching inclusive Gateway found!");
        }
    }

    private void createBranchingInclusiveGatewayRules(AbstractProcess process,
                                                      GrooveRuleBuilder ruleBuilder,
                                                      InclusiveGateway inclusiveGateway) {
        Optional<SequenceFlow> incFlow = inclusiveGateway.getIncomingFlows().findFirst();
        int i = 1;
        for (Set<SequenceFlow> outFlows :
                Sets.powerSet(inclusiveGateway.getOutgoingFlows().collect(Collectors.toCollection(
                        LinkedHashSet::new)))) {
            if (outFlows.size() >= 1) { // Empty set is also part of the power set.
                ruleBuilder.startRule(inclusiveGateway.getName() + "_" + i);
                GrooveNode processInstance = BPMNToGrooveTransformerHelper.createContextRunningProcessInstance(process,
                                                                                                               ruleBuilder);
                String deleteTokenPosition;
                if (incFlow.isPresent()) {
                    deleteTokenPosition = incFlow.get().getID();
                } else {
                    deleteTokenPosition = inclusiveGateway.getName();
                }
                BPMNToGrooveTransformerHelper.deleteTokenWithPosition(ruleBuilder,
                                                                      processInstance,
                                                                      deleteTokenPosition);

                StringBuilder stringBuilder = new StringBuilder();
                outFlows.forEach(outFlow -> {
                    String outFlowID = outFlow.getID();
                    GrooveNode newToken = ruleBuilder.addNode(TYPE_TOKEN);
                    ruleBuilder.addEdge(TOKENS, processInstance, newToken);
                    ruleBuilder.addEdge(POSITION, newToken, ruleBuilder.contextNode(createStringNodeLabel(outFlowID)));
                    stringBuilder.append(outFlowID);
                });
                GrooveNode decision = ruleBuilder.addNode(TYPE_DECISION);
                ruleBuilder.addEdge(DECISIONS, processInstance, decision);
                ruleBuilder.addEdge(DECISION,
                                    decision,
                                    ruleBuilder.contextNode(createStringNodeLabel(stringBuilder.toString())));
                ruleBuilder.buildRule();
                i++;
            }
        }
    }

    void createEndEventRule(AbstractProcess process, EndEvent endEvent) {
        if (endEvent.getIncomingFlows().count() != 1) {
            throw new RuntimeException("End events should have exactly one incoming flow!");
        }
        //noinspection OptionalGetWithoutIsPresent size of the stream is 1
        final String incomingFlowId = endEvent.getIncomingFlows().findFirst().get().getID();
        ruleBuilder.startRule(endEvent.getName());

        GrooveNode processInstance = BPMNToGrooveTransformerHelper.createProcessInstanceWithName(process, ruleBuilder);

        GrooveNode token = ruleBuilder.deleteNode(TYPE_TOKEN);
        GrooveNode position = ruleBuilder.contextNode(createStringNodeLabel(incomingFlowId));
        ruleBuilder.deleteEdge(POSITION, token, position);
        ruleBuilder.deleteEdge(TOKENS, processInstance, token);

        switch (endEvent.getType()) {
            case NONE:
                GrooveNode running = ruleBuilder.contextNode(TYPE_RUNNING);
                ruleBuilder.contextEdge(STATE, processInstance, running);
                break;
            case TERMINATION:
                GrooveNode delete_running = ruleBuilder.deleteNode(TYPE_RUNNING);
                ruleBuilder.deleteEdge(STATE, processInstance, delete_running);

                GrooveNode terminated = ruleBuilder.addNode(TYPE_TERMINATED);
                ruleBuilder.addEdge(STATE, processInstance, terminated);

                // Terminate possible subprocesses with a nested rule.
                GrooveNode subProcess = ruleBuilder.contextNode(TYPE_PROCESS_SNAPSHOT);
                ruleBuilder.contextEdge(SUBPROCESS, processInstance, subProcess);
                GrooveNode subProcessRunning = ruleBuilder.deleteNode(TYPE_RUNNING);
                ruleBuilder.deleteEdge(STATE, subProcess, subProcessRunning);
                GrooveNode subProcessTerminated = ruleBuilder.addNode(TYPE_TERMINATED);
                ruleBuilder.addEdge(STATE, subProcess, subProcessTerminated);

                GrooveNode forAll = ruleBuilder.contextNode(FORALL);
                ruleBuilder.contextEdge(AT, subProcess, forAll);
                ruleBuilder.contextEdge(AT, subProcessRunning, forAll);
                ruleBuilder.contextEdge(AT, subProcessTerminated, forAll);
                // We could also delete all tokens in the current and all subprocess instances.

                break;
            case MESSAGE:
                addOutgoingMessagesForFlowNode(endEvent);
                break;
            case SIGNAL:
                createSignalThrowRulePart(endEvent.getEventDefinition(), ruleBuilder, collaboration);
                break;
        }

        ruleBuilder.buildRule();
    }

    void createParallelGatewayRule(AbstractProcess process, ParallelGateway parallelGateway) {
        ruleBuilder.startRule(parallelGateway.getName());
        GrooveNode processInstance = BPMNToGrooveTransformerHelper.createContextRunningProcessInstance(process,
                                                                                                       ruleBuilder);

        parallelGateway.getIncomingFlows().forEach(sequenceFlow -> BPMNToGrooveTransformerHelper.deleteTokenWithPosition(
                ruleBuilder,
                processInstance,
                sequenceFlow.getID()));
        // If no incoming flows we consume a token at the position of the gateway.
        if (parallelGateway.getIncomingFlows().findAny().isEmpty()) {
            BPMNToGrooveTransformerHelper.deleteTokenWithPosition(ruleBuilder,
                                                                  processInstance,
                                                                  parallelGateway.getName());
        }

        BPMNToGrooveTransformerHelper.addOutgoingTokensForFlowNodeToProcessInstance(parallelGateway,
                                                                                    ruleBuilder,
                                                                                    processInstance);

        ruleBuilder.buildRule();
    }
}
