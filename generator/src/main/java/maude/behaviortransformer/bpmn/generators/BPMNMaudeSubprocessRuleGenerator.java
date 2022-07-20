package maude.behaviortransformer.bpmn.generators;

import behavior.bpmn.AbstractProcess;
import behavior.bpmn.SequenceFlow;
import behavior.bpmn.activities.CallActivity;
import behavior.bpmn.events.StartEventType;
import groove.behaviortransformer.bpmn.generators.BPMNSubprocessRuleGenerator;
import maude.behaviortransformer.bpmn.BPMNMaudeRuleGenerator;
import maude.behaviortransformer.bpmn.BPMNToMaudeTransformerHelper;
import maude.generation.MaudeObject;
import maude.generation.MaudeObjectBuilder;
import maude.generation.MaudeRuleBuilder;

import java.util.stream.Collectors;

import static groove.behaviortransformer.bpmn.BPMNToGrooveTransformerConstants.END;
import static maude.behaviortransformer.bpmn.BPMNToMaudeTransformerHelper.*;

public class BPMNMaudeSubprocessRuleGenerator implements BPMNSubprocessRuleGenerator {
    private final BPMNMaudeRuleGenerator bpmnMaudeRuleGenerator;
    private final MaudeRuleBuilder ruleBuilder;
    private final MaudeObjectBuilder objectBuilder;

    public BPMNMaudeSubprocessRuleGenerator(BPMNMaudeRuleGenerator bpmnMaudeRuleGenerator,
                                            MaudeRuleBuilder ruleBuilder) {
        this.bpmnMaudeRuleGenerator = bpmnMaudeRuleGenerator;
        this.ruleBuilder = ruleBuilder;
        objectBuilder = new MaudeObjectBuilder();
    }

    public void createCallActivityRulesForProcess(AbstractProcess process, CallActivity callActivity) {
        // Rules for instantiating a subprocess
        callActivity.getIncomingFlows().forEach(incomingFlow -> this.createSubProcessInstantiationRule(process,
                                                                                                       callActivity,
                                                                                                       incomingFlow));

        // Rule for terminating a subprocess
        this.createTerminateSubProcessRule(process, callActivity);

        // Generate rules for the sub process
        this.createRulesForExecutingTheSubProcess(callActivity);

        // TODO: Boundary events
    }

    private void createSubProcessInstantiationRule(AbstractProcess process,
                                                   CallActivity callActivity,
                                                   SequenceFlow incomingFlow) {

        ruleBuilder.ruleName(getTaskOrCallActivityRuleName(callActivity, incomingFlow.getId()));

        String preTokens = getTokenForSequenceFlow(incomingFlow) + ANY_OTHER_TOKENS;
        ruleBuilder.addPreObject(createProcessSnapshotObjectAnySubProcessAndMessages(objectBuilder, process, preTokens));

        String subProcessTokens;
        if (subprocessHasStartEvents(callActivity)) {
            // Subprocess has start events which get tokens!
            subProcessTokens = callActivity.getSubProcessModel().getStartEvents().stream()
                                           .filter(startEvent -> startEvent.getType() == StartEventType.NONE)
                                           .map(BPMNToMaudeTransformerHelper::getTokenForFlowNode)
                                           .collect(Collectors.joining(" "));
        } else {
            // All activites and gateways without incoming sequence flows get a token.
            subProcessTokens = callActivity.getSubProcessModel().getFlowNodes()
                                           .filter(flowNode -> flowNode.isTask() ||
                                                               flowNode.isGateway())
                                           .filter(flowNode -> flowNode.getIncomingFlows().findAny().isEmpty())
                                           .map(BPMNToMaudeTransformerHelper::getTokenForFlowNode)
                                           .collect(Collectors.joining(" "));
        }
        MaudeObject subProcess = createProcessSnapshotObjectNoSubProcess(objectBuilder,
                                                                         callActivity.getSubProcessModel(),
                                                                         subProcessTokens);
        ruleBuilder.addPostObject(createProcessSnapshotObjectAnyMessages(objectBuilder,
                                                                         process,
                                                                        subProcess.generateObjectString() +
                                                                        ANY_OTHER_SUBPROCESSES,
                                                                         ANY_TOKENS, "Running"));

        ruleBuilder.build();
    }

    private void createTerminateSubProcessRule(AbstractProcess process, CallActivity callActivity) {
        ruleBuilder.ruleName(getFlowNodeNameAndID(callActivity) + END);

        MaudeObject subProcess = createTerminatedProcessSnapshot(objectBuilder,
                                                                 callActivity.getSubProcessModel());
        ruleBuilder.addPreObject(createProcessSnapshotObjectAnyMessages(objectBuilder,
                                                                        process,
                                                                       subProcess.generateObjectString() +
                                                                       ANY_OTHER_SUBPROCESSES,
                                                                        ANY_TOKENS, "Running"));

        // Add outgoing tokens
        String postTokens = getOutgoingTokensForFlowNode(callActivity) + ANY_OTHER_TOKENS;

        // Subprocess is deleted (since it is not in the post object).
        ruleBuilder.addPostObject(createProcessSnapshotObjectAnySubProcessAndMessages(objectBuilder,
                                                                                      process,
                                                                                      postTokens));


        ruleBuilder.build();
    }

    private void createRulesForExecutingTheSubProcess(CallActivity callActivity) {
        if (bpmnMaudeRuleGenerator.getVisitedProcessModels().contains(callActivity.getSubProcessModel())) {
            return;
        }
        bpmnMaudeRuleGenerator.getVisitedProcessModels().add(callActivity.getSubProcessModel());
        bpmnMaudeRuleGenerator.generateRulesForProcess(callActivity.getSubProcessModel());
    }
}
