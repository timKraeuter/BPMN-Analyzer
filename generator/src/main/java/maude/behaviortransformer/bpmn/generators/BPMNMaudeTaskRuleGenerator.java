package maude.behaviortransformer.bpmn.generators;

import behavior.bpmn.AbstractProcess;
import behavior.bpmn.FlowNode;
import behavior.bpmn.SequenceFlow;
import behavior.bpmn.activities.tasks.Task;
import maude.generation.MaudeObjectBuilder;
import maude.generation.MaudeRuleBuilder;

import static groove.behaviortransformer.bpmn.BPMNToGrooveTransformerConstants.END;
import static groove.behaviortransformer.bpmn.BPMNToGrooveTransformerConstants.START;
import static maude.behaviortransformer.bpmn.BPMNToMaudeTransformerHelper.*;

public class BPMNMaudeTaskRuleGenerator {
    private final MaudeRuleBuilder ruleBuilder;
    private final MaudeObjectBuilder objectBuilder;

    public BPMNMaudeTaskRuleGenerator(MaudeRuleBuilder ruleBuilder) {
        this.ruleBuilder = ruleBuilder;
        this.objectBuilder = new MaudeObjectBuilder();
    }

    public void createTaskRulesForProcess(AbstractProcess process, Task task) {
        // Rules for starting the task
        task.getIncomingFlows().forEach(incomingFlow -> createStartTaskRule(process, task, incomingFlow));
        // Rule for ending the task
        createEndTaskRule(process, task);

        // TODO: Boundary events
    }

    private void createStartTaskRule(AbstractProcess process, Task task, SequenceFlow incomingFlow) {
        ruleBuilder.ruleName(getTaskOrCallActivityRuleName(task, incomingFlow.getId()) + START);

        String preTokens = getTokenForSequenceFlow(incomingFlow) + ANY_OTHER_TOKENS;
        ruleBuilder.addPreObject(createProcessSnapshotObjectAnySubProcess(objectBuilder, process, preTokens));

        String postTokens = getTokenForActivity(task) + ANY_OTHER_TOKENS;
        ruleBuilder.addPostObject(createProcessSnapshotObjectAnySubProcess(objectBuilder, process, postTokens));

        ruleBuilder.build();
    }

    private void createEndTaskRule(AbstractProcess process, Task task) {
        ruleBuilder.ruleName(getFlowNodeNameAndID(task) + END);

        String preTokens = getTokenForActivity(task) + ANY_OTHER_TOKENS;
        ruleBuilder.addPreObject(createProcessSnapshotObjectAnySubProcess(objectBuilder, process, preTokens));

        String postTokens = getOutgoingTokensForFlowNode(task) + ANY_OTHER_TOKENS;
        ruleBuilder.addPostObject(createProcessSnapshotObjectAnySubProcess(objectBuilder, process, postTokens));

        ruleBuilder.build();
    }

    String getTaskOrCallActivityRuleName(FlowNode taskOrCallActivity, String incomingFlowId) {
        if (taskOrCallActivity.getIncomingFlows().count() > 1) {
            return String.format("%s_%s", getFlowNodeNameAndID(taskOrCallActivity), incomingFlowId);
        }
        return getFlowNodeNameAndID(taskOrCallActivity);
    }

    // SendTask

    // ReceiveTask
}
