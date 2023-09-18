package no.tk.maude.behaviortransformer.bpmn.generators;

import static no.tk.maude.behaviortransformer.bpmn.BPMNToMaudeTransformerConstants.ANY_OTHER_SIGNALS;
import static no.tk.maude.behaviortransformer.bpmn.BPMNToMaudeTransformerConstants.ANY_OTHER_TOKENS;
import static no.tk.maude.behaviortransformer.bpmn.BPMNToMaudeTransformerConstants.ANY_SUBPROCESSES;
import static no.tk.maude.behaviortransformer.bpmn.BPMNToMaudeTransformerConstants.ANY_TOKENS;
import static no.tk.maude.behaviortransformer.bpmn.BPMNToMaudeTransformerConstants.NONE;
import static no.tk.maude.behaviortransformer.bpmn.BPMNToMaudeTransformerConstants.TERMINATED;
import static no.tk.maude.behaviortransformer.bpmn.BPMNToMaudeTransformerConstants.WHITE_SPACE;

import no.tk.behavior.bpmn.AbstractBPMNProcess;
import no.tk.behavior.bpmn.BPMNCollaboration;
import no.tk.behavior.bpmn.SequenceFlow;
import no.tk.behavior.bpmn.auxiliary.exceptions.BPMNRuntimeException;
import no.tk.behavior.bpmn.events.BoundaryEvent;
import no.tk.behavior.bpmn.events.EndEvent;
import no.tk.behavior.bpmn.events.Event;
import no.tk.behavior.bpmn.events.IntermediateCatchEvent;
import no.tk.behavior.bpmn.events.IntermediateThrowEvent;
import no.tk.behavior.bpmn.events.StartEvent;
import no.tk.behavior.bpmn.events.definitions.EventDefinition;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;
import no.tk.maude.behaviortransformer.bpmn.BPMNMaudeRuleGenerator;
import no.tk.maude.behaviortransformer.bpmn.BPMNToMaudeTransformerHelper;
import no.tk.maude.behaviortransformer.bpmn.settings.MaudeBPMNGenerationSettings;
import no.tk.maude.generation.BPMNMaudeRuleBuilder;
import no.tk.maude.generation.MaudeObjectBuilder;
import no.tk.groove.behaviortransformer.bpmn.BPMNToGrooveTransformerHelper;
import org.apache.commons.lang3.tuple.Pair;

public class BPMNMaudeEventRuleGenerator implements BPMNToMaudeTransformerHelper {

  private final BPMNMaudeRuleGenerator ruleGenerator;
  private final BPMNMaudeRuleBuilder ruleBuilder;
  private final MaudeObjectBuilder objectBuilder;

  public BPMNMaudeEventRuleGenerator(
      BPMNMaudeRuleGenerator ruleGenerator, BPMNMaudeRuleBuilder ruleBuilder) {
    this.ruleGenerator = ruleGenerator;
    this.ruleBuilder = ruleBuilder;
    this.objectBuilder = new MaudeObjectBuilder();
  }

  public void createStartEventRulesForProcess(AbstractBPMNProcess process, StartEvent startEvent) {
    switch (startEvent.getType()) {
      case NONE:
        break;
      case MESSAGE:
        createEndInteractionNodeRule(startEvent, process);
        break;
      case SIGNAL, ERROR, ESCALATION:
        // Done in the corresponding throw rule.
        break;
    }
  }

  public void createEndEventRule(AbstractBPMNProcess process, EndEvent endEvent) {
    if (endEvent.getIncomingFlows().count() != 1) {
      throw new BPMNRuntimeException("End events should have exactly one incoming flow!");
    }
    // Throw never reached due to check before.
    SequenceFlow incomingFlow = endEvent.getIncomingFlows().findFirst().orElseThrow();
    String preTokens = getTokenForSequenceFlow(incomingFlow) + ANY_OTHER_TOKENS;

    ruleBuilder.startRule(getFlowNodeRuleName(endEvent));

    switch (endEvent.getType()) {
      case NONE:
        createPreAndPostObjectInRuleForProcess(process, preTokens, ANY_TOKENS);
        break;
      case TERMINATION:
        createTerminationEndEventRule(process, preTokens);
        break;
      case MESSAGE:
        createMessageEndEventRule(process, endEvent, preTokens);
        break;
      case ERROR:
        // TODO: Implement Error end events!
        break;
      case ESCALATION:
        // TODO: Implement Escalation end events!
        break;
      case SIGNAL:
        createSignalEndEventRule(process, endEvent, preTokens);
        break;
    }
    ruleBuilder.buildRule();
  }

