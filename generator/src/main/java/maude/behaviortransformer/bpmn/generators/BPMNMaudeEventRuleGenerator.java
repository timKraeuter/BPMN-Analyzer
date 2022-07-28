package maude.behaviortransformer.bpmn.generators;

import behavior.bpmn.AbstractProcess;
import behavior.bpmn.BPMNCollaboration;
import behavior.bpmn.SequenceFlow;
import behavior.bpmn.auxiliary.exceptions.BPMNRuntimeException;
import behavior.bpmn.events.EndEvent;
import behavior.bpmn.events.IntermediateCatchEvent;
import behavior.bpmn.events.StartEvent;
import maude.generation.MaudeObjectBuilder;
import maude.generation.MaudeRuleBuilder;

import static maude.behaviortransformer.bpmn.BPMNToMaudeTransformerHelper.*;

public class BPMNMaudeEventRuleGenerator {
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
                createMessageStartEventRule(startEvent);
                break;
            case MESSAGE_NON_INTERRUPTING:
                // Implemented in the event subprocess rule generator.
                break;
            case SIGNAL:
                // Done in the corresponding throw rule.
                break;
            case SIGNAL_NON_INTERRUPTING:
                // Implemented in the throw part.
                break;
        }
    }

    private void createNoneStartEventRule(StartEvent startEvent, AbstractProcess process) {
        ruleBuilder.ruleName(getFlowNodeRuleName(startEvent));
        String preToken = getStartEventTokenName(startEvent) + ANY_OTHER_TOKENS;
        String postToken = getOutgoingTokensForFlowNode(startEvent) + ANY_OTHER_TOKENS;
        ruleBuilder.addPreObject(createProcessSnapshotObjectAnySubProcessAndMessages(objectBuilder, process, preToken));
        ruleBuilder.addPostObject(createProcessSnapshotObjectAnySubProcessAndMessages(objectBuilder,
                                                                                      process,
                                                                                      postToken));
        ruleBuilder.build();
    }

    private void createMessageStartEventRule(StartEvent startEvent) {
        throw new UnsupportedOperationException();
    }

    public void createEndEventRule(AbstractProcess process, EndEvent endEvent) {
        if (endEvent.getIncomingFlows().count() != 1) {
            throw new BPMNRuntimeException("End events should have exactly one incoming flow!");
        }
        // Throw never reached due to check before.
        SequenceFlow incomingFlow = endEvent.getIncomingFlows().findFirst().orElseThrow();
        String preTokens = getTokenForSequenceFlow(incomingFlow) + ANY_OTHER_TOKENS;

        ruleBuilder.ruleName(getFlowNodeRuleName(endEvent));
        ruleBuilder.addPreObject(createProcessSnapshotObjectAnySubProcessAndMessages(objectBuilder,
                                                                                     process,
                                                                                     preTokens));

        switch (endEvent.getType()) {
            case NONE:
                ruleBuilder.addPostObject(createProcessSnapshotObjectAnySubProcessAndMessages(objectBuilder, process,
                                                                                              ANY_TOKENS));
                break;
            case TERMINATION:
                // TODO: Implement termination end event.
                break;
            case MESSAGE:
                ruleBuilder.addPostObject(createProcessSnapshotObjectAnySubProcessAndMessages(objectBuilder, process,
                                                                                              ANY_TOKENS));
                addSendMessageBehaviorForFlowNode(collaboration, ruleBuilder, objectBuilder, endEvent);
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
                createIntermediateCatchMessageEventRule(intermediateCatchEvent, process, ruleBuilder, collaboration);
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
                                                         MaudeRuleBuilder ruleBuilder,
                                                         BPMNCollaboration collaboration) {
        intermediateCatchEvent.getIncomingFlows().forEach(incomingFlow -> {
            ruleBuilder.ruleName(getFlowNodeRuleNameWithIncFlow(intermediateCatchEvent, incomingFlow.getId()));

            String preTokens = getTokenForSequenceFlow(incomingFlow) + ANY_OTHER_TOKENS;
            String messages = getIncomingMessagesForFlowNode(intermediateCatchEvent, collaboration);
            ruleBuilder.addPreObject(createProcessSnapshotObjectAnySubProcess(objectBuilder,
                                                                              process,
                                                                              preTokens,
                                                                              messages));

            String postTokens = getOutgoingTokensForFlowNode(intermediateCatchEvent) + ANY_OTHER_TOKENS;
            ruleBuilder.addPostObject(createProcessSnapshotObjectAnySubProcessAndMessages(objectBuilder,
                                                                                          process,
                                                                                          postTokens));
            ruleBuilder.build();
        });

    }
}
