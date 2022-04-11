package groove.behaviorTransformer.bpmn;

import behavior.bpmn.Process;
import behavior.bpmn.*;
import behavior.bpmn.activities.CallActivity;
import behavior.bpmn.activities.tasks.AbstractTask;
import behavior.bpmn.events.StartEvent;
import behavior.bpmn.gateways.*;
import com.google.common.collect.Sets;
import groove.behaviorTransformer.bpmn.generators.BPMNEventRuleGenerator;
import groove.behaviorTransformer.bpmn.generators.BPMNEventRuleGeneratorImpl;
import groove.behaviorTransformer.bpmn.generators.BPMNTaskRuleGenerator;
import groove.behaviorTransformer.bpmn.generators.BPMNTaskRuleGeneratorImpl;
import groove.graph.GrooveNode;
import groove.graph.rule.GrooveGraphRule;
import groove.graph.rule.GrooveRuleBuilder;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static groove.behaviorTransformer.GrooveTransformer.AT;
import static groove.behaviorTransformer.GrooveTransformer.FORALL;
import static groove.behaviorTransformer.GrooveTransformerHelper.createStringNodeLabel;
import static groove.behaviorTransformer.bpmn.BPMNToGrooveTransformerConstants.*;

public class BPMNRuleGenerator {
    private final GrooveRuleBuilder ruleBuilder;
    private final BPMNCollaboration collaboration;
    private final Set<Process> visitedProcessModels;

    // Subgenerators
    private final BPMNTaskRuleGenerator taskRuleGenerator;
    private final BPMNEventRuleGenerator eventRuleGenerator;

    public BPMNRuleGenerator(GrooveRuleBuilder ruleBuilder, BPMNCollaboration collaboration) {
        this.ruleBuilder = ruleBuilder;
        this.collaboration = collaboration;
        visitedProcessModels = Sets.newHashSet();

        taskRuleGenerator = new BPMNTaskRuleGeneratorImpl(collaboration, ruleBuilder);
        eventRuleGenerator = new BPMNEventRuleGeneratorImpl(collaboration, ruleBuilder);

        generateRules();
    }

    public Stream<GrooveGraphRule> getRules() {
        return ruleBuilder.getRules();
    }

    public BPMNTaskRuleGenerator getTaskRuleGenerator() {
        return taskRuleGenerator;
    }

    public BPMNEventRuleGenerator getEventRuleGenerator() {
        return eventRuleGenerator;
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

    void createTaskRulesForProcess(AbstractProcess process, AbstractTask task) {
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
            GrooveNode processInstance = BPMNToGrooveTransformerHelper.addTokensForOutgoingFlowsToRunningInstance(boundaryEvent,
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
            GrooveNode eventSubProcessInstance = BPMNToGrooveTransformerHelper.createNewProcessInstance(ruleBuilder, eventSubprocess.getName());
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
