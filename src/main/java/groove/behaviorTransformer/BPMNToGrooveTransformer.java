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
import behavior.bpmn.gateways.ParallelGateway;
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
import java.util.stream.Stream;

public class BPMNToGrooveTransformer implements GrooveTransformer<BPMNProcessModel> {
    // Node names
    private static final String TYPE_TOKEN = TYPE + "Token";

    // Edge names
    private static final String POSITION = "position";

    @Override
    public void generateAndWriteRulesFurther(BPMNProcessModel model, boolean addPrefix, File targetFolder) {
        this.copyTypeGraph(targetFolder);
    }

    private void copyTypeGraph(File targetFolder) {
        File sourceDirectory = new File(this.getClass().getResource("/BPMN").getFile());
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
        GrooveNode startToken = new GrooveNode(TYPE_TOKEN);
        GrooveNode tokenName = new GrooveNode(this.createStringNodeLabel(bpmnProcessModel.getStartEvent().getName()));
        startGraphBuilder.addEdge(POSITION, startToken, tokenName);
        return startGraphBuilder.build();
    }

    private void updateTokenPosition(String oldPosition, String newPosition, GrooveRuleBuilder ruleBuilder) {
        GrooveNode token = ruleBuilder.contextNode(TYPE_TOKEN);
        GrooveNode oldTokenPosition = ruleBuilder.contextNode(this.createStringNodeLabel(oldPosition));
        ruleBuilder.deleteEdge(POSITION, token, oldTokenPosition);

        GrooveNode newTokenPosition = ruleBuilder.contextNode(this.createStringNodeLabel(newPosition));
        ruleBuilder.addEdge(POSITION, token, newTokenPosition);
    }

    @Override
    public Stream<GrooveGraphRule> generateRules(BPMNProcessModel bpmnProcessModel, boolean addPrefix) {
        GrooveRuleBuilder ruleBuilder = new GrooveRuleBuilder(bpmnProcessModel, addPrefix);

        final Set<ParallelGateway> parallelGateways = new LinkedHashSet<>();
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
                        BPMNToGrooveTransformer.this.updateTokenPosition(
                                notParallelGatewayNode.getName(),
                                node.getName(),
                                ruleBuilder);
                    }

                    @Override
                    public void handle(ParallelGateway parallelGateway) {
                        BPMNToGrooveTransformer.this.updateTokenPosition(
                                notParallelGatewayNode.getName(),
                                parallelGateway.getName(),
                                ruleBuilder);
                        parallelGateways.add(parallelGateway);
                    }

                    @Override
                    public void handle(LinkEvent linkEvent) {
                        switch (linkEvent.getType()) {
                            case THROW:
                                BPMNToGrooveTransformer.this.updateTokenPosition(
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

        bpmnProcessModel.getEndEvents().forEach(endEvent -> this.createEndEventRule(ruleBuilder, endEvent));

        return ruleBuilder.getRules();
    }

    private void createEndEventRule(GrooveRuleBuilder ruleBuilder, EndEvent endEvent) {
        ruleBuilder.startRule(endEvent.getName());

        GrooveNode token = ruleBuilder.deleteNode(TYPE_TOKEN);
        GrooveNode oldTokenPosition = ruleBuilder.contextNode(this.createStringNodeLabel(endEvent.getName()));
        ruleBuilder.deleteEdge(POSITION, token, oldTokenPosition);

        ruleBuilder.buildRule();
    }

    private void createParallelGatewayRule(GrooveRuleBuilder ruleBuilder, ParallelGateway parallelGateway) {
        ruleBuilder.startRule(parallelGateway.getName());

        // Delete one token for each incoming flow.
        Set<GrooveNode> previousTokens = new LinkedHashSet<>();
        parallelGateway.getIncomingFlows().forEach(sequenceFlow -> {
            GrooveNode forkedToken = ruleBuilder.deleteNode(TYPE_TOKEN);
            previousTokens.forEach(previousToken -> ruleBuilder.contextEdge(UNEQUALS, previousToken, forkedToken));
            previousTokens.add(forkedToken);
            ruleBuilder.deleteEdge(POSITION, forkedToken, ruleBuilder.contextNode(this.createStringNodeLabel(parallelGateway.getName())));
        });

        // Add one token for each outgoing flow.
        parallelGateway.getOutgoingFlows().forEach(sequenceFlow -> {
            GrooveNode forkedToken = ruleBuilder.addNode(TYPE_TOKEN);
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