  private void createSignalEndEventRule(
      AbstractBPMNProcess process, EndEvent endEvent, String preTokens) {
    ruleBuilder.addPreObject(
        createProcessSnapshotObjectWithParents(process, ANY_SUBPROCESSES, preTokens));
    ruleBuilder.addPostObject(
        createProcessSnapshotObjectWithParents(process, ANY_SUBPROCESSES, ANY_TOKENS));
    createSignalThrowRulePart(endEvent, endEvent.getEventDefinition());
  }

  private void createMessageEndEventRule(
      AbstractBPMNProcess process, EndEvent endEvent, String preTokens) {
    ruleBuilder.addPreObject(
        createProcessSnapshotObjectWithParents(process, ANY_SUBPROCESSES, preTokens));
    ruleBuilder.addPostObject(
        createProcessSnapshotObjectWithParents(process, ANY_SUBPROCESSES, ANY_TOKENS));
    addSendMessageBehaviorForFlowNode(endEvent);
  }

  private void createTerminationEndEventRule(AbstractBPMNProcess process, String preTokens) {
    ruleBuilder.addPreObject(
        createProcessSnapshotObjectAnySubProcessAndSignals(process, preTokens));
    ruleBuilder.addPostObject(
        createProcessSnapshotObject(
            process,
            String.format("terminate(%s)", ANY_SUBPROCESSES),
            ANY_TOKENS,
            NONE,
            TERMINATED));
  }

  private void createPreAndPostObjectInRuleForProcess(
      AbstractBPMNProcess process, String preTokens, String postTokens) {
    ruleBuilder.addPreObject(
        createProcessSnapshotObjectAnySubProcessAndSignals(process, preTokens));
    ruleBuilder.addPostObject(
        createProcessSnapshotObjectAnySubProcessAndNoSignals(process, postTokens));
  }

  private void createSignalThrowRulePart(
          Event signalThrowEvent, EventDefinition signalEventDefinition) {
    Pair<Set<Event>, Set<BoundaryEvent>> correspondingSignalCatchEvents =
        getCollaboration().findAllCorrespondingCatchEvents(signalEventDefinition);

    Set<String> signalAllTokens = new LinkedHashSet<>();
    correspondingSignalCatchEvents.getLeft().stream()
        .flatMap(
            event -> {
              if (event.getIncomingFlows().findAny().isEmpty()) {
                // Start events.
                createSignalStartEventRulePart(event);
              }
              return event.getIncomingFlows();
            })
        .filter(
            inFlow ->
                signalThrowEvent.getOutgoingFlows().noneMatch(outFlow -> outFlow.equals(inFlow)))
        .map(
            sequenceFlow -> {
              if (sequenceFlow.getSource().isExclusiveEventBasedGateway()) {
                return getTokenForFlowNode(sequenceFlow.getSource());
              }
              return getTokenForSequenceFlow(sequenceFlow);
            })
        .forEach(signalAllTokens::add);
    correspondingSignalCatchEvents.getRight().stream()
        .map(boundaryEvent -> getTokenForFlowNode(boundaryEvent.getAttachedTo()))
        .forEach(signalAllTokens::add);
    ruleBuilder.addSignalAll(signalAllTokens);
  }

  private void createSignalStartEventRulePart(Event event) {
    String startTokens = getOutgoingTokensForFlowNode(event);
    final AbstractBPMNProcess processForEvent = getCollaboration().findProcessForFlowNode(event);
    ruleBuilder.addPostObject(
        createProcessSnapshotObjectNoSubProcessAndSignals(processForEvent, startTokens));
  }

