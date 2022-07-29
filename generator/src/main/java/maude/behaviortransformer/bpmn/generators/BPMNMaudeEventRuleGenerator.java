package maude.behaviortransformer.bpmn.generators;

import behavior.bpmn.AbstractProcess;
import behavior.bpmn.BPMNCollaboration;
import behavior.bpmn.SequenceFlow;
import behavior.bpmn.auxiliary.exceptions.BPMNRuntimeException;
import behavior.bpmn.events.EndEvent;
import behavior.bpmn.events.IntermediateCatchEvent;
import behavior.bpmn.events.IntermediateThrowEvent;
import behavior.bpmn.events.StartEvent;
import maude.behaviortransformer.bpmn.BPMNToMaudeTransformerHelper;
import maude.generation.MaudeObjectBuilder;
import maude.generation.MaudeRuleBuilder;

import java.util.function.Consumer;

public class BPMNMaudeEventRuleGenerator implements BPMNToMaudeTransformerHelper {
    private final MaudeRuleBuilder ruleBuilder;
    private final BPMNCollaboration collaboration;
    private final MaudeObjectBuilder objectBuilder;

    public BPMNMaudeEventRuleGenerator(BPMNCollaboration collaboration, MaudeRuleBuilder ruleBuilder) {
        this.collaboration = collaboration;
        this.ruleBuilder = ruleBuilder;
        this.objectBuilder = new MaudeObjectBuilder();
    }

    public void createStartEventRulesForProcess(AbstractProcess process, StartEvent startEvent) {
        switch (startEvent.getType()) {
            case NONE:
                createNoneStartEventRule(startEvent, process);
                break;
            case MESSAGE:
                // Done in the corresponding throw rule.
            case MESSAGE_NON_INTERRUPTING:
                // Implemented in the event subprocess rule generator.
            case SIGNAL:
                // Done in the corresponding throw rule.
            case SIGNAL_NON_INTERRUPTING:
                // Implemented in the throw part.
                break;
        }
    }

    private void createNoneStartEventRule(StartEvent startEvent, AbstractProcess process) {
        ruleBuilder.ruleName(getFlowNodeRuleName(startEvent));
        String preToken = getStartEventTokenName(startEvent) + ANY_OTHER_TOKENS;
        String postToken = getOutgoingTokensForFlowNode(startEvent) + ANY_OTHER_TOKENS;
        ruleBuilder.addPreObject(createProcessSnapshotObjectAnySubProcessAndMessages(process, preToken));
        ruleBuilder.addPostObject(createProcessSnapshotObjectAnySubProcessAndMessages(process, postToken));
        ruleBuilder.build();
    }

    public void createEndEventRule(AbstractProcess process, EndEvent endEvent) {
        if (endEvent.getIncomingFlows().count() != 1) {
            throw new BPMNRuntimeException("End events should have exactly one incoming flow!");
        }
        // Throw never reached due to check before.
        SequenceFlow incomingFlow = endEvent.getIncomingFlows().findFirst().orElseThrow();
        String preTokens = getTokenForSequenceFlow(incomingFlow) + ANY_OTHER_TOKENS;

        ruleBuilder.ruleName(getFlowNodeRuleName(endEvent));
        ruleBuilder.addPreObject(createProcessSnapshotObjectAnySubProcessAndMessages(process,
                                                                                     preTokens));

        switch (endEvent.getType()) {
            case NONE:
                ruleBuilder.addPostObject(createProcessSnapshotObjectAnySubProcessAndMessages(process,
                                                                                              ANY_TOKENS));
                break;
            case TERMINATION:
                // TODO: Implement termination end event.
                break;
            case MESSAGE:
                ruleBuilder.addPostObject(createProcessSnapshotObjectAnySubProcessAndMessages(process,
                                                                                              ANY_TOKENS));
                addSendMessageBehaviorForFlowNode(collaboration, endEvent);
                break;
            case SIGNAL:
                // TODO: Implement signal end event.
                break;
        }
        ruleBuilder.build();
    }

