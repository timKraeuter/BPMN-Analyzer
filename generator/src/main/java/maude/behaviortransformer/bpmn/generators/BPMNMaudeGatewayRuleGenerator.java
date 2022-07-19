package maude.behaviortransformer.bpmn.generators;

import behavior.bpmn.AbstractProcess;
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

    public void createParallelGatewayRule(AbstractProcess process, ParallelGateway parallelGateway) {
        ruleBuilder.ruleName(getRuleNameForFlowNode(parallelGateway));

        String preTokens = getIncomingTokensForParallelGateway(parallelGateway) + ANY_OTHER_TOKENS;
        ruleBuilder.addPreObject(createProcessSnapshotObjectAnySubProcess(objectBuilder, process, preTokens));

        String postTokens = getOutgoingTokensForFlowNode(parallelGateway) + ANY_OTHER_TOKENS;
        ruleBuilder.addPostObject(createProcessSnapshotObjectAnySubProcess(objectBuilder, process, postTokens));

        ruleBuilder.build();
    }

    private String getIncomingTokensForParallelGateway(ParallelGateway parallelGateway) {
        return parallelGateway.getIncomingFlows()
                              .map(BPMNToMaudeTransformerHelper::getTokenForSequenceFlow)
                              .collect(Collectors.joining(" "));
    }
}
