package groove.behaviorTransformer;

import behavior.bpmn.*;
import behavior.bpmn.auxiliary.ControlFlowNodeVisitor;
import behavior.bpmn.auxiliary.StartParallelOrElseControlFlowNodeVisitor;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import groove.graph.GrooveGraph;
import groove.graph.GrooveGraphBuilder;
import groove.graph.GrooveNode;
import groove.graph.rule.GrooveGraphRule;
import groove.graph.rule.GrooveRuleBuilder;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

public class BPMNToGrooveTransformer implements GrooveTransformer<BPMNProcessModel> {
    // Node names
    private static final String TYPE_TOKEN = TYPE + "Token";
    private static final String TYPE_CONTROL_TOKEN = TYPE + "ControlToken";
    private static final String TYPE_FORKED_TOKEN = TYPE + "ForkedToken";

    // Edge names
    private static final String BASE_TOKEN = "baseToken";
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
        GrooveNode startToken = new GrooveNode(TYPE_CONTROL_TOKEN);
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

        // Iteration order of LinkedHashMultimap needed for determinism.
        final Multimap<ParallelGateway, SequenceFlow> parallelGatewayOutgoing = LinkedHashMultimap.create();
        final Multimap<ParallelGateway, SequenceFlow> parallelGatewayIncoming = LinkedHashMultimap.create();

        bpmnProcessModel.getSequenceFlows().forEach(sequenceFlow -> sequenceFlow.getSource().accept(new ControlFlowNodeVisitor() {
            @Override
            public void handle(StartEvent startEvent) {
                this.handleNonParallelGateway(
                        startEvent,
                        sequenceFlow,
                        bpmnProcessModel,
                        ruleBuilder,
                        parallelGatewayIncoming);
            }

            @Override
            public void handle(Activity activity) {
                this.handleNonParallelGateway(
                        activity,
                        sequenceFlow,
                        bpmnProcessModel,
                        ruleBuilder,
                        parallelGatewayIncoming);
            }

            @Override
            public void handle(ExclusiveGateway exclusiveGateway) {
                this.handleNonParallelGateway(
                        exclusiveGateway,
                        sequenceFlow,
                        bpmnProcessModel,
                        ruleBuilder,
                        parallelGatewayIncoming);
            }

            private void handleNonParallelGateway(
                    ControlFlowNode notParallelGatewayNode,
                    SequenceFlow sequenceFlow,
                    BPMNProcessModel bpmnProcessModel,
                    GrooveRuleBuilder ruleGenerator,
                    Multimap<ParallelGateway, SequenceFlow> parallelGatewayIncoming) {
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
                        parallelGatewayIncoming.put(parallelGateway, sequenceFlow);
                    }
                });
                ruleGenerator.buildRule();
            }

            @Override
            public void handle(ParallelGateway parallelGateway) {
                parallelGatewayOutgoing.put(parallelGateway, sequenceFlow);
            }

            @Override
            public void handle(EndEvent endEvent) {
                throw new RuntimeException(
                        String.format("An end event should never be source of a sequence flow! " +
                                        "BPMN-Model: \"%s\", Sequence flow: \"%s\"",
                                bpmnProcessModel,
                                sequenceFlow));
            }
        }));

        // Synchronisation for parallel gateways
        parallelGatewayIncoming.keySet().forEach(parallelGateway ->
                this.createRuleForParallelGateway(
                        ruleBuilder,
                        parallelGatewayOutgoing,
                        parallelGatewayIncoming,
                        parallelGateway));
        return ruleBuilder.getRules();
    }

    private void createRuleForParallelGateway(
            GrooveRuleBuilder ruleBuilder,
            Multimap<ParallelGateway, SequenceFlow> parallelGatewayOutgoing,
            Multimap<ParallelGateway, SequenceFlow> parallelGatewayIncoming,
            ParallelGateway parallelGateway) {
        ruleBuilder.startRule(parallelGateway.getName());

        final Collection<SequenceFlow> incomingFlows = parallelGatewayIncoming.get(parallelGateway);
        final Collection<SequenceFlow> outgoingFlows = parallelGatewayOutgoing.get(parallelGateway);
        GrooveNode baseToken = ruleBuilder.contextNode(TYPE_TOKEN);
        if (outgoingFlows.size() > 1) {
            outgoingFlows.forEach(sequenceFlow -> {
                GrooveNode forkedToken = ruleBuilder.addNode(TYPE_FORKED_TOKEN);
                ruleBuilder.addEdge(BASE_TOKEN, forkedToken, baseToken);
                String flowTargetName = sequenceFlow.getTarget().getName();
                ruleBuilder.addEdge(POSITION, forkedToken, ruleBuilder.contextNode(this.createStringNodeLabel(flowTargetName)));
            });
        } else if (outgoingFlows.size() == 1) {
            SequenceFlow singleOutFlow = outgoingFlows.iterator().next();
            String outFlowTargetName = singleOutFlow.getTarget().getName();
            ruleBuilder.addEdge(POSITION, baseToken, ruleBuilder.contextNode(this.createStringNodeLabel(outFlowTargetName)));
        }
        if (incomingFlows.size() > 1) {
            AtomicReference<GrooveNode> previousToken = new AtomicReference<>();
            incomingFlows.forEach(sequenceFlow -> {
                GrooveNode forkedToken = ruleBuilder.deleteNode(TYPE_FORKED_TOKEN);
                if (previousToken.get() != null) {
                    ruleBuilder.contextEdge(UNEQUALS, previousToken.get(), forkedToken);
                }
                previousToken.set(forkedToken);
                ruleBuilder.deleteEdge(BASE_TOKEN, forkedToken, baseToken);
                ruleBuilder.deleteEdge(POSITION, forkedToken, ruleBuilder.contextNode(this.createStringNodeLabel(parallelGateway.getName())));
            });
        } else if (incomingFlows.size() == 1) {
            ruleBuilder.deleteEdge(POSITION, baseToken, ruleBuilder.contextNode(this.createStringNodeLabel(parallelGateway.getName())));
        }

        ruleBuilder.buildRule();
    }

    @Override
    public boolean isLayoutActivated() {
        return true; // TODO: implement layout as parameter!
    }
}
