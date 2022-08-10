package maude.behaviortransformer.bpmn.generators;

import behavior.bpmn.Process;
import behavior.bpmn.*;
import behavior.bpmn.auxiliary.AbstractProcessVisitor;
import behavior.bpmn.auxiliary.exceptions.BPMNRuntimeException;
import behavior.bpmn.events.*;
import maude.behaviortransformer.bpmn.BPMNToMaudeTransformerHelper;
import maude.generation.BPMNMaudeRuleBuilder;
import maude.generation.MaudeObjectBuilder;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Set;
import java.util.function.Consumer;

import static groove.behaviortransformer.bpmn.BPMNToGrooveTransformerHelper.isAfterInstantiateEventBasedGateway;

public class BPMNMaudeEventRuleGenerator implements BPMNToMaudeTransformerHelper {
    private final BPMNMaudeRuleBuilder ruleBuilder;
    private final BPMNCollaboration collaboration;
    private final MaudeObjectBuilder objectBuilder;

    public BPMNMaudeEventRuleGenerator(BPMNCollaboration collaboration, BPMNMaudeRuleBuilder ruleBuilder) {
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
                createEndInteractionNodeRule(startEvent, process, collaboration);
                break;
            case MESSAGE_NON_INTERRUPTING:
                // Implemented only in the event subprocess rule generator.
            case SIGNAL:
                // Done in the corresponding throw rule.
            case SIGNAL_NON_INTERRUPTING:
                // Implemented only in the event subprocess rule generator.
                break;
        }
    }

    private void createNoneStartEventRule(StartEvent startEvent, AbstractProcess process) {
        ruleBuilder.startRule(getFlowNodeRuleName(startEvent));
        String preToken = getStartEventTokenName(startEvent) + ANY_OTHER_TOKENS;
        String postToken = getOutgoingTokensForFlowNode(startEvent) + ANY_OTHER_TOKENS;
        ruleBuilder.addPreObject(createProcessSnapshotObjectAnySubProcess(process, preToken));
        ruleBuilder.addPostObject(createProcessSnapshotObjectAnySubProcess(process, postToken));
        ruleBuilder.buildRule();
    }

    public void createEndEventRule(AbstractProcess process, EndEvent endEvent) {
        if (endEvent.getIncomingFlows().count() != 1) {
            throw new BPMNRuntimeException("End events should have exactly one incoming flow!");
        }
        // Throw never reached due to check before.
        SequenceFlow incomingFlow = endEvent.getIncomingFlows().findFirst().orElseThrow();
        String preTokens = getTokenForSequenceFlow(incomingFlow) + ANY_OTHER_TOKENS;

        ruleBuilder.startRule(getFlowNodeRuleName(endEvent));
        ruleBuilder.addPreObject(createProcessSnapshotObjectAnySubProcess(process,
                                                                          preTokens));

        switch (endEvent.getType()) {
            case NONE:
                ruleBuilder.addPostObject(createProcessSnapshotObjectAnySubProcess(process,
                                                                                   ANY_TOKENS));
                break;
            case TERMINATION:
                ruleBuilder.addPostObject(createProcessSnapshotObject(process,
                                                                      String.format("terminate(%s)", ANY_SUBPROCESSES),
                                                                      ANY_TOKENS,
                                                                      TERMINATED));
                break;
            case MESSAGE:
                ruleBuilder.addPostObject(createProcessSnapshotObjectAnySubProcess(process,
                                                                                   ANY_TOKENS));
                addSendMessageBehaviorForFlowNode(collaboration, endEvent);
                break;
            case SIGNAL:
                ruleBuilder.addPostObject(createProcessSnapshotObjectAnySubProcess(process,
                                                                                   ANY_TOKENS));
                createSignalThrowRulePart(endEvent.getEventDefinition());
                break;
        }
        ruleBuilder.buildRule();
    }

    private void createSignalThrowRulePart(EventDefinition signalEventDefinition) {
        Pair<Set<Event>, Set<BoundaryEvent>> correspondingSignalCatchEvents =
                collaboration.findAllCorrespondingSignalCatchEvents(signalEventDefinition);

        correspondingSignalCatchEvents.getLeft().forEach(this::createCatchSignalEventRulePart);
        correspondingSignalCatchEvents.getRight().forEach(this::createBoundarySignalCatchEventRulePart);
    }

    private void createBoundarySignalCatchEventRulePart(BoundaryEvent signalBoundaryEvent) {
        // TODO: Implement.
    }

    private void createCatchSignalEventRulePart(Event catchSignalEvent) {
        final AbstractProcess processForEvent = collaboration.findProcessForFlowNode(catchSignalEvent);
        if (catchSignalEvent.isInstantiateFlowNode() || isAfterInstantiateEventBasedGateway(catchSignalEvent)) {
            createSignalThrowInstantiateRulePart(catchSignalEvent, processForEvent);
        } else {
            // Send a signal to all existing processes
            // TODO: Must not be more than one inflow!
            // TODO: Implement forAll vacious behavior.
            catchSignalEvent.getIncomingFlows().forEach(inFlow -> {
                String token;
                if (inFlow.getSource().isExclusiveEventBasedGateway()) {
                    token = getTokenForFlowNode(inFlow.getSource());
                } else {
                    token = getTokenForSequenceFlow(inFlow);
                }
                ruleBuilder.addPreObject(createProcessSnapshotObjectAnySubProcess(processForEvent, token + ANY_OTHER_TOKENS));

                String postTokens = getOutgoingTokensForFlowNode(catchSignalEvent);
                ruleBuilder.addPostObject(createProcessSnapshotObjectAnySubProcess(processForEvent, postTokens + ANY_OTHER_TOKENS));
            });
        }
    }

    private void createSignalThrowInstantiateRulePart(Event event, AbstractProcess processForEvent) {
        processForEvent.accept(new AbstractProcessVisitor() {
            @Override
            public void handle(EventSubprocess eventSubprocess) {
                createSignalThrowInstantiateRulePartForEventSubprocess(eventSubprocess, event);
            }

            @Override
            public void handle(Process process) {
                // Create a new process instance.
                String startTokens = getOutgoingTokensForFlowNode(event);
                ruleBuilder.addPostObject(createProcessSnapshotObjectNoSubProcess(processForEvent, startTokens));
            }
        });
    }

    private void createSignalThrowInstantiateRulePartForEventSubprocess(EventSubprocess eventSubprocess, Event event) {
        // TODO: Implement event subprocesses!
    }

    public void createIntermediateCatchEventRule(AbstractProcess process,
                                                 IntermediateCatchEvent intermediateCatchEvent) {
        switch (intermediateCatchEvent.getType()) {
            case LINK:
                createIntermediateCatchLinkEventRule(intermediateCatchEvent, process);
                break;
            case MESSAGE:
                createIntermediateCatchMessageEventRule(intermediateCatchEvent, process, collaboration);
                break;
            case SIGNAL:
                // Done in the corresponding throw rule.
                break;
            case TIMER:
                // Same behavior as a none event so far. No timings implemented.
                createIntermediateThrowNoneEventRule(intermediateCatchEvent, process);
                break;
        }
    }

    private void createIntermediateCatchLinkEventRule(IntermediateCatchEvent linkCatchEvent,
                                                      AbstractProcess process) {
        ruleBuilder.startRule(getFlowNodeRuleName(linkCatchEvent));

        // Consume an incoming token from an incoming flow.
        String preTokens = String.format(ENQUOTE_FORMAT, linkCatchEvent.getName()) + ANY_OTHER_TOKENS;
        ruleBuilder.addPreObject(createProcessSnapshotObjectAnySubProcess(process,
                                                                          preTokens));
        // Produce a token for each outgoing flow.
        String postTokens = getOutgoingTokensForFlowNode(linkCatchEvent) + ANY_OTHER_TOKENS;
        ruleBuilder.addPostObject(createProcessSnapshotObjectAnySubProcess(process,
                                                                           postTokens));
        ruleBuilder.buildRule();
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
                createIntermediateThrowLinkEventRule(intermediateThrowEvent, process);
                break;
            case MESSAGE:
                createIntermediateThrowMessageEventRule(intermediateThrowEvent, process);
                break;
            case SIGNAL:
                createIntermediateThrowSignalEventRule(intermediateThrowEvent, process);
                break;
            default:
                throw new BPMNRuntimeException("Unexpected throw event type: " + intermediateThrowEvent.getType());
        }
    }

    private void createIntermediateThrowSignalEventRule(IntermediateThrowEvent intermediateThrowEvent,
                                                        AbstractProcess process) {
        intermediateThrowEvent.getIncomingFlows().forEach(incFlow -> {
            ruleBuilder.startRule(getFlowNodeRuleNameWithIncFlow(intermediateThrowEvent, incFlow.getId()));

            // Consume an incoming token from an incoming flow.
            String preTokens = getTokenForSequenceFlow(incFlow) + ANY_OTHER_TOKENS;
            ruleBuilder.addPreObject(createProcessSnapshotObjectAnySubProcess(process,
                                                                              preTokens));
            // Produce a token for each outgoing flow.
            String postTokens = getOutgoingTokensForFlowNode(intermediateThrowEvent) + ANY_OTHER_TOKENS;
            ruleBuilder.addPostObject(createProcessSnapshotObjectAnySubProcess(process,
                                                                               postTokens));
            createSignalThrowRulePart(intermediateThrowEvent.getEventDefinition());
            ruleBuilder.buildRule();
        });

    }

    private void createIntermediateThrowLinkEventRule(IntermediateThrowEvent intermediateThrowEvent,
                                                      AbstractProcess process) {
        intermediateThrowEvent.getIncomingFlows().forEach(incFlow -> {
            ruleBuilder.startRule(getFlowNodeRuleNameWithIncFlow(intermediateThrowEvent, incFlow.getId()));

            // Consume an incoming token from an incoming flow.
            String preTokens = getTokenForSequenceFlow(incFlow) + ANY_OTHER_TOKENS;
            ruleBuilder.addPreObject(createProcessSnapshotObjectAnySubProcess(process,
                                                                              preTokens));
            // Produce a token for each outgoing flow.
            String postTokens = String.format(ENQUOTE_FORMAT, intermediateThrowEvent.getName()) + ANY_OTHER_TOKENS;
            ruleBuilder.addPostObject(createProcessSnapshotObjectAnySubProcess(process,
                                                                               postTokens));
            ruleBuilder.buildRule();
        });
    }

    private void createIntermediateThrowMessageEventRule(IntermediateThrowEvent intermediateThrowEvent,
                                                         AbstractProcess process) {
        createConsumeAndProduceTokenRule(intermediateThrowEvent,
                                         process,
                                         rb -> addSendMessageBehaviorForFlowNode(collaboration,
                                                                                 intermediateThrowEvent));
    }

    private void createIntermediateThrowNoneEventRule(Event intermediateThrowEvent,
                                                      AbstractProcess process) {
        createConsumeAndProduceTokenRule(intermediateThrowEvent, process, x -> {
            // NOOP
        });
    }

    private void createConsumeAndProduceTokenRule(Event intermediateThrowEvent,
                                                  AbstractProcess process,
                                                  Consumer<BPMNMaudeRuleBuilder> ruleExtender) {
        intermediateThrowEvent.getIncomingFlows().forEach(incFlow -> {
            ruleBuilder.startRule(getFlowNodeRuleNameWithIncFlow(intermediateThrowEvent, incFlow.getId()));

            // Consume an incoming token from an incoming flow.
            String preTokens = getTokenForSequenceFlow(incFlow) + ANY_OTHER_TOKENS;
            ruleBuilder.addPreObject(createProcessSnapshotObjectAnySubProcess(process,
                                                                              preTokens));
            // Produce a token for each outgoing flow.
            String postTokens = getOutgoingTokensForFlowNode(intermediateThrowEvent) + ANY_OTHER_TOKENS;
            ruleBuilder.addPostObject(createProcessSnapshotObjectAnySubProcess(process,
                                                                               postTokens));
            ruleExtender.accept(ruleBuilder);
            ruleBuilder.buildRule();
        });
    }

    @Override
    public BPMNMaudeRuleBuilder getRuleBuilder() {
        return ruleBuilder;
    }

    @Override
    public MaudeObjectBuilder getObjectBuilder() {
        return objectBuilder;
    }
}
