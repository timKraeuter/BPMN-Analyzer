package maude.behaviortransformer.bpmn.generators;

import behavior.bpmn.AbstractProcess;
import behavior.bpmn.BPMNCollaboration;
import behavior.bpmn.SequenceFlow;
import behavior.bpmn.activities.tasks.AbstractTask;
import behavior.bpmn.activities.tasks.ReceiveTask;
import behavior.bpmn.activities.tasks.SendTask;
import behavior.bpmn.auxiliary.exceptions.BPMNRuntimeException;
import maude.behaviortransformer.bpmn.BPMNToMaudeTransformerHelper;
import maude.generation.MaudeObjectBuilder;
import maude.generation.MaudeRuleBuilder;

import java.util.function.Consumer;

import static groove.behaviortransformer.bpmn.BPMNToGrooveTransformerConstants.END;
import static groove.behaviortransformer.bpmn.BPMNToGrooveTransformerConstants.START;

public class BPMNMaudeTaskRuleGenerator implements BPMNToMaudeTransformerHelper {
    private final BPMNCollaboration collaboration;
    private final MaudeRuleBuilder ruleBuilder;
    private final MaudeObjectBuilder objectBuilder;

    public BPMNMaudeTaskRuleGenerator(BPMNCollaboration collaboration, MaudeRuleBuilder ruleBuilder) {
        this.collaboration = collaboration;
        this.ruleBuilder = ruleBuilder;
        this.objectBuilder = new MaudeObjectBuilder();
    }

    public void createTaskRulesForProcess(AbstractProcess process,
                                          AbstractTask task,
                                          Consumer<MaudeRuleBuilder> endTaskRuleAdditions) {
        // Rules for starting the task
        task.getIncomingFlows().forEach(incomingFlow -> createStartTaskRule(process, task, incomingFlow));
        // Rule for ending the task
        createEndTaskRule(process, task, endTaskRuleAdditions);

        // TODO: Boundary events
    }

    public void createTaskRulesForProcess(AbstractProcess process, AbstractTask task) {
        createTaskRulesForProcess(process, task, x -> {
            // NOOP
        });
    }

    private void createStartTaskRule(AbstractProcess process, AbstractTask task, SequenceFlow incomingFlow) {
        ruleBuilder.startRule(getFlowNodeRuleNameWithIncFlow(task, incomingFlow.getId()) + START);

        String preTokens = getTokenForSequenceFlow(incomingFlow) + ANY_OTHER_TOKENS;
        ruleBuilder.addPreObject(createProcessSnapshotObjectAnySubProcessAndMessages(process, preTokens));

        String postTokens = getTokenForFlowNode(task) + ANY_OTHER_TOKENS;
        ruleBuilder.addPostObject(createProcessSnapshotObjectAnySubProcessAndMessages(process, postTokens));

        ruleBuilder.buildRule();
    }

    private void createEndTaskRule(AbstractProcess process,
                                   AbstractTask task,
                                   Consumer<MaudeRuleBuilder> ruleAdditions) {
        ruleBuilder.startRule(getFlowNodeRuleName(task) + END);

        String preTokens = getTokenForFlowNode(task) + ANY_OTHER_TOKENS;
        ruleBuilder.addPreObject(createProcessSnapshotObjectAnySubProcessAndMessages(process, preTokens));

        String postTokens = getOutgoingTokensForFlowNode(task) + ANY_OTHER_TOKENS;
        ruleBuilder.addPostObject(createProcessSnapshotObjectAnySubProcessAndMessages(process, postTokens));
        ruleAdditions.accept(ruleBuilder);

        ruleBuilder.buildRule();
    }

    public void createSendTaskRulesForProcess(AbstractProcess process, SendTask sendTask) {
        createTaskRulesForProcess(process,
                                  sendTask,
                                  maudeRuleBuilder -> addSendMessageBehaviorForFlowNode(collaboration, sendTask));
    }

    public void createReceiveTaskRulesForProcess(AbstractProcess process, ReceiveTask receiveTask) {
        // TODO: Boundary events
        if (receiveTask.isInstantiate()) {
            if (receiveTask.getIncomingFlows().findAny().isPresent()) {
                throw new BPMNRuntimeException("Instantiate receive tasks should not have incoming sequence " +
                                               "flows!");
            }
            return;
        }
        // Rules for starting the task
        createStartInteractionNodeRule(receiveTask, process);
        // Rule for ending the task (now consumes messages)
        createEndInteractionNodeRule(receiveTask, process, collaboration);
    }

    @Override
    public MaudeRuleBuilder getRuleBuilder() {
        return ruleBuilder;
    }

    @Override
    public MaudeObjectBuilder getObjectBuilder() {
        return objectBuilder;
    }
}