  public void createIntermediateCatchEventRule(
      AbstractBPMNProcess process, IntermediateCatchEvent intermediateCatchEvent) {
    switch (intermediateCatchEvent.getType()) {
      case LINK:
        // No catch rule needed since tokens are teleported when the link event happens.
        break;
      case MESSAGE:
        createIntermediateCatchMessageEventRule(intermediateCatchEvent, process);
        break;
      case SIGNAL:
        createIntermediateSignalCatchEventRule(intermediateCatchEvent, process);
        break;
      case TIMER:
        // Same behavior as a none event so far. No timings implemented.
        createIntermediateThrowNoneEventRule(intermediateCatchEvent, process);
        break;
    }
  }

  private void createIntermediateSignalCatchEventRule(
      IntermediateCatchEvent intermediateCatchEvent, AbstractBPMNProcess process) {
    intermediateCatchEvent
        .getIncomingFlows()
        .forEach(
            inFlow -> {
              String preToken;
              String signalOccurrence;
              if (inFlow.getSource().isExclusiveEventBasedGateway()) {
                preToken = getTokenForFlowNode(inFlow.getSource());
                signalOccurrence = getSignalOccurrenceForFlowNode(inFlow.getSource());
              } else {
                preToken = getTokenForSequenceFlow(inFlow);
                signalOccurrence = getSignalOccurrenceForSequenceFlow(inFlow);
              }

              ruleBuilder.startRule(
                  getFlowNodeRuleNameWithIncFlow(intermediateCatchEvent, inFlow.getId()));

              // Consume incoming token + signal.
              ruleBuilder.addPreObject(
                  createProcessSnapshotObjectRunning(
                      process,
                      ANY_SUBPROCESSES,
                      preToken + ANY_OTHER_TOKENS,
                      signalOccurrence + ANY_OTHER_SIGNALS));
              // Produce a token for each outgoing flow.
              String postTokens =
                  getOutgoingTokensForFlowNode(intermediateCatchEvent) + ANY_OTHER_TOKENS;
              ruleBuilder.addPostObject(
                  createProcessSnapshotObjectAnySubProcessAndSignals(process, postTokens));
              ruleBuilder.buildRule();
            });
  }

  private void createIntermediateCatchMessageEventRule(
      IntermediateCatchEvent intermediateCatchEvent, AbstractBPMNProcess process) {
    // Start event rule
    createStartInteractionNodeRule(intermediateCatchEvent, process);

    // Rule to end the event when a message is received.
    createEndInteractionNodeRule(intermediateCatchEvent, process);
  }

  public void createIntermediateThrowEventRule(
      AbstractBPMNProcess process, IntermediateThrowEvent intermediateThrowEvent) {
    // We currently limit to one incoming token, but we could implement an implicit exclusive
    // gateway.
    if (intermediateThrowEvent.getIncomingFlows().count() != 1) {
      throw new BPMNRuntimeException(
          "Intermediate throw events should have exactly one incoming sequence flow!");
    }
    switch (intermediateThrowEvent.getType()) {
      case NONE -> createIntermediateThrowNoneEventRule(intermediateThrowEvent, process);
      case LINK -> createIntermediateThrowLinkEventRule(intermediateThrowEvent, process);
      case MESSAGE -> createIntermediateThrowMessageEventRule(intermediateThrowEvent, process);
      case SIGNAL -> createIntermediateThrowSignalEventRule(intermediateThrowEvent, process);
      default -> throw new BPMNRuntimeException(
          "Unexpected throw event type: " + intermediateThrowEvent.getType());
    }
  }

  private void createIntermediateThrowSignalEventRule(
      IntermediateThrowEvent intermediateThrowEvent, AbstractBPMNProcess process) {
    intermediateThrowEvent
        .getIncomingFlows()
        .forEach(
            incFlow -> {
              ruleBuilder.startRule(
                  getFlowNodeRuleNameWithIncFlow(intermediateThrowEvent, incFlow.getId()));

              // Consume an incoming token from an incoming flow.
              String preTokens = getTokenForSequenceFlow(incFlow) + ANY_OTHER_TOKENS;
              ruleBuilder.addPreObject(
                  createProcessSnapshotObjectWithParents(process, ANY_SUBPROCESSES, preTokens));
              // Produce a token for each outgoing flow.
              String postTokens =
                  getOutgoingTokensForFlowNode(intermediateThrowEvent) + ANY_OTHER_TOKENS;
              ruleBuilder.addPostObject(
                  createProcessSnapshotObjectWithParents(process, ANY_SUBPROCESSES, postTokens));
              createSignalThrowRulePart(
                  intermediateThrowEvent, intermediateThrowEvent.getEventDefinition());
              ruleBuilder.buildRule();
            });
  }

