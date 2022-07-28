package maude.behaviortransformer.bpmn.generators;

import behavior.bpmn.AbstractProcess;
import behavior.bpmn.BPMNCollaboration;
import behavior.bpmn.SequenceFlow;
import behavior.bpmn.activities.tasks.AbstractTask;
import behavior.bpmn.activities.tasks.ReceiveTask;
import behavior.bpmn.activities.tasks.SendTask;
import maude.generation.MaudeObjectBuilder;
import maude.generation.MaudeRuleBuilder;

import java.util.function.Consumer;

import static groove.behaviortransformer.bpmn.BPMNToGrooveTransformerConstants.END;
import static groove.behaviortransformer.bpmn.BPMNToGrooveTransformerConstants.START;
import static maude.behaviortransformer.bpmn.BPMNToMaudeTransformerHelper.*;

public class BPMNMaudeTaskRuleGenerator {
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
        ruleBuilder.ruleName(getFlowNodeRuleNameWithIncFlow(task, incomingFlow.getId()) + START);

        String preTokens = getTokenForSequenceFlow(incomingFlow) + ANY_OTHER_TOKENS;
        ruleBuilder.addPreObject(createProcessSnapshotObjectAnySubProcess(objectBuilder,
                                                                          process,
                                                                          preTokens,
                                                                          getIncomingMessagesForFlowNode(task,
                                                                                                         collaboration)));

        String postTokens = getTokenForActivity(task) + ANY_OTHER_TOKENS;
        ruleBuilder.addPostObject(createProcessSnapshotObjectAnySubProcessAndMessages(objectBuilder,
                                                                                      process,
                                                                                      postTokens));

        ruleBuilder.build();
    }

    private void createEndTaskRule(AbstractProcess process,
                                   AbstractTask task,
                                   Consumer<MaudeRuleBuilder> ruleAdditions) {
        ruleBuilder.ruleName(getFlowNodeRuleName(task) + END);

        String preTokens = getTokenForActivity(task) + ANY_OTHER_TOKENS;
        ruleBuilder.addPreObject(createProcessSnapshotObjectAnySubProcessAndMessages(objectBuilder,
                                                                                     process,
                                                                                     preTokens));

        String postTokens = getOutgoingTokensForFlowNode(task) + ANY_OTHER_TOKENS;
        ruleBuilder.addPostObject(createProcessSnapshotObjectAnySubProcessAndMessages(objectBuilder,
                                                                                      process,
                                                                                      postTokens));
        ruleAdditions.accept(ruleBuilder);

        ruleBuilder.build();
    }

    public void createSendTaskRulesForProcess(AbstractProcess process, SendTask sendTask) {
        createTaskRulesForProcess(process,
                                  sendTask,
                                  maudeRuleBuilder -> addSendMessageBehaviorForFlowNode(collaboration,
                                                                                        maudeRuleBuilder,
                                                                                        objectBuilder,
                                                                                        sendTask));
    }

    public void createReceiveTaskRulesForProcess(AbstractProcess process, ReceiveTask receiveTask) {
        // TODO: Receive task instantiation rules (and evtl. boundary rules).
        // TODO: Receive task after instantiate event based gateways.
        // TODO: Receive task after event based gateways.
        createTaskRulesForProcess(process, receiveTask);
    }
}
