package groove.behaviorTransformer;

import behavior.bpmn.*;
import behavior.bpmn.auxiliary.ControlFlowNodeVisitor;
import behavior.bpmn.events.EndEvent;
import behavior.bpmn.events.LinkEvent;
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
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BPMNToGrooveTransformer implements GrooveTransformer<BPMNProcessModel> {
    public static final String AT = "at";
    public static final String FORALL = "forall:";
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
    public void generateAndWriteRulesFurther(BPMNProcessModel model, boolean addPrefix, File targetFolder) {
        this.copyTypeGraph(targetFolder);
    }

    private void copyTypeGraph(File targetFolder) {
        File sourceDirectory = new File(this.getClass().getResource(FIXED_RULES_AND_TYPE_GRAPH_DIR).getFile());
        try {
            FileUtils.copyDirectory(sourceDirectory, targetFolder);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public GrooveGraph generateStartGraph(BPMNProcessModel bpmnProcessModel, boolean addPrefix) {
        // TODO: Add prefix if needed!
        GrooveGraphBuilder startGraphBuilder = new GrooveGraphBuilder().setName(bpmnProcessModel.getName());
        GrooveNode processInstance = new GrooveNode(TYPE_PROCESS_INSTANCE);
        GrooveNode running = new GrooveNode(TYPE_RUNNING);
        startGraphBuilder.addEdge(STATE, processInstance, running);

        GrooveNode startToken = new GrooveNode(TYPE_TOKEN);
        GrooveNode tokenName = new GrooveNode(this.createStringNodeLabel(getStartEventTokenName(bpmnProcessModel)));
        startGraphBuilder.addEdge(POSITION, startToken, tokenName);
        startGraphBuilder.addEdge(TOKENS, processInstance, startToken);

        return startGraphBuilder.build();
    }

    private String getStartEventTokenName(BPMNProcessModel bpmnProcessModel) {
        return bpmnProcessModel.getName() + "_" + bpmnProcessModel.getStartEvent().getName();
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
    public Stream<GrooveGraphRule> generateRules(BPMNProcessModel bpmnProcessModel, boolean addPrefix) {
        GrooveRuleBuilder ruleBuilder = new GrooveRuleBuilder(bpmnProcessModel, addPrefix);
        Set<BPMNProcessModel> visitedProcessModels = Sets.newHashSet(bpmnProcessModel);

        generateRules(bpmnProcessModel, ruleBuilder, visitedProcessModels);

        return ruleBuilder.getRules();
    }

    private void generateRules(BPMNProcessModel bpmnProcessModel, GrooveRuleBuilder ruleBuilder, Set<BPMNProcessModel> visitedProcessModels) {
        bpmnProcessModel.getControlFlowNodes().forEach(node -> node.accept(new ControlFlowNodeVisitor() {
            @Override
            public void handle(StartEvent startEvent) {
                if (startEvent.getOutgoingFlows().count() != 1) {
                    throw new RuntimeException("Start events should have exactly one outgoing flow!");
                }
                final String outgoingFlowID = startEvent.getOutgoingFlows().findFirst().get().getID();
                ruleBuilder.startRule(startEvent.getName());
                BPMNToGrooveTransformer.this.updateTokenPositionWhenRunning(getStartEventTokenName(bpmnProcessModel), outgoingFlowID, ruleBuilder);
                ruleBuilder.buildRule();
            }

            @Override
            public void handle(Task task) {
                task.getIncomingFlows().forEach(incomingFlow -> {
                    final String incomingFlowId = incomingFlow.getID();
                    ruleBuilder.startRule(this.getTaskOrCallActivityRuleName(task, incomingFlowId));
                    GrooveNode processInstance = BPMNToGrooveTransformer.this.createContextRunningProcessInstance(ruleBuilder);
                    deleteTokenWithPosition(ruleBuilder, processInstance, incomingFlowId);
                    task.getOutgoingFlows().forEach(outgoingFlow -> {
                        final String outgoingFlowID = outgoingFlow.getID();
                        addTokenWithPosition(ruleBuilder, processInstance, outgoingFlowID);
                    });
                    ruleBuilder.buildRule();
                });
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
                BPMNToGrooveTransformer.this.generateRules(callActivity.getSubProcessModel(), ruleBuilder, visitedProcessModels);
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
                addTokenWithPosition(ruleBuilder, subProcessInstance, getStartEventTokenName(callActivity.getSubProcessModel()));

                ruleBuilder.buildRule();
            }

            private String getTaskOrCallActivityRuleName(ControlFlowNode taskOrCallActivity, String incomingFlowId) {
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
                    exclusiveGateway.getOutgoingFlows().forEach(outFlow -> {
                        final String outgoingFlowID = outFlow.getID();
                        ruleBuilder.startRule(this.getExclusiveGatewayName(exclusiveGateway, incomingFlowId, outgoingFlowID));
                        BPMNToGrooveTransformer.this.updateTokenPositionWhenRunning(incomingFlowId, outgoingFlowID, ruleBuilder);
                        ruleBuilder.buildRule();
                    });
                });
            }

            private String getExclusiveGatewayName(ExclusiveGateway exclusiveGateway, String incomingFlowId, String outFlowID) {
                final long inCount = exclusiveGateway.getIncomingFlows().count();
                final long outCount = exclusiveGateway.getOutgoingFlows().count();
                if (inCount == 1 && outCount == 1) {
                    return exclusiveGateway.getName();
                }
                if (inCount == 1) {
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
            public void handle(LinkEvent linkEvent) {
                switch (linkEvent.getType()) {
                    case THROW:
                        ruleBuilder.startRule("Throw_" + linkEvent.getName());
                        if (linkEvent.getIncomingFlows().count() != 1) {
                            throw new RuntimeException("A throw link event should have exactly one incoming sequence flow!");
                        }
                        final String incomingFlowId = linkEvent.getIncomingFlows().findFirst().get().getID();
                        BPMNToGrooveTransformer.this.updateTokenPositionWhenRunning(incomingFlowId, linkEvent.getName(), ruleBuilder);
                        break;
                    case CATCH:
                        ruleBuilder.startRule("Catch_" + linkEvent.getName());
                        if (linkEvent.getOutgoingFlows().count() != 1) {
                            throw new RuntimeException("A catch link event should have exactly one outgoing sequence flow!");
                        }
                        final String outgoingFlowId = linkEvent.getOutgoingFlows().findFirst().get().getID();
                        BPMNToGrooveTransformer.this.updateTokenPositionWhenRunning(linkEvent.getName(), outgoingFlowId, ruleBuilder);
                        break;
                }
                ruleBuilder.buildRule();
            }
        }));
    }

    private void createInclusiveGatewayRules(GrooveRuleBuilder ruleBuilder, InclusiveGateway inclusiveGateway) {
        long incomingFlowCount = inclusiveGateway.getIncomingFlows().count();
        long outgoingFlowCount = inclusiveGateway.getOutgoingFlows().count();
        if (incomingFlowCount == 1 && outgoingFlowCount > 1) {
            this.createBranchingInclusiveGatewayRules(ruleBuilder, inclusiveGateway);
            return;
        }
        if (incomingFlowCount > 1 && outgoingFlowCount == 1) {
            this.createMergingInclusiveGatewayRules(ruleBuilder, inclusiveGateway);
            return;
        }
        if (incomingFlowCount == 0 || outgoingFlowCount == 0) {
            throw new RuntimeException(String.format("The inclusive gateway \"%s\" has 0 incoming or outgoing flows!", inclusiveGateway.getName()));
        }
        throw new RuntimeException("Inclusive gateway should not have multiple incoming and outgoing flows.");
    }

    private void createMergingInclusiveGatewayRules(GrooveRuleBuilder ruleBuilder, InclusiveGateway inclusiveGateway) {
        // Find the corresponding branching inclusive gateway
        Map<SequenceFlow, SequenceFlow> branchFlowsToInFlows = new HashMap<>();
        ControlFlowNode branchGateway = this.findCorrespondingBranchGateway(inclusiveGateway, branchFlowsToInFlows);
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

    private ControlFlowNode findCorrespondingBranchGateway(InclusiveGateway inclusiveGateway, Map<SequenceFlow, SequenceFlow> branchFlowsToInFlows) {
        final Set<ControlFlowNode> iGateways = inclusiveGateway.getIncomingFlows().map(inFlow -> searchBranchingGateway(inFlow, inFlow, branchFlowsToInFlows)).collect(Collectors.toSet());
        return getSingleGatewayOrThrowException(iGateways);
    }

    private ControlFlowNode searchBranchingGateway(SequenceFlow originalFlow, SequenceFlow currentFlow, Map<SequenceFlow, SequenceFlow> branchFlowsToInFlows) {
        final ControlFlowNode source = currentFlow.getSource();
        if (source.isInclusiveGateway()) {
            branchFlowsToInFlows.put(currentFlow, originalFlow);
            return source;
        }
        final Set<ControlFlowNode> iGateways = source.getIncomingFlows().map(inFlow -> searchBranchingGateway(originalFlow, inFlow, branchFlowsToInFlows)).collect(Collectors.toSet());
        return getSingleGatewayOrThrowException(iGateways);
    }

    private ControlFlowNode getSingleGatewayOrThrowException(Set<ControlFlowNode> iGateways) {
        if (iGateways.size() == 1) {
            return iGateways.iterator().next();
        } else {
            throw new RuntimeException("No matching branching inclusive Gateway found!");
        }
    }

    private void createBranchingInclusiveGatewayRules(GrooveRuleBuilder ruleBuilder, InclusiveGateway inclusiveGateway) {
        SequenceFlow incFlow = inclusiveGateway.getIncomingFlows().findFirst().get(); // size 1 means this operation is save.
        int i = 1;
        for (Set<SequenceFlow> outFlows : Sets.powerSet(inclusiveGateway.getOutgoingFlows().collect(Collectors.toCollection(LinkedHashSet::new)))) {
            if (outFlows.size() >= 1) { // Empty set is also part of the power set.
                ruleBuilder.startRule(inclusiveGateway.getName() + "_" + i);
                GrooveNode processInstance = this.createContextRunningProcessInstance(ruleBuilder);
                GrooveNode previousToken = ruleBuilder.deleteNode(TYPE_TOKEN);
                ruleBuilder.deleteEdge(TOKENS, processInstance, previousToken);
                String incFlowID = incFlow.getID();
                ruleBuilder.deleteEdge(POSITION, previousToken, ruleBuilder.contextNode(this.createStringNodeLabel(incFlowID)));

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

                // Terminate possible subprocess instances.
                GrooveNode subInstance = ruleBuilder.contextNode(TYPE_PROCESS_INSTANCE);
                ruleBuilder.contextEdge(SUBPROCESS, processInstance, subInstance);
                GrooveNode subRunning = ruleBuilder.deleteNode(TYPE_RUNNING);
                ruleBuilder.deleteEdge(STATE, subInstance, subRunning);
                GrooveNode subTerminated = ruleBuilder.addNode(TYPE_TERMINATED);
                ruleBuilder.addEdge(STATE, subInstance, subTerminated);

                GrooveNode forAll = ruleBuilder.contextNode(FORALL);
                ruleBuilder.contextEdge(AT, subInstance, forAll);
                ruleBuilder.contextEdge(AT, subRunning, forAll);
                ruleBuilder.contextEdge(AT, subTerminated, forAll);

                break;
        }

        ruleBuilder.buildRule();
    }

    private void createParallelGatewayRule(GrooveRuleBuilder ruleBuilder, ParallelGateway parallelGateway) {
        ruleBuilder.startRule(parallelGateway.getName());
        GrooveNode processInstance = this.createContextRunningProcessInstance(ruleBuilder);

        parallelGateway.getIncomingFlows().forEach(sequenceFlow -> deleteTokenWithPosition(ruleBuilder, processInstance, sequenceFlow.getID()));

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
