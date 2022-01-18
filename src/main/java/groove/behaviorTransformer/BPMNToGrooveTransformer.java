package groove.behaviorTransformer;

import behavior.bpmn.BPMNCollaboration;
import behavior.bpmn.FlowNode;
import behavior.bpmn.Process;
import behavior.bpmn.SequenceFlow;
import behavior.bpmn.activities.CallActivity;
import behavior.bpmn.activities.Task;
import behavior.bpmn.auxiliary.FlowNodeVisitor;
import behavior.bpmn.events.EndEvent;
import behavior.bpmn.events.IntermediateCatchEvent;
import behavior.bpmn.events.IntermediateThrowEvent;
import behavior.bpmn.events.StartEvent;
import behavior.bpmn.gateways.ExclusiveGateway;
import behavior.bpmn.gateways.InclusiveGateway;
import behavior.bpmn.gateways.ParallelGateway;
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
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BPMNToGrooveTransformer implements GrooveTransformer<BPMNCollaboration> {
    public static final String THROW = "Throw_";
    public static final String CATCH = "Catch_";
    private static final String FIXED_RULES_AND_TYPE_GRAPH_DIR = "/BPMNFixedRulesAndTypeGraph";
    // Node names
    private static final String TYPE_TOKEN = TYPE + "Token";
    private static final String TYPE_PROCESS_INSTANCE = TYPE + "ProcessInstance";
    private static final String TYPE_RUNNING = TYPE + "Running";
    private static final String TYPE_TERMINATED = TYPE + "Terminated";
    private static final String TYPE_DECISION = TYPE + "Decision";
    // Edge names
    private static final String POSITION = "position";
    private static final String STATE = "state";
    private static final String TOKENS = "tokens";
    private static final String DECISIONS = "decisions";
    private static final String DECISION = "decision";
    private static final String SUBPROCESS = "subprocess";

    @Override
    public void generateAndWriteRulesFurther(BPMNCollaboration collaboration, boolean addPrefix, File targetFolder) {
        this.copyTypeGraphAndFixedRules(targetFolder);
    }

    private void copyTypeGraphAndFixedRules(File targetFolder) {
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
        GrooveNode processInstance = new GrooveNode(TYPE_PROCESS_INSTANCE);
        GrooveNode running = new GrooveNode(TYPE_RUNNING);
        startGraphBuilder.addEdge(STATE, processInstance, running);

        collaboration.getParticipants().forEach(process -> {
            GrooveNode startToken = new GrooveNode(TYPE_TOKEN);
            GrooveNode tokenName = new GrooveNode(this.createStringNodeLabel(getStartEventTokenName(process)));
            startGraphBuilder.addEdge(POSITION, startToken, tokenName);
            startGraphBuilder.addEdge(TOKENS, processInstance, startToken);
        });

        return startGraphBuilder.build();
    }

    private String getStartEventTokenName(Process process) {
        return process.getName() + "_" + process.getStartEvent().getName();
    }

    private void updateTokenPositionWhenRunning(String oldPosition, String newPosition, GrooveRuleBuilder ruleBuilder) {
        // Process instance has to be running
        GrooveNode processInstance = this.createContextRunningProcessInstance(ruleBuilder);

        // Update tokens
        GrooveNode token = ruleBuilder.contextNode(TYPE_TOKEN);
        ruleBuilder.contextEdge(TOKENS, processInstance, token);
        GrooveNode oldTokenPosition = ruleBuilder.contextNode(this.createStringNodeLabel(oldPosition));
        ruleBuilder.deleteEdge(POSITION, token, oldTokenPosition);

        GrooveNode newTokenPosition = ruleBuilder.contextNode(this.createStringNodeLabel(newPosition));
        ruleBuilder.addEdge(POSITION, token, newTokenPosition);
    }

    private GrooveNode createContextRunningProcessInstance(GrooveRuleBuilder ruleBuilder) {
        GrooveNode processInstance = ruleBuilder.contextNode(TYPE_PROCESS_INSTANCE);
        GrooveNode running = ruleBuilder.contextNode(TYPE_RUNNING);
        ruleBuilder.contextEdge(STATE, processInstance, running);
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

    private void generateRules(
            BPMNCollaboration collaboration,
            Process process,
            GrooveRuleBuilder ruleBuilder,
            Set<Process> visitedProcessModels) {
        process.getControlFlowNodes().forEach(node -> node.accept(new FlowNodeVisitor() {
            @Override
            public void handle(StartEvent startEvent) {
                if (startEvent.getOutgoingFlows().count() != 1) {
                    throw new RuntimeException("Start events should have exactly one outgoing flow!");
                }
                final String outgoingFlowID = startEvent.getOutgoingFlows().findFirst().get().getID();
                ruleBuilder.startRule(startEvent.getName());
                BPMNToGrooveTransformer.this.updateTokenPositionWhenRunning(
                        getStartEventTokenName(process),
                        outgoingFlowID,
                        ruleBuilder);
                ruleBuilder.buildRule();
            }

            @Override
            public void handle(Task task) {
                // Rules for starting the activity
                task.getIncomingFlows().forEach(incomingFlow -> {
                    final String incomingFlowId = incomingFlow.getID();
                    ruleBuilder.startRule(this.getTaskOrCallActivityRuleName(task, incomingFlowId) + "_start");
                    GrooveNode processInstance = BPMNToGrooveTransformer.this.createContextRunningProcessInstance(ruleBuilder);
                    deleteTokenWithPosition(ruleBuilder, processInstance, incomingFlowId);
                    addTokenWithPosition(ruleBuilder, processInstance, task.getName());
                    ruleBuilder.buildRule();
                });
                // Rule for ending the activity
                ruleBuilder.startRule(task.getName() + "_end");
                GrooveNode processInstance = BPMNToGrooveTransformer.this.createContextRunningProcessInstance(ruleBuilder);
                deleteTokenWithPosition(ruleBuilder, processInstance, task.getName());

                task.getOutgoingFlows().forEach(outgoingFlow -> {
                    final String outgoingFlowID = outgoingFlow.getID();
                    addTokenWithPosition(ruleBuilder, processInstance, outgoingFlowID);
                });
                ruleBuilder.buildRule();
            }

            @Override
            public void handle(CallActivity callActivity) {
                // Rules for instantiating a subprocess
                callActivity.getIncomingFlows().forEach(
                        incomingFlow -> createSubProcessInstantiationRule(callActivity, incomingFlow));

                // Rule for terminating a subprocess
                createTerminateSubProcessRule(callActivity);

                // Generate rules for the sub process
                createRulesForExecutingTheSubProcess(callActivity);
            }

            private void createRulesForExecutingTheSubProcess(CallActivity callActivity) {
                if (visitedProcessModels.contains(callActivity.getSubProcessModel())) {
                    return;
                }
                visitedProcessModels.add(callActivity.getSubProcessModel());
                BPMNToGrooveTransformer.this.generateRules(
                        collaboration,
                        callActivity.getSubProcessModel(),
                        ruleBuilder,
                        visitedProcessModels);
            }

            private void createTerminateSubProcessRule(CallActivity callActivity) {
                ruleBuilder.startRule(callActivity.getName() + "_end");

                GrooveNode processInstance = ruleBuilder.contextNode(TYPE_PROCESS_INSTANCE);
                GrooveNode running = ruleBuilder.contextNode(TYPE_RUNNING);
                ruleBuilder.contextEdge(STATE, processInstance, running);

                GrooveNode subProcessInstance = ruleBuilder.deleteNode(TYPE_PROCESS_INSTANCE);
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
                GrooveNode processInstance = BPMNToGrooveTransformer.this.createContextRunningProcessInstance(ruleBuilder);
                deleteTokenWithPosition(ruleBuilder, processInstance, incomingFlowId);

                GrooveNode subProcessInstance = ruleBuilder.addNode(TYPE_PROCESS_INSTANCE);
                ruleBuilder.addEdge(SUBPROCESS, processInstance, subProcessInstance);
                GrooveNode running = ruleBuilder.addNode(TYPE_RUNNING);
                ruleBuilder.addEdge(STATE, subProcessInstance, running);
                if (callActivity.getSubProcessModel().getStartEvent() != null) {
                    // Subprocess has a unique start event which gets a token!
                    addTokenWithPosition(ruleBuilder, subProcessInstance, getStartEventTokenName(callActivity.getSubProcessModel()));
                } else {
                    // All activites and gateways without incoming sequence flows get a token.
                    callActivity.getSubProcessModel().getControlFlowNodes()
                                .filter(controlFlowNode -> controlFlowNode.isTask() || controlFlowNode.isGateway())
                                .forEach(controlFlowNode -> addTokenWithPosition(ruleBuilder, subProcessInstance, controlFlowNode.getName()));
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
                    exclusiveGateway.getOutgoingFlows().forEach(
                            outFlow -> createRuleExclusiveGatewayRule(
                                    exclusiveGateway,
                                    incomingFlowId,
                                    outFlow.getID()));
                });
                // No incoming flows means we expect a token sitting at the gateway.
                if (exclusiveGateway.getIncomingFlows().findAny().isEmpty()) {
                    exclusiveGateway.getOutgoingFlows().forEach(
                            outFlow -> createRuleExclusiveGatewayRule(
                                    exclusiveGateway,
                                    exclusiveGateway.getName(),
                                    outFlow.getID()
                            ));
                }
            }

            private void createRuleExclusiveGatewayRule(
                    ExclusiveGateway exclusiveGateway,
                    String oldTokenPosition,
                    String newTokenPosition) {
                ruleBuilder.startRule(this.getExclusiveGatewayName(exclusiveGateway, oldTokenPosition, newTokenPosition));
                BPMNToGrooveTransformer.this.updateTokenPositionWhenRunning(oldTokenPosition, newTokenPosition, ruleBuilder);
                ruleBuilder.buildRule();
            }

            private String getExclusiveGatewayName(ExclusiveGateway exclusiveGateway, String incomingFlowId, String outFlowID) {
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
                BPMNToGrooveTransformer.this.createParallelGatewayRule(ruleBuilder, parallelGateway);
            }

            @Override
            public void handle(InclusiveGateway inclusiveGateway) {
                BPMNToGrooveTransformer.this.createInclusiveGatewayRules(ruleBuilder, inclusiveGateway);
            }

            @Override
            public void handle(EndEvent endEvent) {
                BPMNToGrooveTransformer.this.createEndEventRule(ruleBuilder, endEvent);
            }

            @Override
            public void handle(IntermediateThrowEvent intermediateThrowEvent) {
                switch (intermediateThrowEvent.getType()) {
                    case LINK:
                        ruleBuilder.startRule(THROW + intermediateThrowEvent.getName());
                        if (intermediateThrowEvent.getIncomingFlows().count() != 1) {
                            throw new RuntimeException("A throw link event should have exactly one incoming sequence flow!");
                        }
                        final String incomingFlowId = intermediateThrowEvent.getIncomingFlows().findFirst().get().getID();
                        BPMNToGrooveTransformer.this.updateTokenPositionWhenRunning(incomingFlowId, intermediateThrowEvent.getName(), ruleBuilder);
                        break;
                    case MESSAGE:
                        break;
                }
                ruleBuilder.buildRule();
            }

            @Override
            public void handle(IntermediateCatchEvent intermediateCatchEvent) {
                switch (intermediateCatchEvent.getType()) {
                    case LINK:
                        ruleBuilder.startRule(CATCH + intermediateCatchEvent.getName());
                        if (intermediateCatchEvent.getOutgoingFlows().count() != 1) {
                            throw new RuntimeException("A catch link event should have exactly one outgoing sequence flow!");
                        }
                        final String outgoingFlowId = intermediateCatchEvent.getOutgoingFlows().findFirst().get().getID();
                        BPMNToGrooveTransformer.this.updateTokenPositionWhenRunning(intermediateCatchEvent.getName(), outgoingFlowId, ruleBuilder);
                        break;
                    case MESSAGE:
                        break;
                }
                ruleBuilder.buildRule();
            }
        }));
    }

    private void createInclusiveGatewayRules(GrooveRuleBuilder ruleBuilder, InclusiveGateway inclusiveGateway) {
        long incomingFlowCount = inclusiveGateway.getIncomingFlows().count();
        long outgoingFlowCount = inclusiveGateway.getOutgoingFlows().count();
        if (incomingFlowCount <= 1 && outgoingFlowCount > 1) {
            this.createBranchingInclusiveGatewayRules(ruleBuilder, inclusiveGateway);
            return;
        }
        if (incomingFlowCount > 1 && outgoingFlowCount == 1) {
            this.createMergingInclusiveGatewayRules(ruleBuilder, inclusiveGateway);
            return;
        }
        if (outgoingFlowCount == 0) {
            throw new RuntimeException(String.format("The inclusive gateway \"%s\" has no outgoing flows!", inclusiveGateway.getName()));
        }
        throw new RuntimeException("Inclusive gateway should not have multiple incoming and outgoing flows.");
    }

    private void createMergingInclusiveGatewayRules(GrooveRuleBuilder ruleBuilder, InclusiveGateway inclusiveGateway) {
        // Find the corresponding branching inclusive gateway
        Map<SequenceFlow, SequenceFlow> branchFlowsToInFlows = new HashMap<>();
        FlowNode branchGateway = this.findCorrespondingBranchGateway(inclusiveGateway, branchFlowsToInFlows);
        SequenceFlow outFlow = inclusiveGateway.getOutgoingFlows().findFirst().get(); // size 1 means this operation is save.
        int i = 1;
        for (Set<SequenceFlow> branchGatewayOutFlows : Sets.powerSet(branchGateway.getOutgoingFlows().collect(Collectors.toCollection(LinkedHashSet::new)))) {
            if (branchGatewayOutFlows.size() >= 1) { // Empty set is also part of the power set.
                ruleBuilder.startRule(inclusiveGateway.getName() + "_" + i);
                GrooveNode processInstance = this.createContextRunningProcessInstance(ruleBuilder);
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

    private void createBranchingInclusiveGatewayRules(GrooveRuleBuilder ruleBuilder, InclusiveGateway inclusiveGateway) {
        Optional<SequenceFlow> incFlow = inclusiveGateway.getIncomingFlows().findFirst();
        int i = 1;
        for (Set<SequenceFlow> outFlows : Sets.powerSet(inclusiveGateway.getOutgoingFlows().collect(Collectors.toCollection(LinkedHashSet::new)))) {
            if (outFlows.size() >= 1) { // Empty set is also part of the power set.
                ruleBuilder.startRule(inclusiveGateway.getName() + "_" + i);
                GrooveNode processInstance = this.createContextRunningProcessInstance(ruleBuilder);
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

    private void createEndEventRule(GrooveRuleBuilder ruleBuilder, EndEvent endEvent) {
        if (endEvent.getIncomingFlows().count() != 1) {
            throw new RuntimeException("End events should have exactly one incoming flow!");
        }
        final String incomingFlowId = endEvent.getIncomingFlows().findFirst().get().getID();
        ruleBuilder.startRule(endEvent.getName());

        GrooveNode processInstance = ruleBuilder.contextNode(TYPE_PROCESS_INSTANCE);

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
                break;
        }

        ruleBuilder.buildRule();
    }

    private void createParallelGatewayRule(GrooveRuleBuilder ruleBuilder, ParallelGateway parallelGateway) {
        ruleBuilder.startRule(parallelGateway.getName());
        GrooveNode processInstance = this.createContextRunningProcessInstance(ruleBuilder);

        parallelGateway.getIncomingFlows().forEach(sequenceFlow -> deleteTokenWithPosition(ruleBuilder, processInstance, sequenceFlow.getID()));
        // If no incoming flows we consume a token at the position of the gateway.
        if (parallelGateway.getIncomingFlows().findAny().isEmpty()) {
            deleteTokenWithPosition(ruleBuilder, processInstance, parallelGateway.getName());
        }

        parallelGateway.getOutgoingFlows().forEach(sequenceFlow -> addTokenWithPosition(ruleBuilder, processInstance, sequenceFlow.getID()));

        ruleBuilder.buildRule();
    }

    private void deleteTokenWithPosition(GrooveRuleBuilder ruleBuilder, GrooveNode processInstance, String position) {
        GrooveNode forkedToken = ruleBuilder.deleteNode(TYPE_TOKEN);
        ruleBuilder.deleteEdge(TOKENS, processInstance, forkedToken);
        ruleBuilder.deleteEdge(POSITION, forkedToken, ruleBuilder.contextNode(this.createStringNodeLabel(position)));
    }

    private void addTokenWithPosition(GrooveRuleBuilder ruleBuilder, GrooveNode processInstance, String position) {
        GrooveNode forkedToken = ruleBuilder.addNode(TYPE_TOKEN);
        ruleBuilder.addEdge(TOKENS, processInstance, forkedToken);
        ruleBuilder.addEdge(POSITION, forkedToken, ruleBuilder.contextNode(this.createStringNodeLabel(position)));
    }

    @Override
    public boolean isLayoutActivated() {
        return true; // TODO: implement layout as parameter!
    }
}
