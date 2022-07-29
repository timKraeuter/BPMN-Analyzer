package maude.behaviortransformer.bpmn.generators;

import behavior.bpmn.AbstractProcess;
import behavior.bpmn.SequenceFlow;
import behavior.bpmn.gateways.EventBasedGateway;
import behavior.bpmn.gateways.ExclusiveGateway;
import behavior.bpmn.gateways.ParallelGateway;
import maude.behaviortransformer.bpmn.BPMNToMaudeTransformerHelper;
import maude.generation.MaudeObjectBuilder;
import maude.generation.MaudeRuleBuilder;

import java.util.stream.Collectors;

public class BPMNMaudeGatewayRuleGenerator implements BPMNToMaudeTransformerHelper {
    private final MaudeRuleBuilder ruleBuilder;
    private final MaudeObjectBuilder objectBuilder;

    public BPMNMaudeGatewayRuleGenerator(MaudeRuleBuilder ruleBuilder) {
        this.ruleBuilder = ruleBuilder;
        objectBuilder = new MaudeObjectBuilder();
    }

    @Override
    public MaudeRuleBuilder getRuleBuilder() {
        return ruleBuilder;
    }

    @Override
    public MaudeObjectBuilder getObjectBuilder() {
        return objectBuilder;
    }

    public void createExclusiveGatewayRule(AbstractProcess process, ExclusiveGateway exclusiveGateway) {
        exclusiveGateway.getIncomingFlows().forEach(incomingFlow -> exclusiveGateway.getOutgoingFlows().forEach(
                outgoingFlow -> createExclusiveGatewayRule(process,
                                                           exclusiveGateway,
                                                           getTokenForSequenceFlow(incomingFlow),
                                                           outgoingFlow)));


        // No incoming flows means we expect a token sitting at the gateway.
        if (exclusiveGateway.getIncomingFlows().findAny().isEmpty()) {
            exclusiveGateway.getOutgoingFlows().forEach(outgoingFlow -> createExclusiveGatewayRule(process,
                                                                                                   exclusiveGateway,
                                                                                                   getTokenForFlowNode(
                                                                                                           exclusiveGateway),
                                                                                                   outgoingFlow));
        }

    }

    private void createExclusiveGatewayRule(AbstractProcess process,
                                            ExclusiveGateway exclusiveGateway,
                                            String preToken,
                                            SequenceFlow outgoingFlow) {
        ruleBuilder.startRule(getFlowNodeRuleName(exclusiveGateway));

        String preTokens = preToken + ANY_OTHER_TOKENS;
        ruleBuilder.addPreObject(createProcessSnapshotObjectAnySubProcessAndMessages(process, preTokens));

        String postTokens = getTokenForSequenceFlow(outgoingFlow) + ANY_OTHER_TOKENS;
        ruleBuilder.addPostObject(createProcessSnapshotObjectAnySubProcessAndMessages(process, postTokens));

        ruleBuilder.buildRule();

    }

    public void createParallelGatewayRule(AbstractProcess process, ParallelGateway parallelGateway) {
        ruleBuilder.startRule(getFlowNodeRuleName(parallelGateway));

        String preTokens = getPreTokensForParallelGateway(parallelGateway) + ANY_OTHER_TOKENS;
        ruleBuilder.addPreObject(createProcessSnapshotObjectAnySubProcessAndMessages(process, preTokens));

        String postTokens = getOutgoingTokensForFlowNode(parallelGateway) + ANY_OTHER_TOKENS;
        ruleBuilder.addPostObject(createProcessSnapshotObjectAnySubProcessAndMessages(process, postTokens));

        ruleBuilder.buildRule();
    }

    private String getPreTokensForParallelGateway(ParallelGateway parallelGateway) {
        if (parallelGateway.getIncomingFlows().findAny().isEmpty()) {
            return getTokenForFlowNode(parallelGateway);
        }
        return parallelGateway.getIncomingFlows()
                              .map(this::getTokenForSequenceFlow)
                              .collect(Collectors.joining(" "));
    }

    public void createEventBasedGatewayRule(AbstractProcess process, EventBasedGateway eventBasedGateway) {
        eventBasedGateway.getIncomingFlows().forEach(inFlow -> {
            ruleBuilder.startRule(getFlowNodeRuleNameWithIncFlow(eventBasedGateway, inFlow.getId()));

            String preTokens = getTokenForSequenceFlow(inFlow) + ANY_OTHER_TOKENS;
            ruleBuilder.addPreObject(createProcessSnapshotObjectAnySubProcessAndMessages(process, preTokens));

            String postTokens = getTokenForFlowNode(eventBasedGateway) + ANY_OTHER_TOKENS;
            ruleBuilder.addPostObject(createProcessSnapshotObjectAnySubProcessAndMessages(process, postTokens));

            ruleBuilder.buildRule();
        });
        // Effects the rules of the subsequent flow nodes!
        // Possible subsequent nodes: Message catch, Receive task, Signal catch, timer and condition.
        // We currently only implemented the first two.
    }
}
