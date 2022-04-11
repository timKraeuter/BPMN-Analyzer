package groove.behaviorTransformer.bpmn.generators;

import behavior.bpmn.AbstractProcess;
import behavior.bpmn.BPMNCollaboration;
import behavior.bpmn.FlowNode;
import behavior.bpmn.SequenceFlow;
import behavior.bpmn.gateways.*;
import com.google.common.collect.Sets;
import groove.behaviorTransformer.bpmn.BPMNToGrooveTransformerHelper;
import groove.graph.GrooveNode;
import groove.graph.rule.GrooveRuleBuilder;

import java.util.*;
import java.util.stream.Collectors;

import static groove.behaviorTransformer.GrooveTransformerHelper.createStringNodeLabel;
import static groove.behaviorTransformer.bpmn.BPMNToGrooveTransformerConstants.*;
import static groove.behaviorTransformer.bpmn.BPMNToGrooveTransformerHelper.updateTokenPositionWhenRunning;

public class BPMNGatewayRuleGeneratorImpl implements BPMNGatewayRuleGenerator {
    private final BPMNCollaboration collaboration;
    private final GrooveRuleBuilder ruleBuilder;

    public BPMNGatewayRuleGeneratorImpl(BPMNCollaboration collaboration, GrooveRuleBuilder ruleBuilder) {
        this.collaboration = collaboration;
        this.ruleBuilder = ruleBuilder;
    }

    @Override
    public void createExclusiveGatewayRules(AbstractProcess process, ExclusiveGateway exclusiveGateway) {
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

    @Override
    public void createParallelGatewayRule(AbstractProcess process, ParallelGateway parallelGateway) {
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

    @Override
    public void createEventBasedGatewayRule(EventBasedGateway eventBasedGateway, AbstractProcess process) {
        boolean implicitExclusiveGateway = eventBasedGateway.getIncomingFlows().count() > 1;
        eventBasedGateway.getIncomingFlows().forEach(inFlow -> {
            String ruleName = implicitExclusiveGateway ? inFlow.getID() + "_" + eventBasedGateway.getName() :
                    eventBasedGateway.getName();
            ruleBuilder.startRule(ruleName);
            updateTokenPositionWhenRunning(process, inFlow.getID(), eventBasedGateway.getName(), ruleBuilder);
            ruleBuilder.buildRule();
        });
        // Effects the rules of the subsequent flow nodes!
        // Possible subsequent nodes: Message catch, Receive task, Signal catch, timer and condition.
        // We currently only implemented the first three.
    }

    @Override
    public void createInclusiveGatewayRules(AbstractProcess process, InclusiveGateway inclusiveGateway) {
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


    private void createRuleExclusiveGatewayRule(AbstractProcess process,
                                                GrooveRuleBuilder ruleBuilder,
                                                ExclusiveGateway exclusiveGateway,
                                                String oldTokenPosition,
                                                String newTokenPosition) {
        ruleBuilder.startRule(this.getExclusiveGatewayName(exclusiveGateway, oldTokenPosition, newTokenPosition));
        updateTokenPositionWhenRunning(process, oldTokenPosition, newTokenPosition, ruleBuilder);
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
}