  private void createIntermediateThrowLinkEventRule(
      IntermediateThrowEvent intermediateThrowEvent, AbstractBPMNProcess process) {
    intermediateThrowEvent
        .getIncomingFlows()
        .forEach(
            incFlow -> {
              ruleBuilder.startRule(
                  getFlowNodeRuleNameWithIncFlow(intermediateThrowEvent, incFlow.getId()));

              // Consume an incoming token from an incoming flow.
              String preTokens = getTokenForSequenceFlow(incFlow) + ANY_OTHER_TOKENS;
              ruleBuilder.addPreObject(
                  createProcessSnapshotObjectAnySubProcessAndSignals(process, preTokens));
              // Find corresponding catching link events and put tokens on their outgoing flows
              String postTokens =
                  createLinkThrowEventTokens(intermediateThrowEvent, process) + ANY_OTHER_TOKENS;
              ruleBuilder.addPostObject(
                  createProcessSnapshotObjectAnySubProcessAndNoSignals(process, postTokens));
              ruleBuilder.buildRule();
            });
  }

  private String createLinkThrowEventTokens(
      IntermediateThrowEvent intermediateThrowEvent, AbstractBPMNProcess process) {
    return process
        .getFlowNodes()
        // Find corresponding link events (correct name and type)
        .filter(flowNode -> BPMNToGrooveTransformerHelper.matchesLinkThrowEvent(intermediateThrowEvent, flowNode))
        // Get outgoing tokens for corresponding link events
        .map(this::getOutgoingTokensForFlowNode)
        .collect(Collectors.joining(WHITE_SPACE));
  }

  private void createIntermediateThrowMessageEventRule(
      IntermediateThrowEvent intermediateThrowEvent, AbstractBPMNProcess process) {
    intermediateThrowEvent
        .getIncomingFlows()
        .forEach(
            incFlow -> {
              ruleBuilder.startRule(
                  getFlowNodeRuleNameWithIncFlow(intermediateThrowEvent, incFlow.getId()));

              // Consume an incoming token from an incoming flow.
              String preTokens = getTokenForSequenceFlow(incFlow) + ANY_OTHER_TOKENS;
              ruleBuilder.addPreObject(
                  createProcessSnapshotObjectWithParents(process, ANY_SUBPROCESSES, preTokens));
              // Produce a token for each outgoing flow.
              String postTokens =
                  getOutgoingTokensForFlowNode(intermediateThrowEvent) + ANY_OTHER_TOKENS;
              ruleBuilder.addPostObject(
                  createProcessSnapshotObjectWithParents(process, ANY_SUBPROCESSES, postTokens));
              addSendMessageBehaviorForFlowNode(intermediateThrowEvent);
              ruleBuilder.buildRule();
            });
  }

  private void createIntermediateThrowNoneEventRule(
      Event intermediateThrowEvent, AbstractBPMNProcess process) {
    intermediateThrowEvent
        .getIncomingFlows()
        .forEach(
            incFlow -> {
              ruleBuilder.startRule(
                  getFlowNodeRuleNameWithIncFlow(intermediateThrowEvent, incFlow.getId()));

              // Consume an incoming token from an incoming flow.
              String preTokens = getTokenForSequenceFlow(incFlow) + ANY_OTHER_TOKENS;
              ruleBuilder.addPreObject(
                  createProcessSnapshotObjectAnySubProcessAndSignals(process, preTokens));
              // Produce a token for each outgoing flow.
              String postTokens =
                  getOutgoingTokensForFlowNode(intermediateThrowEvent) + ANY_OTHER_TOKENS;
              ruleBuilder.addPostObject(
                  createProcessSnapshotObjectAnySubProcessAndNoSignals(process, postTokens));
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

  @Override
  public BPMNCollaboration getCollaboration() {
    return ruleGenerator.getCollaboration();
  }

  @Override
  public MaudeBPMNGenerationSettings getSettings() {
    return ruleGenerator.getSettings();
  }
}
