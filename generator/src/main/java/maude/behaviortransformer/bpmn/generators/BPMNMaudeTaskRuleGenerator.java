package maude.behaviortransformer.bpmn.generators;

import behavior.bpmn.AbstractProcess;
import behavior.bpmn.BPMNCollaboration;
import behavior.bpmn.SequenceFlow;
import behavior.bpmn.activities.tasks.AbstractTask;
import behavior.bpmn.activities.tasks.ReceiveTask;
import behavior.bpmn.activities.tasks.SendTask;
import behavior.bpmn.auxiliary.exceptions.BPMNRuntimeException;
import behavior.bpmn.events.BoundaryEvent;
import maude.behaviortransformer.bpmn.BPMNToMaudeTransformerHelper;
import maude.generation.BPMNMaudeRuleBuilder;
import maude.generation.MaudeObjectBuilder;

import java.util.function.Consumer;

import static groove.behaviortransformer.bpmn.BPMNToGrooveTransformerConstants.END;
import static groove.behaviortransformer.bpmn.BPMNToGrooveTransformerConstants.START;

public class BPMNMaudeTaskRuleGenerator implements BPMNToMaudeTransformerHelper {
    private final BPMNCollaboration collaboration;
    private final BPMNMaudeRuleBuilder ruleBuilder;
    private final MaudeObjectBuilder objectBuilder;

    public BPMNMaudeTaskRuleGenerator(BPMNCollaboration collaboration, BPMNMaudeRuleBuilder ruleBuilder) {
        this.collaboration = collaboration;
        this.ruleBuilder = ruleBuilder;
        this.objectBuilder = new MaudeObjectBuilder();
    }

    public void createTaskRulesForProcess(AbstractProcess process,
                                          AbstractTask task,
                                          Consumer<BPMNMaudeRuleBuilder> endTaskRuleAdditions) {
        // Rules for starting the task
        task.getIncomingFlows().forEach(incomingFlow -> createStartTaskRule(process, task, incomingFlow));
        // Rule for ending the task
        createEndTaskRule(process, task, endTaskRuleAdditions);

        // Generate rules for boundary events
        this.createBoundaryEventRules(process, task, collaboration);
    }

    private void createBoundaryEventRules(AbstractProcess process, AbstractTask task, BPMNCollaboration collaboration) {
        task.getBoundaryEvents().forEach(boundaryEvent -> {
            switch (boundaryEvent.getType()) {
                case NONE:
                case TIMER:
                    createTaskBoundaryEventRule(process, task, boundaryEvent, rb -> {
                    }); // NOOP
                    break;
                case MESSAGE:
                    createTaskMessageBoundaryEventRule(process, task, boundaryEvent, collaboration);
                    break;
                case SIGNAL:
                    // Handled in the throw rule part.
                    break;
                default:
                    throw new IllegalStateException("Unexpected value: " + boundaryEvent.getType());
            }
        });
    }

    private void createTaskMessageBoundaryEventRule(AbstractProcess process,
                                                    AbstractTask task,
                                                    BoundaryEvent boundaryEvent,
                                                    BPMNCollaboration collaboration) {
        collaboration.getIncomingMessageFlows(boundaryEvent).forEach(messageFlow -> createTaskBoundaryEventRule(
                process,
                task,
                boundaryEvent,
                maudeRuleBuilder -> addMessageConsumption(messageFlow)));
    }

    private void createTaskBoundaryEventRule(AbstractProcess process,
                                             AbstractTask task,
                                             BoundaryEvent boundaryEvent,
                                             Consumer<BPMNMaudeRuleBuilder> ruleAddditions) {
        ruleBuilder.startRule(getFlowNodeRuleName(boundaryEvent));
        ruleAddditions.accept(getRuleBuilder());

        String taskToken = getTokenForFlowNode(task);
        ruleBuilder.addPreObject(createProcessSnapshotObjectAnySubProcessAndSignals(process,
                                                                          taskToken +
                                                                          ANY_OTHER_TOKENS));
        String postTokens;
        if (boundaryEvent.isInterrupt()) {
            // Add outgoing tokens and not the task token.
            postTokens = getOutgoingTokensForFlowNode(boundaryEvent) + ANY_OTHER_TOKENS;
        } else {
            // Add outgoing tokens alongside the task token.
            postTokens = getOutgoingTokensForFlowNode(boundaryEvent) +
                         WHITE_SPACE +
                         taskToken +
                         ANY_OTHER_TOKENS;
        }
        ruleBuilder.addPostObject(createProcessSnapshotObjectAnySubProcessAndNoSignals(process, postTokens));
        ruleBuilder.buildRule();
    }

    public void createTaskRulesForProcess(AbstractProcess process, AbstractTask task) {
        createTaskRulesForProcess(process, task, x -> {
            // NOOP
        });
    }

    private void createStartTaskRule(AbstractProcess process, AbstractTask task, SequenceFlow incomingFlow) {
        ruleBuilder.startRule(getFlowNodeRuleNameWithIncFlow(task, incomingFlow.getId()) + START);

        String preTokens = getTokenForSequenceFlow(incomingFlow) + ANY_OTHER_TOKENS;
        ruleBuilder.addPreObject(createProcessSnapshotObjectAnySubProcessAndSignals(process, preTokens));

        String postTokens = getTokenForFlowNode(task) + ANY_OTHER_TOKENS;
        ruleBuilder.addPostObject(createProcessSnapshotObjectAnySubProcessAndNoSignals(process, postTokens));

        ruleBuilder.buildRule();
    }

    private void createEndTaskRule(AbstractProcess process,
                                   AbstractTask task,
                                   Consumer<BPMNMaudeRuleBuilder> ruleAdditions) {
        ruleBuilder.startRule(getFlowNodeRuleName(task) + END);

        String preTokens = getTokenForFlowNode(task) + ANY_OTHER_TOKENS;
        ruleBuilder.addPreObject(createProcessSnapshotObjectAnySubProcessAndSignals(process, preTokens));

        String postTokens = getOutgoingTokensForFlowNode(task) + ANY_OTHER_TOKENS;
        ruleBuilder.addPostObject(createProcessSnapshotObjectAnySubProcessAndNoSignals(process, postTokens));
        ruleAdditions.accept(ruleBuilder);

        ruleBuilder.buildRule();
    }

    public void createSendTaskRulesForProcess(AbstractProcess process, SendTask sendTask) {
        createTaskRulesForProcess(process,
                                  sendTask,
                                  maudeRuleBuilder -> addSendMessageBehaviorForFlowNode(collaboration, sendTask));
    }

    public void createReceiveTaskRulesForProcess(AbstractProcess process, ReceiveTask receiveTask) {
        this.createBoundaryEventRules(process, receiveTask, collaboration);

        if (receiveTask.isInstantiate() && receiveTask.getIncomingFlows().findAny().isPresent()) {
            throw new BPMNRuntimeException("Instantiate receive tasks should not have incoming sequence " +
                                           "flows!");
        } else {
            // Rules for starting the task
            createStartInteractionNodeRule(receiveTask, process);
        }
        // Rule for ending the task (now consumes messages)
        createEndInteractionNodeRule(receiveTask, process, collaboration);
    }

    @Override
    public BPMNMaudeRuleBuilder getRuleBuilder() {
        return ruleBuilder;
    }

    @Override
    public MaudeObjectBuilder getObjectBuilder() {
        return objectBuilder;
    }

    @Override
    public BPMNCollaboration getCollaboration() {
        return collaboration;
    }
}
