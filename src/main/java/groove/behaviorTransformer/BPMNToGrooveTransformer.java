package groove.behaviorTransformer;

import behavior.bpmn.Activity;
import behavior.bpmn.BPMNProcessModel;
import behavior.bpmn.ControlFlowNode;
import behavior.bpmn.SequenceFlow;
import behavior.bpmn.auxiliary.ControlFlowNodeVisitor;
import behavior.bpmn.auxiliary.StartParallelOrElseControlFlowNodeVisitor;
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
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BPMNToGrooveTransformer implements GrooveTransformer<BPMNProcessModel> {
    private static final String TYPE_GRAPH_DIR = "/BPMNTypeGraph";
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

    @Override
    public void generateAndWriteRulesFurther(BPMNProcessModel model, boolean addPrefix, File targetFolder) {
        this.copyTypeGraph(targetFolder);
    }

    private void copyTypeGraph(File targetFolder) {
        File sourceDirectory = new File(this.getClass().getResource(TYPE_GRAPH_DIR).getFile());
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
        GrooveNode tokenName = new GrooveNode(this.createStringNodeLabel(bpmnProcessModel.getStartEvent().getName()));
        startGraphBuilder.addEdge(POSITION, startToken, tokenName);
        startGraphBuilder.addEdge(TOKENS, processInstance, startToken);

        return startGraphBuilder.build();
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

        final Set<ParallelGateway> parallelGateways = new LinkedHashSet<>();
        final Set<InclusiveGateway> inclusiveGateways = new LinkedHashSet<>();
        // One rule for each sequence flow
        bpmnProcessModel.getSequenceFlows().forEach(sequenceFlow -> sequenceFlow.getSource().accept(new ControlFlowNodeVisitor() {
            @Override
            public void handle(StartEvent startEvent) {
                this.handleNonParallelGateway(
                        startEvent,
                        sequenceFlow,
                        bpmnProcessModel,
                        ruleBuilder,
                        parallelGateways);
            }

            @Override
            public void handle(Activity activity) {
                this.handleNonParallelGateway(
                        activity,
                        sequenceFlow,
                        bpmnProcessModel,
                        ruleBuilder,
                        parallelGateways);
            }

            @Override
            public void handle(ExclusiveGateway exclusiveGateway) {
                this.handleNonParallelGateway(
                        exclusiveGateway,
                        sequenceFlow,
                        bpmnProcessModel,
                        ruleBuilder,
                        parallelGateways);
            }

            private void handleNonParallelGateway(
                    ControlFlowNode notParallelGatewayNode,
                    SequenceFlow sequenceFlow,
                    BPMNProcessModel bpmnProcessModel,
                    GrooveRuleBuilder ruleGenerator,
                    Set<ParallelGateway> parallelGateways) {
                ruleGenerator.startRule(sequenceFlow.getName());
                sequenceFlow.getTarget().accept(new StartParallelOrElseControlFlowNodeVisitor() {
                    @Override
                    public void handle(StartEvent startEvent) {
                        throw new RuntimeException(
                                String.format("There should be no sequence flow to a start event! " +
                                                "BPMN-Model: \"%s\", Sequence flow: \"%s\"",
                                        bpmnProcessModel,
                                        sequenceFlow));
                    }

                    @Override
                    public void handleRest(ControlFlowNode node) {
                        BPMNToGrooveTransformer.this.updateTokenPositionWhenRunning(
                                notParallelGatewayNode.getName(),
                                node.getName(),
                                ruleBuilder);
                    }

                    @Override
                    public void handle(ParallelGateway parallelGateway) {
                        BPMNToGrooveTransformer.this.updateTokenPositionWhenRunning(
                                notParallelGatewayNode.getName(),
                                parallelGateway.getName(),
                                ruleBuilder);
                        parallelGateways.add(parallelGateway);
                    }

                    @Override
                    public void handle(InclusiveGateway inclusiveGateway) {
                        inclusiveGateways.add(inclusiveGateway);
                    }

                    @Override
                    public void handle(LinkEvent linkEvent) {
                        switch (linkEvent.getType()) {
                            case THROW:
                                BPMNToGrooveTransformer.this.updateTokenPositionWhenRunning(
                                        notParallelGatewayNode.getName(),
                                        linkEvent.getName(),
                                        ruleBuilder);
                                break;
                            case CATCH:
                                throw new RuntimeException("A link catch event cannot have incoming sequence flows!");
                        }
                    }
                });
                ruleGenerator.buildRule();
            }

            @Override
            public void handle(ParallelGateway parallelGateway) {
                parallelGateways.add(parallelGateway);
            }

            @Override
            public void handle(InclusiveGateway inclusiveGateway) {
                inclusiveGateways.add(inclusiveGateway);
            }

            @Override
            public void handle(EndEvent endEvent) {
                throw new RuntimeException(
                        String.format("An end event should never be source of a sequence flow! " +
                                        "BPMN-Model: \"%s\", Sequence flow: \"%s\"",
                                bpmnProcessModel,
                                sequenceFlow));
            }

            @Override
            public void handle(LinkEvent linkEvent) {
                switch (linkEvent.getType()) {
                    case THROW:
                        // do nothing. It is not possible for a throw event to have outgoing flows!
                        throw new RuntimeException("A link throw event cannot have outgoing sequence flows!");
                    case CATCH:
                        this.handleNonParallelGateway(
                                linkEvent,
                                sequenceFlow,
                                bpmnProcessModel,
                                ruleBuilder,
                                parallelGateways);
                        break;
                }
            }
        }));

        parallelGateways.forEach(parallelGateway -> this.createParallelGatewayRule(ruleBuilder, parallelGateway));

        inclusiveGateways.forEach(inclusiveGateway -> this.createInclusiveGatewayRules(ruleBuilder, inclusiveGateway));

        bpmnProcessModel.getEndEvents().forEach(endEvent -> this.createEndEventRule(ruleBuilder, endEvent));

        return ruleBuilder.getRules();
    }

    private void createInclusiveGatewayRules(GrooveRuleBuilder ruleBuilder, InclusiveGateway inclusiveGateway) {
        long incomingFlowCount = inclusiveGateway.getIncomingFlows().count();
        long outgoingFlowCount = inclusiveGateway.getOutgoingFlows().count();
        if (incomingFlowCount == 1 && outgoingFlowCount > 1) {
            this.createBranchingInclusiveGatewayRules(ruleBuilder, inclusiveGateway);
        }
    }

    private void createBranchingInclusiveGatewayRules(GrooveRuleBuilder ruleBuilder, InclusiveGateway inclusiveGateway) {
        SequenceFlow incFlow = inclusiveGateway.getIncomingFlows().findFirst().get(); // size 1 means this operation is save.
        int i = 1;
        for (Set<SequenceFlow> sequenceFlows : Sets.powerSet(inclusiveGateway.getOutgoingFlows().collect(Collectors.toSet()))) {
            if (sequenceFlows.size() >= 1) { // Empty set is also part of the power set.
                ruleBuilder.startRule(inclusiveGateway.getName() + i);
                GrooveNode processInstance = this.createContextRunningProcessInstance(ruleBuilder);
                GrooveNode previousToken = ruleBuilder.deleteNode(TYPE_TOKEN);
                String incFlowSourceName = incFlow.getSource().getName();
                ruleBuilder.deleteEdge(
                        POSITION,
                        previousToken,
                        ruleBuilder.contextNode(this.createStringNodeLabel(incFlowSourceName)));

                StringBuilder stringBuilder = new StringBuilder();
                sequenceFlows.forEach(sequenceFlow -> {
                    String sequenceFlowTargetName = sequenceFlow.getTarget().getName();
                    GrooveNode newToken = ruleBuilder.addNode(TYPE_TOKEN);
                    ruleBuilder.addEdge(
                            POSITION,
                            newToken,
                            ruleBuilder.contextNode(this.createStringNodeLabel(sequenceFlowTargetName)));
                    stringBuilder.append(sequenceFlowTargetName);
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
        ruleBuilder.startRule(endEvent.getName());

        GrooveNode processInstance = ruleBuilder.contextNode(TYPE_PROCESS_INSTANCE);

        GrooveNode token = ruleBuilder.deleteNode(TYPE_TOKEN);
        GrooveNode position = ruleBuilder.contextNode(this.createStringNodeLabel(endEvent.getName()));
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

        // Delete one token for each incoming flow.
        Set<GrooveNode> previousTokens = new LinkedHashSet<>();
        parallelGateway.getIncomingFlows().forEach(sequenceFlow -> {
            GrooveNode forkedToken = ruleBuilder.deleteNode(TYPE_TOKEN);
            ruleBuilder.deleteEdge(TOKENS, processInstance, forkedToken);
            previousTokens.forEach(previousToken -> ruleBuilder.contextEdge(UNEQUALS, previousToken, forkedToken));
            previousTokens.add(forkedToken);
            ruleBuilder.deleteEdge(POSITION, forkedToken, ruleBuilder.contextNode(this.createStringNodeLabel(parallelGateway.getName())));
        });

        // Add one token for each outgoing flow.
        parallelGateway.getOutgoingFlows().forEach(sequenceFlow -> {
            GrooveNode forkedToken = ruleBuilder.addNode(TYPE_TOKEN);
            ruleBuilder.addEdge(TOKENS, processInstance, forkedToken);
            String flowTargetName = sequenceFlow.getTarget().getName();
            ruleBuilder.addEdge(POSITION, forkedToken, ruleBuilder.contextNode(this.createStringNodeLabel(flowTargetName)));
        });

        ruleBuilder.buildRule();
    }

    @Override
    public boolean isLayoutActivated() {
        return true; // TODO: implement layout as parameter!
    }
}