    public void createIntermediateCatchEventRule(AbstractProcess process,
                                                 IntermediateCatchEvent intermediateCatchEvent) {
        switch (intermediateCatchEvent.getType()) {
            case LINK:
                // TODO: Link events
                throw new UnsupportedOperationException();
            case MESSAGE:
                createIntermediateCatchMessageEventRule(intermediateCatchEvent, process, collaboration);
                break;
            case SIGNAL:
                // TODO: Signal events
                throw new UnsupportedOperationException();
                // Done in the corresponding throw rule.
            case TIMER:
                // TODO: Timer events
                throw new UnsupportedOperationException();
        }
    }

    private void createIntermediateCatchMessageEventRule(IntermediateCatchEvent intermediateCatchEvent,
                                                         AbstractProcess process,
                                                         BPMNCollaboration collaboration) {
        // Start event rule
        createStartInteractionNodeRule(intermediateCatchEvent, process);

        // Rule to end the event when a message is received.
        createEndInteractionNodeRule(intermediateCatchEvent, process, collaboration);
    }

    public void createIntermediateThrowEventRule(AbstractProcess process,
                                                 IntermediateThrowEvent intermediateThrowEvent) {
        // We currently limit to one incoming token, but we could implement an implicit exclusive gateway.
        if (intermediateThrowEvent.getIncomingFlows().count() != 1) {
            throw new BPMNRuntimeException("Intermediate throw events should have exactly one incoming sequence flow!");
        }
        switch (intermediateThrowEvent.getType()) {
            case NONE:
                createIntermediateThrowNoneEventRule(intermediateThrowEvent, process);
                break;
            case LINK:
                // TODO: Implement Link throw events.
                break;
            case MESSAGE:
                createIntermediateThrowMessageEventRule(intermediateThrowEvent, process);
                break;
            case SIGNAL:
                // TODO: Implement signal throw events.
                break;
            default:
                throw new BPMNRuntimeException("Unexpected throw event type: " + intermediateThrowEvent.getType());
        }
    }

    private void createIntermediateThrowMessageEventRule(IntermediateThrowEvent intermediateThrowEvent,
                                                         AbstractProcess process) {
        createConsumeAndProduceTokenRule(intermediateThrowEvent,
                                         process,
                                         rb -> addSendMessageBehaviorForFlowNode(collaboration,
                                                                                 intermediateThrowEvent));
    }

    private void createIntermediateThrowNoneEventRule(IntermediateThrowEvent intermediateThrowEvent,
                                                      AbstractProcess process) {
        createConsumeAndProduceTokenRule(intermediateThrowEvent, process, x -> {
            // NOOP
        });
    }

    private void createConsumeAndProduceTokenRule(IntermediateThrowEvent intermediateThrowEvent,
                                                  AbstractProcess process,
                                                  Consumer<MaudeRuleBuilder> ruleExtender) {
        intermediateThrowEvent.getIncomingFlows().forEach(incFlow -> {
            ruleBuilder.ruleName(getFlowNodeRuleNameWithIncFlow(intermediateThrowEvent, incFlow.getId()));

            // Consume an incoming token from an incoming flow.
            String preTokens = getTokenForSequenceFlow(incFlow) + ANY_OTHER_TOKENS;
            ruleBuilder.addPreObject(createProcessSnapshotObjectAnySubProcessAndMessages(process,
                                                                                         preTokens));
            // Produce a token for each outgoing flow.
            String postTokens = getOutgoingTokensForFlowNode(intermediateThrowEvent) + ANY_OTHER_TOKENS;
            ruleBuilder.addPostObject(createProcessSnapshotObjectAnySubProcessAndMessages(process,
                                                                                          postTokens));
            ruleExtender.accept(ruleBuilder);
            ruleBuilder.build();
        });
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
