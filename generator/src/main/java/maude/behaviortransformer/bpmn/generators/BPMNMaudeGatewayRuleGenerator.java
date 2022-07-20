package maude.behaviortransformer.bpmn.generators;

import behavior.bpmn.AbstractProcess;
import behavior.bpmn.SequenceFlow;
import behavior.bpmn.gateways.ExclusiveGateway;
import behavior.bpmn.gateways.ParallelGateway;
import maude.behaviortransformer.bpmn.BPMNToMaudeTransformerHelper;
import maude.generation.MaudeObjectBuilder;
import maude.generation.MaudeRuleBuilder;

import java.util.stream.Collectors;

import static maude.behaviortransformer.bpmn.BPMNToMaudeTransformerHelper.*;

public class BPMNMaudeGatewayRuleGenerator {
    private final MaudeRuleBuilder ruleBuilder;
    private final MaudeObjectBuilder objectBuilder;

    public BPMNMaudeGatewayRuleGenerator(MaudeRuleBuilder ruleBuilder) {
        this.ruleBuilder = ruleBuilder;
        objectBuilder = new MaudeObjectBuilder();
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
        ruleBuilder.ruleName(getFlowNodeNameAndID(exclusiveGateway));

        String preTokens = preToken + ANY_OTHER_TOKENS;
        ruleBuilder.addPreObject(createProcessSnapshotObjectAnySubProcess(objectBuilder, process, preTokens));

        String postTokens = getTokenForSequenceFlow(outgoingFlow) + ANY_OTHER_TOKENS;
        ruleBuilder.addPostObject(createProcessSnapshotObjectAnySubProcess(objectBuilder, process, postTokens));

        ruleBuilder.build();

    }

    public void createParallelGatewayRule(AbstractProcess process, ParallelGateway parallelGateway) {
        ruleBuilder.ruleName(getFlowNodeNameAndID(parallelGateway));

        String preTokens = getPreTokensForParallelGateway(parallelGateway) + ANY_OTHER_TOKENS;
        ruleBuilder.addPreObject(createProcessSnapshotObjectAnySubProcess(objectBuilder, process, preTokens));

        String postTokens = getOutgoingTokensForFlowNode(parallelGateway) + ANY_OTHER_TOKENS;
        ruleBuilder.addPostObject(createProcessSnapshotObjectAnySubProcess(objectBuilder, process, postTokens));

        ruleBuilder.build();
    }

    private String getPreTokensForParallelGateway(ParallelGateway parallelGateway) {
        if (parallelGateway.getIncomingFlows().findAny().isEmpty()) {
            return getTokenForFlowNode(parallelGateway);
        }
        return parallelGateway.getIncomingFlows()
                              .map(BPMNToMaudeTransformerHelper::getTokenForSequenceFlow)
                              .collect(Collectors.joining(" "));
    }
}
