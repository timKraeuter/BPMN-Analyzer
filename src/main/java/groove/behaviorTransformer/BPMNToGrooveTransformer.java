package groove.behaviorTransformer;

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
import groove.graph.GrooveGraph;
import groove.graph.GrooveGraphBuilder;
import groove.graph.GrooveNode;
import groove.graph.rule.GrooveGraphRule;
import groove.graph.rule.GrooveRuleBuilder;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BPMNToGrooveTransformer implements GrooveTransformer<BPMNCollaboration> {
    public static final String EXISTS_OPTIONAL = "existsx:";
    private static final String THROW = "Throw_";
    private static final String CATCH = "Catch_";
    private static final String START = "_start";
    private static final String FIXED_RULES_AND_TYPE_GRAPH_DIR = "/BPMNFixedRulesAndTypeGraph";
    // Node names
    private static final String TYPE_TOKEN = TYPE + "Token";
    private static final String TYPE_PROCESS_SNAPSHOT = TYPE + "ProcessSnapshot";
    private static final String TYPE_RUNNING = TYPE + "Running";
    private static final String TYPE_TERMINATED = TYPE + "Terminated";
    private static final String TYPE_DECISION = TYPE + "Decision";
    private static final String TYPE_MESSAGE = TYPE + "Message";
    // Edge names
    private static final String POSITION = "position";
    private static final String STATE = "state";
    private static final String TOKENS = "tokens";
    private static final String MESSAGES = "messages";
    private static final String DECISIONS = "decisions";
    private static final String DECISION = "decision";
    private static final String SUBPROCESS = "subprocess";
    private static final String NAME = "name";

    @Override
    public void generateAndWriteRulesFurther(BPMNCollaboration collaboration, boolean addPrefix, File targetFolder) {
        this.copyTypeGraphAndFixedRules(targetFolder);
    }

    private void copyTypeGraphAndFixedRules(File targetFolder) {
        //noinspection ConstantConditions must be present!. Otherwise, tests will also fail!
        File sourceDirectory = new File(this.getClass().getResource(FIXED_RULES_AND_TYPE_GRAPH_DIR).getFile());
        try {
            FileUtils.copyDirectory(sourceDirectory, targetFolder);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public GrooveGraph generateStartGraph(BPMNCollaboration collaboration, boolean addPrefix) {
        // TODO: Add prefix if needed!
        GrooveGraphBuilder startGraphBuilder = new GrooveGraphBuilder().setName(collaboration.getName());

        collaboration.getParticipants().stream().filter(process -> process.getStartEvent() != null).forEach(process -> {
            if (process.getStartEvent().getType() == StartEventType.NONE) {
                GrooveNode processInstance = new GrooveNode(TYPE_PROCESS_SNAPSHOT);
                GrooveNode processName = new GrooveNode(createStringNodeLabel(process.getName()));
                startGraphBuilder.addEdge(NAME, processInstance, processName);
                GrooveNode running = new GrooveNode(TYPE_RUNNING);
                startGraphBuilder.addEdge(STATE, processInstance, running);
                GrooveNode startToken = new GrooveNode(TYPE_TOKEN);
                GrooveNode tokenName = new GrooveNode(this.createStringNodeLabel(getStartEventTokenName(process)));
                startGraphBuilder.addEdge(POSITION, startToken, tokenName);
                startGraphBuilder.addEdge(TOKENS, processInstance, startToken);
            }
        });

        return startGraphBuilder.build();
    }

    private String getStartEventTokenName(Process process) {
        return process.getName() + "_" + process.getStartEvent().getName();
    }

    private void updateTokenPositionWhenRunning(Process process, String oldPosition, String newPosition, GrooveRuleBuilder ruleBuilder) {
        // Process instance has to be running
        GrooveNode processInstance = this.createContextRunningProcessInstance(process, ruleBuilder);

        // Update tokens
        updateTokenPositionForProcessInstance(oldPosition, newPosition, ruleBuilder, processInstance);
    }

    private void updateTokenPositionForProcessInstance(String oldPosition, String newPosition, GrooveRuleBuilder ruleBuilder, GrooveNode processInstance) {
        GrooveNode token = ruleBuilder.contextNode(TYPE_TOKEN);
        ruleBuilder.contextEdge(TOKENS, processInstance, token);
        GrooveNode oldTokenPosition = ruleBuilder.contextNode(this.createStringNodeLabel(oldPosition));
        ruleBuilder.deleteEdge(POSITION, token, oldTokenPosition);

        GrooveNode newTokenPosition = ruleBuilder.contextNode(this.createStringNodeLabel(newPosition));
        ruleBuilder.addEdge(POSITION, token, newTokenPosition);
    }

    private GrooveNode createContextRunningProcessInstance(Process process, GrooveRuleBuilder ruleBuilder) {
        GrooveNode processInstance = createProcessInstanceWithName(process, ruleBuilder);
        GrooveNode running = ruleBuilder.contextNode(TYPE_RUNNING);
        ruleBuilder.contextEdge(STATE, processInstance, running);
        return processInstance;
    }

    private GrooveNode createProcessInstanceWithName(Process process, GrooveRuleBuilder ruleBuilder) {
        GrooveNode processInstance = ruleBuilder.contextNode(TYPE_PROCESS_SNAPSHOT);
        ruleBuilder.contextEdge(NAME, processInstance, ruleBuilder.contextNode(createStringNodeLabel(process.getName())));
        return processInstance;
    }

    @Override
    public Stream<GrooveGraphRule> generateRules(BPMNCollaboration collaboration, boolean addPrefix) {
        GrooveRuleBuilder ruleBuilder = new GrooveRuleBuilder(collaboration, addPrefix);

        Set<Process> visitedProcessModels = Sets.newHashSet();
        collaboration.getParticipants().forEach(process -> {
            if (!visitedProcessModels.contains(process)) {
                visitedProcessModels.add(process);
                generateRules(collaboration, process, ruleBuilder, visitedProcessModels);
            }
        });


        return ruleBuilder.getRules();
    }

    private void generateRules(BPMNCollaboration collaboration, Process process, GrooveRuleBuilder ruleBuilder, Set<Process> visitedProcessModels) {
        process.getControlFlowNodes().forEach(node -> node.accept(new FlowNodeVisitor() {
            @Override
            public void handle(StartEvent startEvent) {
                createStartEventRule(startEvent, ruleBuilder, process, collaboration);
            }

            @Override
            public void handle(Task task) {
                createTaskRules(task);
            }

            @Override
            public void handle(SendTask sendTask) {
                createTaskRules(sendTask, (ruleBuilder) -> addOutgoingMessagesForFlowNode(sendTask, collaboration, ruleBuilder));
            }

            @Override
            public void handle(ReceiveTask receiveTask) {
                if (receiveTask.isInstantiate()) {
                    if (receiveTask.getIncomingFlows().findAny().isPresent()) {
                        throw new RuntimeException("Instantiate receive tasks should not have incoming sequence flows!");
                    }
                    createInstantiateReceiveTaskRule(receiveTask);
                    return;
                }
                // Create start task rules.
                receiveTask.getIncomingFlows().forEach(incomingFlow -> createReceiveTaskStartRule(receiveTask, incomingFlow));
                // End task rule is standard.
                createEndTaskRule(receiveTask, (noop) -> {
                });
            }

            private void createReceiveTaskStartRule(ReceiveTask receiveTask, SequenceFlow incomingFlow) {
                if (incomingFlow.getSource().isExclusiveEventBasedGateway()) {
                    createEventBasedGatewayStartTaskRule(receiveTask, incomingFlow);
                } else {
                    // Should only be able to start when one message is present.
                    collaboration.getIncomingMessageFlows(receiveTask).forEach(messageFlow -> {
                        // TODO: We need to make the rule name unique here, if there are multiple incoming messages!
                        // TODO: Delete all other possible messages!
                        createStartTaskRule(
                                receiveTask,
                                incomingFlow,
                                (ruleBuilder, processInstance) -> deleteMessageWithPosition(ruleBuilder, processInstance, messageFlow.getName()));

                    });
                }
            }

            private void createEventBasedGatewayStartTaskRule(ReceiveTask receiveTask, SequenceFlow incomingFlow) {
                collaboration.getIncomingMessageFlows(receiveTask).forEach(messageFlow -> {
                    final String incomingFlowId = incomingFlow.getID();
                    // TODO: We need to make the rule name unique here, if there are multiple incoming messages!
                    // TODO: Delete all other possible messages!
                    ruleBuilder.startRule(this.getTaskOrCallActivityRuleName(receiveTask, incomingFlowId) + START);
                    GrooveNode processInstance = BPMNToGrooveTransformer.this.createContextRunningProcessInstance(process, ruleBuilder);
                    deleteMessageWithPosition(ruleBuilder, processInstance, messageFlow.getName());
                    addTokenWithPosition(ruleBuilder, processInstance, receiveTask.getName());
                    // Consume the token at the event-based gateway.
                    deleteTokenWithPosition(ruleBuilder, processInstance, incomingFlow.getSource().getName());
                    ruleBuilder.buildRule();
                });
            }

            private void createInstantiateReceiveTaskRule(ReceiveTask receiveTask) {
                Set<MessageFlow> incomingMessageFlows = collaboration.getIncomingMessageFlows(receiveTask);
                // Each incoming message flow will instantiate the process.
                incomingMessageFlows.forEach(incomingMessageFlow -> {
                    ruleBuilder.startRule(incomingMessageFlows.size() > 1 ? incomingMessageFlow.getName() : receiveTask.getName() + START);
                    GrooveNode processInstance = deleteIncomingMessageAndCreateProcessInstance(incomingMessageFlow, collaboration, ruleBuilder);

                    GrooveNode activityToken = ruleBuilder.addNode(TYPE_TOKEN);
                    ruleBuilder.addEdge(POSITION, activityToken, ruleBuilder.contextNode(createStringNodeLabel(receiveTask.getName())));
                    ruleBuilder.addEdge(TOKENS, processInstance, activityToken);
                    ruleBuilder.buildRule();
                });
                // Create rules for the outgoing sequence flows.
                createTaskRules(receiveTask);
            }

            private void createTaskRules(AbstractTask task) {
                createTaskRules(task, (noop) -> {
                });
            }

            private void createTaskRules(AbstractTask task, Consumer<GrooveRuleBuilder> endTaskRuleAdditions) {
                // Rules for starting the task
                task.getIncomingFlows().forEach(incomingFlow -> createStartTaskRule(task, incomingFlow, (grooveRuleBuilder, grooveNode) -> {
                }));
                // Rule for ending the task
                createEndTaskRule(task, endTaskRuleAdditions);
            }

            private void createStartTaskRule(
                    AbstractTask task,
                    SequenceFlow incomingFlow,
                    BiConsumer<GrooveRuleBuilder, GrooveNode> startTaskRuleAdditions) {
                final String incomingFlowId = incomingFlow.getID();
                ruleBuilder.startRule(this.getTaskOrCallActivityRuleName(task, incomingFlowId) + START);
                GrooveNode processInstance = BPMNToGrooveTransformer.this.createContextRunningProcessInstance(process, ruleBuilder);
                deleteTokenWithPosition(ruleBuilder, processInstance, incomingFlowId);
                addTokenWithPosition(ruleBuilder, processInstance, task.getName());
                startTaskRuleAdditions.accept(ruleBuilder, processInstance);
                ruleBuilder.buildRule();
            }

            private void createEndTaskRule(AbstractTask task, Consumer<GrooveRuleBuilder> endTaskRuleAdditions) {
                ruleBuilder.startRule(task.getName() + "_end");
                GrooveNode processInstance = BPMNToGrooveTransformer.this.createContextRunningProcessInstance(process, ruleBuilder);
                deleteTokenWithPosition(ruleBuilder, processInstance, task.getName());

                task.getOutgoingFlows().forEach(outgoingFlow -> {
                    final String outgoingFlowID = outgoingFlow.getID();
                    addTokenWithPosition(ruleBuilder, processInstance, outgoingFlowID);
                });
                endTaskRuleAdditions.accept(ruleBuilder);
                ruleBuilder.buildRule();
            }

            @Override
            public void handle(CallActivity callActivity) {
                // Rules for instantiating a subprocess
                callActivity.getIncomingFlows().forEach(incomingFlow -> createSubProcessInstantiationRule(callActivity, incomingFlow));

                // Rule for terminating a subprocess
                createTerminateSubProcessRule(callActivity);

                // Generate rules for the sub process
                createRulesForExecutingTheSubProcess(callActivity);

                createBoundaryEventRules(callActivity);
            }

            private void createBoundaryEventRules(CallActivity callActivity) {
                callActivity.getBoundaryEvents().forEach(boundaryEvent -> {
                    ruleBuilder.startRule(boundaryEvent.getName());
                    GrooveNode processInstance = addTokensForOutgoingFlowsToRunningInstance(
                            boundaryEvent,
                            process,
                            ruleBuilder);

                    if (boundaryEvent.isCancelActivity()) {
                        // Terminate subprocess and delete all its tokens.
                        GrooveNode subProcess = ruleBuilder.deleteNode(TYPE_PROCESS_SNAPSHOT);
                        ruleBuilder.contextEdge(SUBPROCESS, processInstance, subProcess);
                        String subprocessName = callActivity.getSubProcessModel().getName();
                        ruleBuilder.contextEdge(NAME, subProcess, ruleBuilder.contextNode(createStringNodeLabel(subprocessName)));
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

            private void createRulesForExecutingTheSubProcess(CallActivity callActivity) {
                if (visitedProcessModels.contains(callActivity.getSubProcessModel())) {
                    return;
                }
                visitedProcessModels.add(callActivity.getSubProcessModel());
                BPMNToGrooveTransformer.this.generateRules(collaboration, callActivity.getSubProcessModel(), ruleBuilder, visitedProcessModels);
            }

            private void createTerminateSubProcessRule(CallActivity callActivity) {
                ruleBuilder.startRule(callActivity.getName() + "_end");

                GrooveNode processInstance = ruleBuilder.contextNode(TYPE_PROCESS_SNAPSHOT);
                GrooveNode running = ruleBuilder.contextNode(TYPE_RUNNING);
                ruleBuilder.contextEdge(STATE, processInstance, running);

                GrooveNode subProcessInstance = ruleBuilder.deleteNode(TYPE_PROCESS_SNAPSHOT);
                ruleBuilder.deleteEdge(NAME, subProcessInstance, ruleBuilder.contextNode(createStringNodeLabel(callActivity.getSubProcessModel().getName())));
                ruleBuilder.deleteEdge(SUBPROCESS, processInstance, subProcessInstance);
                GrooveNode terminated = ruleBuilder.deleteNode(TYPE_TERMINATED);
                ruleBuilder.deleteEdge(STATE, subProcessInstance, terminated);

                callActivity.getOutgoingFlows().forEach(outgoingFlow -> {
                    final String outgoingFlowID = outgoingFlow.getID();
                    addTokenWithPosition(ruleBuilder, processInstance, outgoingFlowID);
                });

                ruleBuilder.buildRule();
            }

            private void createSubProcessInstantiationRule(CallActivity callActivity, SequenceFlow incomingFlow) {
                final String incomingFlowId = incomingFlow.getID();
                ruleBuilder.startRule(this.getTaskOrCallActivityRuleName(callActivity, incomingFlowId));
                GrooveNode processInstance = BPMNToGrooveTransformer.this.createContextRunningProcessInstance(process, ruleBuilder);
                deleteTokenWithPosition(ruleBuilder, processInstance, incomingFlowId);

                GrooveNode subProcessInstance = ruleBuilder.addNode(TYPE_PROCESS_SNAPSHOT);
                ruleBuilder.contextEdge(NAME, subProcessInstance, ruleBuilder.contextNode(createStringNodeLabel(callActivity.getSubProcessModel().getName())));
                ruleBuilder.addEdge(SUBPROCESS, processInstance, subProcessInstance);
                GrooveNode running = ruleBuilder.addNode(TYPE_RUNNING);
                ruleBuilder.addEdge(STATE, subProcessInstance, running);
                if (callActivity.getSubProcessModel().getStartEvent() != null) {
                    // Subprocess has a unique start event which gets a token!
                    addTokenWithPosition(ruleBuilder, subProcessInstance, getStartEventTokenName(callActivity.getSubProcessModel()));
                } else {
                    // All activites and gateways without incoming sequence flows get a token.
                    callActivity.getSubProcessModel().getControlFlowNodes().filter(controlFlowNode -> controlFlowNode.isTask() || controlFlowNode.isGateway()).forEach(controlFlowNode -> addTokenWithPosition(ruleBuilder, subProcessInstance, controlFlowNode.getName()));
                }

                ruleBuilder.buildRule();
            }

            private String getTaskOrCallActivityRuleName(FlowNode taskOrCallActivity, String incomingFlowId) {
                if (taskOrCallActivity.getIncomingFlows().count() > 1) {
                    return taskOrCallActivity.getName() + "_" + incomingFlowId;
                }
                return taskOrCallActivity.getName();
            }

            @Override
            public void handle(ExclusiveGateway exclusiveGateway) {
                createExclusiveGatewayRules(exclusiveGateway);
            }

            private void createExclusiveGatewayRules(ExclusiveGateway exclusiveGateway) {
                exclusiveGateway.getIncomingFlows().forEach(incomingFlow -> {
                    final String incomingFlowId = incomingFlow.getID();
                    exclusiveGateway.getOutgoingFlows().forEach(outFlow -> createRuleExclusiveGatewayRule(exclusiveGateway, incomingFlowId, outFlow.getID()));
                });
                // No incoming flows means we expect a token sitting at the gateway.
                if (exclusiveGateway.getIncomingFlows().findAny().isEmpty()) {
                    exclusiveGateway.getOutgoingFlows().forEach(outFlow -> createRuleExclusiveGatewayRule(exclusiveGateway, exclusiveGateway.getName(), outFlow.getID()));
                }
            }

            private void createRuleExclusiveGatewayRule(ExclusiveGateway exclusiveGateway, String oldTokenPosition, String newTokenPosition) {
                ruleBuilder.startRule(this.getExclusiveGatewayName(exclusiveGateway, oldTokenPosition, newTokenPosition));
                BPMNToGrooveTransformer.this.updateTokenPositionWhenRunning(process, oldTokenPosition, newTokenPosition, ruleBuilder);
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

            @Override
            public void handle(ParallelGateway parallelGateway) {
                BPMNToGrooveTransformer.this.createParallelGatewayRule(process, ruleBuilder, parallelGateway);
            }

            @Override
            public void handle(InclusiveGateway inclusiveGateway) {
                BPMNToGrooveTransformer.this.createInclusiveGatewayRules(process, ruleBuilder, inclusiveGateway);
            }

            @Override
            public void handle(EndEvent endEvent) {
                BPMNToGrooveTransformer.this.createEndEventRule(process, ruleBuilder, endEvent, collaboration);
            }

            @Override
            public void handle(EventBasedGateway eventBasedGateway) {
                boolean implicitExclusiveGateway = eventBasedGateway.getIncomingFlows().count() > 1;
                eventBasedGateway.getIncomingFlows().forEach(inFlow -> {
                    String ruleName = implicitExclusiveGateway ? inFlow.getID() + "_" + eventBasedGateway.getName() : eventBasedGateway.getName();
                    ruleBuilder.startRule(ruleName);
                    updateTokenPositionWhenRunning(process, inFlow.getID(), eventBasedGateway.getName(), ruleBuilder);
                    ruleBuilder.buildRule();
                });
                // Effects the rules of the subsequent flow nodes!
                // Possible subsequent nodes: Message catch, Receive task, Signal catch, timer and condition.
                // We currently only implemented the first three.
            }

            @Override
            public void handle(IntermediateThrowEvent intermediateThrowEvent) {
                createIntermediateThrowEventRule(intermediateThrowEvent, ruleBuilder, process, collaboration);
            }

            @Override
            public void handle(IntermediateCatchEvent intermediateCatchEvent) {
                createIntermediateCatchEventRule(intermediateCatchEvent);
            }

            private void createIntermediateCatchEventRule(IntermediateCatchEvent intermediateCatchEvent) {
                String ruleName = CATCH + intermediateCatchEvent.getName();
                switch (intermediateCatchEvent.getType()) {
                    case LINK:
                        createIntermediateCatchLinkEventRule(intermediateCatchEvent, process, ruleName, ruleBuilder);
                        break;
                    case MESSAGE:
                        createIntermediateCatchMessageEventRule(
                                intermediateCatchEvent,
                                process,
                                ruleBuilder,
                                collaboration);
                        break;
                    case SIGNAL:
                        // no rule needed since it will be embedded in the throw rule.
                        break;
                    case TIMER:
                        createIntermediateCatchTimerEventRule(intermediateCatchEvent, process, ruleBuilder);
                        break;
                }
            }
        }));
    }

    private void createIntermediateCatchTimerEventRule(
            IntermediateCatchEvent intermediateCatchEvent,
            Process process,
            GrooveRuleBuilder ruleBuilder) {

        ruleBuilder.startRule(intermediateCatchEvent.getName());
        final GrooveNode processInstance = createContextRunningProcessInstance(process, ruleBuilder);

        if (intermediateCatchEvent.getIncomingFlows().count() != 1) {
            // current restriction, again we would need implicit exclusive gateway.
            throw new RuntimeException("Intermediate message catch events are only allowed to have one incoming sequence flow!");
        }
        intermediateCatchEvent.getIncomingFlows().forEach(
                inFlow -> deleteTokenWithPosition(ruleBuilder, processInstance, inFlow.getID()));

        // Add tokens on outgoing flows.
        addOutgoingTokensForFlowNodeToProcessInstance(intermediateCatchEvent, ruleBuilder, processInstance);

        ruleBuilder.buildRule();
    }

    private GrooveNode deleteIncomingMessageAndCreateProcessInstance(
            MessageFlow incomingMessageFlow,
            BPMNCollaboration collaboration,
            GrooveRuleBuilder ruleBuilder) {
        Process receiverProcess = collaboration.getMessageFlowReceiver(incomingMessageFlow);
        GrooveNode message = ruleBuilder.deleteNode(TYPE_MESSAGE);
        ruleBuilder.deleteEdge(POSITION, message, ruleBuilder.contextNode(createStringNodeLabel(incomingMessageFlow.getName())));

        return createNewProcessInstance(ruleBuilder, receiverProcess);
    }

    private GrooveNode createNewProcessInstance(GrooveRuleBuilder ruleBuilder, Process receiverProcess) {
        GrooveNode processInstance = ruleBuilder.addNode(TYPE_PROCESS_SNAPSHOT);
        ruleBuilder.addEdge(NAME, processInstance, ruleBuilder.contextNode(createStringNodeLabel(receiverProcess.getName())));
        ruleBuilder.addEdge(STATE, processInstance, ruleBuilder.addNode(TYPE_RUNNING));
        return processInstance;
    }

    private void createIntermediateCatchMessageEventRule(
            IntermediateCatchEvent intermediateCatchEvent,
            Process process,
            GrooveRuleBuilder ruleBuilder,
            BPMNCollaboration collaboration) {
        collaboration.getIncomingMessageFlows(intermediateCatchEvent).forEach(messageFlow -> {
            ruleBuilder.startRule(createCatchMessageRuleName(messageFlow, intermediateCatchEvent, collaboration));

            GrooveNode processInstance = addTokensForOutgoingFlowsToRunningInstance(intermediateCatchEvent, process, ruleBuilder);
            if (intermediateCatchEvent.getIncomingFlows().count() != 1) {
                // current restriction, again we would need implicit exclusive gateway.
                throw new RuntimeException("Intermediate message catch events are only allowed to have one incoming sequence flow!");
            }
            //noinspection OptionalGetWithoutIsPresent Size of the stream must be 1.
            SequenceFlow incFlow = intermediateCatchEvent.getIncomingFlows().findFirst().get();
            // Consume incoming token
            if (incFlow.getSource().isExclusiveEventBasedGateway()) {
                deleteTokenWithPosition(ruleBuilder, processInstance, incFlow.getSource().getName());
            } else {
                deleteTokenWithPosition(ruleBuilder, processInstance, incFlow.getID());
            }
            // Consume incoming message.
            final GrooveNode deletedMessage = deleteMessageWithPosition(ruleBuilder, processInstance, messageFlow.getName());
            // Delete all other incoming messages.
            final GrooveNode forAll = ruleBuilder.contextNode(FORALL);
            GrooveNode message = ruleBuilder.deleteNode(TYPE_MESSAGE);
            ruleBuilder.deleteEdge(MESSAGES, processInstance, message);
            ruleBuilder.contextEdge(AT, message, forAll);
            ruleBuilder.contextEdge(UNEQUALS, message, deletedMessage);

            ruleBuilder.buildRule();

        });
    }

    private String createCatchMessageRuleName(
            MessageFlow messageFlow,
            IntermediateCatchEvent intermediateCatchEvent,
            BPMNCollaboration collaboration) {
        final int amountOfIncMessages = collaboration.getIncomingMessageFlows(intermediateCatchEvent).size();
        if (amountOfIncMessages <= 1) {
            return intermediateCatchEvent.getName();
        }
        return intermediateCatchEvent.getName() + "_" + messageFlow.getName();
    }

    private void createIntermediateCatchLinkEventRule(IntermediateCatchEvent intermediateCatchEvent, Process process, String ruleName, GrooveRuleBuilder ruleBuilder) {
        ruleBuilder.startRule(ruleName);

        GrooveNode processInstance = addTokensForOutgoingFlowsToRunningInstance(intermediateCatchEvent, process, ruleBuilder);
        if (intermediateCatchEvent.getIncomingFlows().findAny().isPresent()) {
            throw new RuntimeException("Link intermediate catch events are not allowed to have incoming sequence flows!");
        }
        deleteTokenWithPosition(ruleBuilder, processInstance, intermediateCatchEvent.getName());

        ruleBuilder.buildRule();
    }

    private GrooveNode addTokensForOutgoingFlowsToRunningInstance(FlowNode flowNode, Process process, GrooveRuleBuilder ruleBuilder) {
        GrooveNode processInstance = BPMNToGrooveTransformer.this.createContextRunningProcessInstance(process, ruleBuilder);
        addOutgoingTokensForFlowNodeToProcessInstance(flowNode, ruleBuilder, processInstance);
        return processInstance;
    }

    private void addOutgoingTokensForFlowNodeToProcessInstance(
            FlowNode flowNode,
            GrooveRuleBuilder ruleBuilder,
            GrooveNode processInstance) {
        flowNode.getOutgoingFlows().forEach(
                sequenceFlow -> addTokenWithPosition(ruleBuilder, processInstance, sequenceFlow.getID()));
    }

    private void createIntermediateThrowEventRule(IntermediateThrowEvent intermediateThrowEvent, GrooveRuleBuilder ruleBuilder, Process process, BPMNCollaboration collaboration) {
        String ruleName = THROW + intermediateThrowEvent.getName();
        // We currently limit to one incoming token, but we could implement an implicit exclusive gateway.
        if (intermediateThrowEvent.getIncomingFlows().count() != 1) {
            throw new RuntimeException("Intermediate throw events should have exactly one incoming sequence flow!");
        }
        switch (intermediateThrowEvent.getType()) {
            case LINK:
                createIntermediateThrowLinkEventRule(intermediateThrowEvent, ruleName, ruleBuilder, process);
                break;
            case MESSAGE:
                createIntermediateThrowMessageEventRule(intermediateThrowEvent, ruleName, ruleBuilder, collaboration, process);
                break;
            case SIGNAL:
                createIntermediateThrowSignalEventRule(intermediateThrowEvent, ruleName, ruleBuilder, collaboration, process);
                break;
        }
    }

    private void createIntermediateThrowSignalEventRule(IntermediateThrowEvent intermediateThrowEvent, String ruleName, GrooveRuleBuilder ruleBuilder, BPMNCollaboration collaboration, Process process) {
        ruleBuilder.startRule(ruleName);
        GrooveNode processInstance = BPMNToGrooveTransformer.this.createContextRunningProcessInstance(process, ruleBuilder);
        intermediateThrowEvent.getIncomingFlows().forEach(sequenceFlow -> deleteTokenWithPosition(ruleBuilder, processInstance, sequenceFlow.getID()));
        addOutgoingTokensForFlowNodeToProcessInstance(intermediateThrowEvent, ruleBuilder, processInstance);

        createSignalThrowRulePart(intermediateThrowEvent.getEventDefinition(), ruleBuilder, collaboration);
        ruleBuilder.buildRule();
    }

    private void createSignalThrowRulePart(EventDefinition eventDefinition, GrooveRuleBuilder ruleBuilder, BPMNCollaboration collaboration) {
        Set<Event> correspondingSignalCatchEvents = this.findAllCorrespondingSignalCatchEvents(collaboration, eventDefinition);

        correspondingSignalCatchEvents.forEach(event -> {
            final Process processForEvent = findProcessForEvent(event, collaboration);
            if (event.isInstantiateFlowNode()) {
                // Create a new process instance.
                GrooveNode processInstance = createNewProcessInstance(ruleBuilder, processForEvent);
                addOutgoingTokensForFlowNodeToProcessInstance(event, ruleBuilder, processInstance);
            } else {
                // Send a signal only if the process instance exists.
                GrooveNode existsOptional = ruleBuilder.contextNode(EXISTS_OPTIONAL);
                GrooveNode processInstance = createRunningExistsOptionalProcessInstance(
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
                    ruleBuilder.deleteEdge(POSITION, token, ruleBuilder.contextNode(this.createStringNodeLabel(position)));

                    event.getOutgoingFlows().forEach(outFlow -> {
                        GrooveNode newToken = ruleBuilder.addNode(TYPE_TOKEN);
                        ruleBuilder.contextEdge(AT, newToken, existsOptional);
                        ruleBuilder.addEdge(TOKENS, processInstance, newToken);
                        ruleBuilder.addEdge(POSITION, newToken, ruleBuilder.contextNode(this.createStringNodeLabel(outFlow.getID())));
                    });
                });
            }
        });
    }

    private GrooveNode createRunningExistsOptionalProcessInstance(GrooveRuleBuilder ruleBuilder, Process processForEvent, GrooveNode existsOptional) {
        GrooveNode processInstance;
        processInstance = createProcessInstanceWithName(processForEvent, ruleBuilder);
        GrooveNode running = ruleBuilder.contextNode(TYPE_RUNNING);
        ruleBuilder.contextEdge(STATE, processInstance, running);
        ruleBuilder.contextEdge(AT, processInstance, existsOptional);
        ruleBuilder.contextEdge(AT, running, existsOptional);
        return processInstance;
    }

    private Process findProcessForEvent(Event event, BPMNCollaboration collaboration) {
        for (Process participant : collaboration.getParticipants()) {
            final boolean processFound = participant.getControlFlowNodes().anyMatch(flowNode -> flowNode.equals(event));
            if (processFound) {
                return participant;
            }
            Optional<Process> optionalProcess = participant.getSubProcesses().filter(process -> process.getControlFlowNodes().anyMatch(flowNode -> flowNode.equals(event))).findFirst();
            if (optionalProcess.isPresent()) {
                return optionalProcess.get();
            }
        }
        // Should not happen.
        throw new RuntimeException(String.format("No process for the event %s found!", event));
    }

    private Set<Event> findAllCorrespondingSignalCatchEvents(BPMNCollaboration collaboration, EventDefinition eventDefinition) {
        Set<Event> signalCatchEvents = new LinkedHashSet<>();
        Set<Process> seenProcesses = new HashSet<>();
        collaboration.getParticipants().forEach(
                process -> signalCatchEvents.addAll(findAllCorrespondingSignalCatchEvents(
                        process,
                        eventDefinition,
                        seenProcesses)));
        return signalCatchEvents;
    }

    private Set<Event> findAllCorrespondingSignalCatchEvents(Process process, EventDefinition eventDefinition, Set<Process> seenProcesses) {
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
                signalCatchEvents.addAll(findAllCorrespondingSignalCatchEvents(callActivity.getSubProcessModel(), eventDefinition, seenProcesses));
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
                if (startEvent.getType() == StartEventType.SIGNAL && startEvent.getEventDefinition().getGlobalSignalName().equals(globalSignalName)) {
                    signalCatchEvents.add(startEvent);
                }
            }

            @Override
            public void handle(IntermediateThrowEvent intermediateThrowEvent) {
                // not relevant
            }

            @Override
            public void handle(IntermediateCatchEvent intermediateCatchEvent) {
                if (intermediateCatchEvent.getType() == IntermediateCatchEventType.SIGNAL && intermediateCatchEvent.getEventDefinition().getGlobalSignalName().equals(globalSignalName)) {
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

    private void createIntermediateThrowMessageEventRule(IntermediateThrowEvent intermediateThrowEvent, String ruleName, GrooveRuleBuilder ruleBuilder, BPMNCollaboration collaboration, Process process) {
        ruleBuilder.startRule(ruleName);
        GrooveNode processInstance = BPMNToGrooveTransformer.this.createContextRunningProcessInstance(process, ruleBuilder);
        intermediateThrowEvent.getIncomingFlows().forEach(sequenceFlow -> deleteTokenWithPosition(ruleBuilder, processInstance, sequenceFlow.getID()));
        addOutgoingTokensForFlowNodeToProcessInstance(intermediateThrowEvent, ruleBuilder, processInstance);
        addOutgoingMessagesForFlowNode(intermediateThrowEvent, collaboration, ruleBuilder);

        ruleBuilder.buildRule();
    }

    private void createIntermediateThrowLinkEventRule(IntermediateThrowEvent intermediateThrowEvent, String ruleName, GrooveRuleBuilder ruleBuilder, Process process) {
        ruleBuilder.startRule(ruleName);
        GrooveNode processInstance = BPMNToGrooveTransformer.this.createContextRunningProcessInstance(process, ruleBuilder);
        intermediateThrowEvent.getIncomingFlows().forEach(sequenceFlow -> deleteTokenWithPosition(ruleBuilder, processInstance, sequenceFlow.getID()));
        addTokenWithPosition(ruleBuilder, processInstance, intermediateThrowEvent.getName());

        ruleBuilder.buildRule();
    }

    private void createStartEventRule(StartEvent startEvent, GrooveRuleBuilder ruleBuilder, Process process, BPMNCollaboration collaboration) {
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
        deleteTokenWithPosition(ruleBuilder, processInstance, getStartEventTokenName(process));
        ruleBuilder.buildRule();
    }

    private void createMessageStartEventRule(StartEvent startEvent, GrooveRuleBuilder ruleBuilder, BPMNCollaboration collaboration) {
        Set<MessageFlow> incomingMessageFlows = collaboration.getIncomingMessageFlows(startEvent);
        incomingMessageFlows.forEach(incomingMessageFlow -> {
            ruleBuilder.startRule(incomingMessageFlows.size() > 1 ? incomingMessageFlow.getName() : startEvent.getName());
            GrooveNode processInstance = deleteIncomingMessageAndCreateProcessInstance(incomingMessageFlow, collaboration, ruleBuilder);
            addOutgoingTokensForFlowNodeToProcessInstance(startEvent, ruleBuilder, processInstance);
            ruleBuilder.buildRule();
        });
    }

    private GrooveNode createProcessInstanceAndAddTokens(StartEvent startEvent, GrooveRuleBuilder ruleBuilder, Process process) {
        GrooveNode processInstance = createContextRunningProcessInstance(process, ruleBuilder);
        addOutgoingTokensForFlowNodeToProcessInstance(startEvent, ruleBuilder, processInstance);
        return processInstance;
    }

    private void addOutgoingMessagesForFlowNode(
            FlowNode producingMessageFlowNode,
            BPMNCollaboration collaboration,
            GrooveRuleBuilder ruleBuilder) {
        collaboration.getMessageFlows().stream().filter(messageFlow -> messageFlow.getSource() == producingMessageFlowNode).forEach(messageFlow -> {
            if (messageFlow.getTarget().isInstantiateFlowNode()) {
                // In case of instantiate receive tasks or start events with trigger and active process instance does not exist!
                GrooveNode newMessage = ruleBuilder.addNode(TYPE_MESSAGE);
                ruleBuilder.addEdge(POSITION, newMessage, ruleBuilder.contextNode(this.createStringNodeLabel(messageFlow.getName())));
            } else {
                Process messageFlowReceiver = collaboration.getMessageFlowReceiver(messageFlow);
                // If a process instance exists, send a message.
                GrooveNode existsOptional = ruleBuilder.contextNode(EXISTS_OPTIONAL);
                GrooveNode receiverInstance = createRunningExistsOptionalProcessInstance(
                        ruleBuilder,
                        messageFlowReceiver,
                        existsOptional);
                addExistentialMessageWithPosition(ruleBuilder, receiverInstance, messageFlow.getName(), existsOptional);
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
                    ruleBuilder.contextEdge(POSITION, token, ruleBuilder.contextNode(createStringNodeLabel(tokenPosition)));
                    ruleBuilder.contextEdge(AT, token, existsOptional);
                });
                // TODO: Afterwards remove deleting messages from terminate rule.
            }
        });
    }

    private void createInclusiveGatewayRules(Process process, GrooveRuleBuilder ruleBuilder, InclusiveGateway inclusiveGateway) {
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
            throw new RuntimeException(String.format("The inclusive gateway \"%s\" has no outgoing flows!", inclusiveGateway.getName()));
        }
        throw new RuntimeException("Inclusive gateway should not have multiple incoming and outgoing flows.");
    }

    private void createMergingInclusiveGatewayRules(Process process, GrooveRuleBuilder ruleBuilder, InclusiveGateway inclusiveGateway) {
        // Find the corresponding branching inclusive gateway
        Map<SequenceFlow, SequenceFlow> branchFlowsToInFlows = new HashMap<>();
        FlowNode branchGateway = this.findCorrespondingBranchGateway(inclusiveGateway, branchFlowsToInFlows);
        //noinspection OptionalGetWithoutIsPresent size 1 means this operation is save.
        SequenceFlow outFlow = inclusiveGateway.getOutgoingFlows().findFirst().get();
        int i = 1;
        for (Set<SequenceFlow> branchGatewayOutFlows : Sets.powerSet(branchGateway.getOutgoingFlows().collect(Collectors.toCollection(LinkedHashSet::new)))) {
            if (branchGatewayOutFlows.size() >= 1) { // Empty set is also part of the power set.
                ruleBuilder.startRule(inclusiveGateway.getName() + "_" + i);
                GrooveNode processInstance = this.createContextRunningProcessInstance(process, ruleBuilder);
                GrooveNode newToken = ruleBuilder.addNode(TYPE_TOKEN);
                ruleBuilder.addEdge(TOKENS, processInstance, newToken);
                String outFlowID = outFlow.getID();
                ruleBuilder.addEdge(POSITION, newToken, ruleBuilder.contextNode(this.createStringNodeLabel(outFlowID)));

                StringBuilder stringBuilder = new StringBuilder();
                branchGatewayOutFlows.forEach(branchOutFlow -> {
                    String branchOutFlowID = branchOutFlow.getID();
                    final SequenceFlow correspondingInFlow = branchFlowsToInFlows.get(branchOutFlow);
                    BPMNToGrooveTransformer.this.deleteTokenWithPosition(ruleBuilder, processInstance, correspondingInFlow.getID());
                    stringBuilder.append(branchOutFlowID);
                });
                GrooveNode decision = ruleBuilder.deleteNode(TYPE_DECISION);
                ruleBuilder.deleteEdge(DECISIONS, processInstance, decision);
                ruleBuilder.deleteEdge(DECISION, decision, ruleBuilder.contextNode(this.createStringNodeLabel(stringBuilder.toString())));
                ruleBuilder.buildRule();
                i++;
            }
        }
    }

    private FlowNode findCorrespondingBranchGateway(InclusiveGateway inclusiveGateway, Map<SequenceFlow, SequenceFlow> branchFlowsToInFlows) {
        final Set<FlowNode> iGateways = inclusiveGateway.getIncomingFlows().map(inFlow -> searchBranchingGateway(inFlow, inFlow, branchFlowsToInFlows)).collect(Collectors.toSet());
        return getSingleGatewayOrThrowException(iGateways);
    }

    private FlowNode searchBranchingGateway(SequenceFlow originalFlow, SequenceFlow currentFlow, Map<SequenceFlow, SequenceFlow> branchFlowsToInFlows) {
        final FlowNode source = currentFlow.getSource();
        if (source.isInclusiveGateway()) {
            branchFlowsToInFlows.put(currentFlow, originalFlow);
            return source;
        }
        final Set<FlowNode> iGateways = source.getIncomingFlows().map(inFlow -> searchBranchingGateway(originalFlow, inFlow, branchFlowsToInFlows)).collect(Collectors.toSet());
        return getSingleGatewayOrThrowException(iGateways);
    }

    private FlowNode getSingleGatewayOrThrowException(Set<FlowNode> iGateways) {
        if (iGateways.size() == 1) {
            return iGateways.iterator().next();
        } else {
            throw new RuntimeException("No matching branching inclusive Gateway found!");
        }
    }

    private void createBranchingInclusiveGatewayRules(Process process, GrooveRuleBuilder ruleBuilder, InclusiveGateway inclusiveGateway) {
        Optional<SequenceFlow> incFlow = inclusiveGateway.getIncomingFlows().findFirst();
        int i = 1;
        for (Set<SequenceFlow> outFlows : Sets.powerSet(inclusiveGateway.getOutgoingFlows().collect(Collectors.toCollection(LinkedHashSet::new)))) {
            if (outFlows.size() >= 1) { // Empty set is also part of the power set.
                ruleBuilder.startRule(inclusiveGateway.getName() + "_" + i);
                GrooveNode processInstance = this.createContextRunningProcessInstance(process, ruleBuilder);
                String deleteTokenPosition;
                if (incFlow.isPresent()) {
                    deleteTokenPosition = incFlow.get().getID();
                } else {
                    deleteTokenPosition = inclusiveGateway.getName();
                }
                deleteTokenWithPosition(ruleBuilder, processInstance, deleteTokenPosition);

                StringBuilder stringBuilder = new StringBuilder();
                outFlows.forEach(outFlow -> {
                    String outFlowID = outFlow.getID();
                    GrooveNode newToken = ruleBuilder.addNode(TYPE_TOKEN);
                    ruleBuilder.addEdge(TOKENS, processInstance, newToken);
                    ruleBuilder.addEdge(POSITION, newToken, ruleBuilder.contextNode(this.createStringNodeLabel(outFlowID)));
                    stringBuilder.append(outFlowID);
                });
                GrooveNode decision = ruleBuilder.addNode(TYPE_DECISION);
                ruleBuilder.addEdge(DECISIONS, processInstance, decision);
                ruleBuilder.addEdge(DECISION, decision, ruleBuilder.contextNode(this.createStringNodeLabel(stringBuilder.toString())));
                ruleBuilder.buildRule();
                i++;
            }
        }
    }

    private void createEndEventRule(Process process, GrooveRuleBuilder ruleBuilder, EndEvent endEvent, BPMNCollaboration collaboration) {
        if (endEvent.getIncomingFlows().count() != 1) {
            throw new RuntimeException("End events should have exactly one incoming flow!");
        }
        //noinspection OptionalGetWithoutIsPresent size of the stream is 1
        final String incomingFlowId = endEvent.getIncomingFlows().findFirst().get().getID();
        ruleBuilder.startRule(endEvent.getName());

        GrooveNode processInstance = createProcessInstanceWithName(process, ruleBuilder);

        GrooveNode token = ruleBuilder.deleteNode(TYPE_TOKEN);
        GrooveNode position = ruleBuilder.contextNode(this.createStringNodeLabel(incomingFlowId));
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
                addOutgoingMessagesForFlowNode(endEvent, collaboration, ruleBuilder);
                break;
            case SIGNAL:
                createSignalThrowRulePart(endEvent.getEventDefinition(), ruleBuilder, collaboration);
                break;
        }

        ruleBuilder.buildRule();
    }

    private void createParallelGatewayRule(Process process, GrooveRuleBuilder ruleBuilder, ParallelGateway parallelGateway) {
        ruleBuilder.startRule(parallelGateway.getName());
        GrooveNode processInstance = this.createContextRunningProcessInstance(process, ruleBuilder);

        parallelGateway.getIncomingFlows().forEach(sequenceFlow -> deleteTokenWithPosition(ruleBuilder, processInstance, sequenceFlow.getID()));
        // If no incoming flows we consume a token at the position of the gateway.
        if (parallelGateway.getIncomingFlows().findAny().isEmpty()) {
            deleteTokenWithPosition(ruleBuilder, processInstance, parallelGateway.getName());
        }

        addOutgoingTokensForFlowNodeToProcessInstance(parallelGateway, ruleBuilder, processInstance);

        ruleBuilder.buildRule();
    }

    private void deleteTokenWithPosition(GrooveRuleBuilder ruleBuilder, GrooveNode processInstance, String position) {
        GrooveNode token = ruleBuilder.deleteNode(TYPE_TOKEN);
        ruleBuilder.deleteEdge(TOKENS, processInstance, token);
        ruleBuilder.deleteEdge(POSITION, token, ruleBuilder.contextNode(this.createStringNodeLabel(position)));
    }

    private void addTokenWithPosition(GrooveRuleBuilder ruleBuilder, GrooveNode processInstance, String position) {
        GrooveNode newToken = ruleBuilder.addNode(TYPE_TOKEN);
        ruleBuilder.addEdge(TOKENS, processInstance, newToken);
        ruleBuilder.addEdge(POSITION, newToken, ruleBuilder.contextNode(this.createStringNodeLabel(position)));
    }

    private GrooveNode deleteMessageWithPosition(GrooveRuleBuilder ruleBuilder, GrooveNode processInstance, String position) {
        GrooveNode message = ruleBuilder.deleteNode(TYPE_MESSAGE);
        ruleBuilder.deleteEdge(MESSAGES, processInstance, message);
        ruleBuilder.deleteEdge(POSITION, message, ruleBuilder.contextNode(this.createStringNodeLabel(position)));
        return message;
    }

    private void addExistentialMessageWithPosition(
            GrooveRuleBuilder ruleBuilder,
            GrooveNode processInstance,
            String position,
            GrooveNode existsOptional) {
        GrooveNode newMessage = ruleBuilder.addNode(TYPE_MESSAGE);
        ruleBuilder.contextEdge(AT, newMessage, existsOptional);
        ruleBuilder.addEdge(MESSAGES, processInstance, newMessage);
        ruleBuilder.addEdge(POSITION, newMessage, ruleBuilder.contextNode(this.createStringNodeLabel(position)));
    }

    @Override
    public boolean isLayoutActivated() {
        return true; // TODO: implement layout as parameter!
    }
}
