package groove.behaviorTransformer;

import behavior.Behavior;
import behavior.bpmn.*;
import behavior.bpmn.auxiliary.ControlFlowNodeVisitor;
import behavior.bpmn.auxiliary.StartParallelOrElseControlFlowNodeVisitor;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import groove.graph.GrooveGraph;
import groove.graph.GrooveNode;
import groove.graph.rule.GrooveRuleBuilder;

import java.util.Collection;

public class BPMNToGrooveTransformer implements GrooveTransformer<BPMNProcessModel> {
    private static final String FINISHED_SUFFIX = "_finished";
    private static final String SYNCHRONISATION_SUFFIX = "_synchronisation";
    private static final String DISTRIBUTION_SUFFIX = "_distribution";

    @Override
    public GrooveGraph generateStartGraph(BPMNProcessModel bpmnProcessModel, boolean addPrefix) {
        String potentialPrefix = this.getPrefixOrEmpty(bpmnProcessModel, addPrefix);
        GrooveNode startEventNode = new GrooveNode(potentialPrefix + bpmnProcessModel.getStartEvent().getName());
        return new GrooveGraph(bpmnProcessModel.getName(), Sets.newHashSet(startEventNode), Sets.newHashSet());
    }

    @Override
    public GrooveRuleBuilder generateRules(BPMNProcessModel bpmnProcessModel, boolean addPrefix) {
        GrooveRuleBuilder ruleBuilder = new GrooveRuleBuilder(bpmnProcessModel, addPrefix);

        // Iteration order of LinkedHashMultimap needed for testcases
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
                        ruleGenerator.deleteNode(notParallelGatewayNode.getName());
                        ruleGenerator.addNode(node.getName());
                    }

                    @Override
                    public void handle(ParallelGateway parallelGateway) {
                        ruleGenerator.deleteNode(notParallelGatewayNode.getName());
                        ruleGenerator.addNode(notParallelGatewayNode.getName() + FINISHED_SUFFIX);

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
        parallelGatewayIncoming.keySet().forEach(parallelGateway -> {
            ruleBuilder.startRule(parallelGateway.getName() + SYNCHRONISATION_SUFFIX);

            final Collection<SequenceFlow> incomingFlows = parallelGatewayIncoming.get(parallelGateway);
            incomingFlows.forEach(sequenceFlow -> ruleBuilder.deleteNode(sequenceFlow.getSource().getName() + FINISHED_SUFFIX));
            ruleBuilder.addNode(parallelGateway.getName());

            ruleBuilder.buildRule();
        });

        // Distribution for parallel gateways
        parallelGatewayOutgoing.keySet().forEach(parallelGateway -> {
            ruleBuilder.startRule(parallelGateway.getName() + DISTRIBUTION_SUFFIX);

            final Collection<SequenceFlow> outgoingFlows = parallelGatewayOutgoing.get(parallelGateway);
            outgoingFlows.forEach(sequenceFlow -> {
                if (sequenceFlow.getTarget().isParallelGateway()) {
                    ruleBuilder.addNode(sequenceFlow.getTarget().getName() + FINISHED_SUFFIX);
                } else {
                    ruleBuilder.addNode(sequenceFlow.getTarget().getName());
                }
            });
            ruleBuilder.deleteNode(parallelGateway.getName());

            ruleBuilder.buildRule();
        });

        return ruleBuilder;
    }

    private String getPrefixOrEmpty(Behavior finiteStateMachine, Boolean addPrefix) {
        return addPrefix ? finiteStateMachine.getName() + "_" : "";
    }
}
