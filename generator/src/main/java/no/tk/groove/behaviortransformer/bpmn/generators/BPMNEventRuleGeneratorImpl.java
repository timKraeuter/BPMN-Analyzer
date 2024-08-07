package no.tk.groove.behaviortransformer.bpmn.generators;

import static no.tk.groove.behaviortransformer.GrooveTransformer.AT;
import static no.tk.groove.behaviortransformer.GrooveTransformer.EXISTS_OPTIONAL;
import static no.tk.groove.behaviortransformer.GrooveTransformer.FORALL;
import static no.tk.groove.behaviortransformer.GrooveTransformer.IN;
import static no.tk.groove.behaviortransformer.GrooveTransformer.UNEQUALS;
import static no.tk.groove.behaviortransformer.GrooveTransformerHelper.createStringNodeLabel;
import static no.tk.groove.behaviortransformer.bpmn.BPMNToGrooveTransformerConstants.MESSAGES;
import static no.tk.groove.behaviortransformer.bpmn.BPMNToGrooveTransformerConstants.POSITION;
import static no.tk.groove.behaviortransformer.bpmn.BPMNToGrooveTransformerConstants.STATE;
import static no.tk.groove.behaviortransformer.bpmn.BPMNToGrooveTransformerConstants.SUBPROCESS;
import static no.tk.groove.behaviortransformer.bpmn.BPMNToGrooveTransformerConstants.THROW;
import static no.tk.groove.behaviortransformer.bpmn.BPMNToGrooveTransformerConstants.TOKENS;
import static no.tk.groove.behaviortransformer.bpmn.BPMNToGrooveTransformerConstants.TYPE_MESSAGE;
import static no.tk.groove.behaviortransformer.bpmn.BPMNToGrooveTransformerConstants.TYPE_RUNNING;
import static no.tk.groove.behaviortransformer.bpmn.BPMNToGrooveTransformerConstants.TYPE_TERMINATED;
import static no.tk.groove.behaviortransformer.bpmn.BPMNToGrooveTransformerConstants.TYPE_TOKEN;
import static no.tk.groove.behaviortransformer.bpmn.BPMNToGrooveTransformerHelper.addOutgoingTokensForFlowNodeToProcessInstance;
import static no.tk.groove.behaviortransformer.bpmn.BPMNToGrooveTransformerHelper.addProcessInstance;
import static no.tk.groove.behaviortransformer.bpmn.BPMNToGrooveTransformerHelper.addProcessInstanceWithQuantifier;
import static no.tk.groove.behaviortransformer.bpmn.BPMNToGrooveTransformerHelper.addSendMessageBehaviorForFlowNode;
import static no.tk.groove.behaviortransformer.bpmn.BPMNToGrooveTransformerHelper.addTokensForOutgoingFlowsToRunningInstance;
import static no.tk.groove.behaviortransformer.bpmn.BPMNToGrooveTransformerHelper.addTokensForOutgoingFlowsToRunningInstanceWithQuantifier;
import static no.tk.groove.behaviortransformer.bpmn.BPMNToGrooveTransformerHelper.contextFlowNodeToken;
import static no.tk.groove.behaviortransformer.bpmn.BPMNToGrooveTransformerHelper.contextProcessInstance;
import static no.tk.groove.behaviortransformer.bpmn.BPMNToGrooveTransformerHelper.contextProcessInstanceWithOnlyName;
import static no.tk.groove.behaviortransformer.bpmn.BPMNToGrooveTransformerHelper.contextProcessInstanceWithQuantifier;
import static no.tk.groove.behaviortransformer.bpmn.BPMNToGrooveTransformerHelper.deleteAllTokensForProcess;
import static no.tk.groove.behaviortransformer.bpmn.BPMNToGrooveTransformerHelper.deleteFlowNodeToken;
import static no.tk.groove.behaviortransformer.bpmn.BPMNToGrooveTransformerHelper.deleteMessageToProcessInstanceWithPosition;
import static no.tk.groove.behaviortransformer.bpmn.BPMNToGrooveTransformerHelper.deleteSequenceFlowToken;
import static no.tk.groove.behaviortransformer.bpmn.BPMNToGrooveTransformerHelper.distinctByKey;
import static no.tk.groove.behaviortransformer.bpmn.BPMNToGrooveTransformerHelper.getFlowNodeRuleName;
import static no.tk.groove.behaviortransformer.bpmn.BPMNToGrooveTransformerHelper.interruptSubprocess;
import static no.tk.groove.behaviortransformer.bpmn.BPMNToGrooveTransformerHelper.isAfterInstantiateEventBasedGateway;
import static no.tk.groove.behaviortransformer.bpmn.BPMNToGrooveTransformerHelper.matchesLinkThrowEvent;

import io.github.timkraeuter.groove.graph.GrooveNode;
import io.github.timkraeuter.groove.rule.GrooveRuleBuilder;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import no.tk.behavior.bpmn.AbstractBPMNProcess;
import no.tk.behavior.bpmn.BPMNCollaboration;
import no.tk.behavior.bpmn.BPMNEventSubprocess;
import no.tk.behavior.bpmn.BPMNProcess;
import no.tk.behavior.bpmn.FlowElement;
import no.tk.behavior.bpmn.FlowNode;
import no.tk.behavior.bpmn.MessageFlow;
import no.tk.behavior.bpmn.SequenceFlow;
import no.tk.behavior.bpmn.activities.Activity;
import no.tk.behavior.bpmn.activities.CallActivity;
import no.tk.behavior.bpmn.activities.tasks.AbstractTask;
import no.tk.behavior.bpmn.activities.tasks.ReceiveTask;
import no.tk.behavior.bpmn.activities.tasks.SendTask;
import no.tk.behavior.bpmn.activities.tasks.Task;
import no.tk.behavior.bpmn.auxiliary.exceptions.BPMNRuntimeException;
import no.tk.behavior.bpmn.auxiliary.exceptions.GrooveGenerationRuntimeException;
import no.tk.behavior.bpmn.auxiliary.exceptions.ShouldNotHappenRuntimeException;
import no.tk.behavior.bpmn.auxiliary.visitors.AbstractProcessVisitor;
import no.tk.behavior.bpmn.auxiliary.visitors.ActivityVisitor;
import no.tk.behavior.bpmn.auxiliary.visitors.EventVisitor;
import no.tk.behavior.bpmn.events.BoundaryEvent;
import no.tk.behavior.bpmn.events.BoundaryEventType;
import no.tk.behavior.bpmn.events.EndEvent;
import no.tk.behavior.bpmn.events.EndEventType;
import no.tk.behavior.bpmn.events.Event;
import no.tk.behavior.bpmn.events.IntermediateCatchEvent;
import no.tk.behavior.bpmn.events.IntermediateThrowEvent;
import no.tk.behavior.bpmn.events.StartEvent;
import no.tk.behavior.bpmn.events.StartEventType;
import no.tk.behavior.bpmn.events.definitions.EventDefinition;
import no.tk.groove.behaviortransformer.bpmn.BPMNRuleGenerator;
import org.apache.commons.lang3.tuple.Pair;

public class BPMNEventRuleGeneratorImpl implements BPMNEventRuleGenerator {

  private final BPMNCollaboration collaboration;
  private final GrooveRuleBuilder ruleBuilder;

  public BPMNEventRuleGeneratorImpl(
      BPMNRuleGenerator bpmnRuleGenerator, GrooveRuleBuilder ruleBuilder) {
    this.collaboration = bpmnRuleGenerator.getCollaboration();
    this.ruleBuilder = ruleBuilder;
  }

  @Override
  public void createStartEventRulesForProcess(AbstractBPMNProcess process, StartEvent startEvent) {
    if (startEvent.getOutgoingFlows().findAny().isEmpty()) {
      throw new GrooveGenerationRuntimeException(
          String.format(
              "The start event %s has no outgoing sequence flows!", startEvent.getName()));
    }
    // Done in the corresponding throw rules.
  }

  @Override
  public void createEndEventRule(AbstractBPMNProcess process, EndEvent endEvent) {
    endEvent
        .getIncomingFlows()
        .forEach(
            inFlow -> {
              ruleBuilder.startRule(getFlowNodeRuleName(endEvent, inFlow.getNameOrIDIfEmpty()));
              switch (endEvent.getType()) {
                case NONE -> {
                  GrooveNode processInstance = deleteIncomingEndEventToken(process, inFlow);
                  GrooveNode noneRunning = ruleBuilder.contextNode(TYPE_RUNNING);
                  ruleBuilder.contextEdge(STATE, processInstance, noneRunning);
                }
                case TERMINATION -> {
                  GrooveNode processInstance = deleteIncomingEndEventToken(process, inFlow);
                  GrooveNode running = ruleBuilder.deleteNode(TYPE_RUNNING);
                  ruleBuilder.deleteEdge(STATE, processInstance, running);

                  GrooveNode terminated = ruleBuilder.addNode(TYPE_TERMINATED);
                  ruleBuilder.addEdge(STATE, processInstance, terminated);

                  GrooveNode anyToken = ruleBuilder.deleteNode(TYPE_TOKEN);
                  ruleBuilder.deleteEdge(TOKENS, processInstance, anyToken);
                  GrooveNode forAll = ruleBuilder.contextNode(FORALL);
                  ruleBuilder.contextEdge(AT, anyToken, forAll);

                  interruptSubprocess(ruleBuilder, null, processInstance, true);
                }
                case MESSAGE -> {
                  GrooveNode processInstance = deleteIncomingEndEventToken(process, inFlow);
                  GrooveNode messageRunning = ruleBuilder.contextNode(TYPE_RUNNING);
                  ruleBuilder.contextEdge(STATE, processInstance, messageRunning);
                  addSendMessageBehaviorForFlowNode(collaboration, ruleBuilder, endEvent);
                }
                case ERROR, ESCALATION -> createErrorOrEscalationEndEventRule(endEvent, inFlow);
                case SIGNAL -> {
                  GrooveNode processInstance = deleteIncomingEndEventToken(process, inFlow);
                  GrooveNode signalRunning = ruleBuilder.contextNode(TYPE_RUNNING);
                  ruleBuilder.contextEdge(STATE, processInstance, signalRunning);
                  createSignalThrowRulePart(endEvent.getEventDefinition());
                }
              }

              ruleBuilder.buildRule();
            });
  }

  private void createErrorOrEscalationEndEventRule(EndEvent endEvent, SequenceFlow inFlow) {
    // Find container process and see if a matching error/escalation catch event can be found.
    AbstractBPMNProcess endEventProcess = this.collaboration.findProcessForFlowNode(endEvent);
    endEventProcess.accept(
        new AbstractProcessVisitor() {
          @Override
          public void handle(BPMNEventSubprocess eventSubprocess) {
            createErrorOrEscalationEndEventRuleForEventSubprocess(
                eventSubprocess, endEvent, inFlow);
          }

          @Override
          public void handle(BPMNProcess process) {
            createErrorOrEscalationEndEventRule(process, endEvent, inFlow);
          }
        });
  }

  private void createErrorOrEscalationEndEventRuleForEventSubprocess(
      BPMNEventSubprocess eventSubprocess,
      EndEvent errorOrEscalationEndEvent,
      SequenceFlow inFlow) {
    AbstractBPMNProcess parentProcess = collaboration.getParentProcess(eventSubprocess);
    parentProcess.accept(
        new AbstractProcessVisitor() {
          @Override
          public void handle(BPMNEventSubprocess eventSubprocess) {
            throw new GrooveGenerationRuntimeException("Event subprocess nesting not supported!");
          }

          @Override
          public void handle(BPMNProcess parentProcess) {
            CallActivity callActivity = parentProcess.getCallActivityIfExists();
            if (callActivity != null) {
              Optional<BoundaryEvent> matchingBoundaryEvent =
                  findMatchingErrorOrEscalationBoundaryEvent(
                      callActivity, errorOrEscalationEndEvent);
              if (matchingBoundaryEvent.isPresent()) {
                // Add outgoing tokens to the boundary event for the parent parent process instance.
                GrooveNode parentParentProcessInstance =
                    contextProcessInstance(
                        collaboration.getParentProcess(parentProcess), ruleBuilder);
                BoundaryEvent boundaryEvent = matchingBoundaryEvent.get();
                addOutgoingTokensForFlowNodeToProcessInstance(
                    boundaryEvent, ruleBuilder, parentParentProcessInstance);
                // Interrupt if needed.
                GrooveNode evProcessInstance =
                    interruptIfNeeded(
                        parentProcess, boundaryEvent, parentParentProcessInstance, eventSubprocess);

                deleteSequenceFlowToken(ruleBuilder, evProcessInstance, inFlow);
                return;
              }
            }
            throw new GrooveGenerationRuntimeException(
                noMatchingErrorOrEscalationCatchEventFoundFor(errorOrEscalationEndEvent));
          }
        });
  }

  private GrooveNode interruptIfNeeded(
      BPMNProcess parentProcess,
      BoundaryEvent matchingBoundaryEvent,
      GrooveNode parentParentProcessInstance,
      BPMNEventSubprocess eventSubprocess) {
    GrooveNode evProcessInstance;
    if (matchingBoundaryEvent.isInterrupt()) {
      // Interrupt the parent process and ev process since there was an error/escalation.
      GrooveNode parentProcessInstance =
          interruptSubprocess(ruleBuilder, parentProcess, parentParentProcessInstance, false);
      evProcessInstance =
          interruptSubprocess(ruleBuilder, eventSubprocess, parentProcessInstance, false);
    } else {
      GrooveNode parentProcessInstance = contextProcessInstance(parentProcess, ruleBuilder);
      evProcessInstance = contextProcessInstance(eventSubprocess, ruleBuilder);
      ruleBuilder.contextEdge(SUBPROCESS, parentProcessInstance, evProcessInstance);
      ruleBuilder.contextEdge(SUBPROCESS, parentParentProcessInstance, parentProcessInstance);
    }
    return evProcessInstance;
  }

  private void createErrorOrEscalationEndEventRule(
      BPMNProcess process, EndEvent errorEndEvent, SequenceFlow inFlow) {
    CallActivity callActivity = process.getCallActivityIfExists();
    if (callActivity != null) {
      createErrorOrEscalationEndEventInSubprocessRule(process, errorEndEvent, inFlow, callActivity);
      return;
    }
    Optional<Pair<BPMNEventSubprocess, StartEvent>> eSubProcessAndMatchingStartEvent =
        findMatchingEventSubprocessWithErrorOrEscalationStartEvent(process, errorEndEvent);
    if (eSubProcessAndMatchingStartEvent.isPresent()) {
      createErrorOrEscalationStartEventSubprocessRule(
          process, inFlow, eSubProcessAndMatchingStartEvent.get());
      return;
    }
    throw new GrooveGenerationRuntimeException(
        noMatchingErrorOrEscalationCatchEventFoundFor(errorEndEvent));
  }

  private void createErrorOrEscalationEndEventInSubprocessRule(
      BPMNProcess process, EndEvent errorEndEvent, SequenceFlow inFlow, CallActivity callActivity) {
    Optional<Pair<BPMNEventSubprocess, StartEvent>> eSubProcessAndMatchingStartEvent =
        findMatchingEventSubprocessWithErrorOrEscalationStartEvent(process, errorEndEvent);
    Optional<BoundaryEvent> matchingBoundaryEvent =
        findMatchingErrorOrEscalationBoundaryEvent(callActivity, errorEndEvent);
    if (eSubProcessAndMatchingStartEvent.isPresent() && matchingBoundaryEvent.isPresent()) {
      throw new GrooveGenerationRuntimeException(
          multipleErrorOrEscalationCatchEventsFoundMessage(
              errorEndEvent,
              List.of(
                  eSubProcessAndMatchingStartEvent.get().getValue(), matchingBoundaryEvent.get())));
    }
    if (eSubProcessAndMatchingStartEvent.isEmpty() && matchingBoundaryEvent.isEmpty()) {
      throw new GrooveGenerationRuntimeException(
          noMatchingErrorOrEscalationCatchEventFoundFor(errorEndEvent));
    }
    // Only one can be present.
    matchingBoundaryEvent.ifPresent(
        boundaryEvent -> createErrorOrEscalationBoundaryEventRule(process, inFlow, boundaryEvent));
    eSubProcessAndMatchingStartEvent.ifPresent(
        bpmnEventSubprocessStartEventPair ->
            createErrorOrEscalationStartEventSubprocessRule(
                process, inFlow, bpmnEventSubprocessStartEventPair));
  }

  private static String noMatchingErrorOrEscalationCatchEventFoundFor(EndEvent errorEndEvent) {
    return String.format(
        "No matching %s catch event found for \"%s\"!",
        getErrorOrEscalationString(errorEndEvent), errorEndEvent);
  }

  private static String getErrorOrEscalationString(EndEvent errorEndEvent) {
    if (errorEndEvent.getType() == EndEventType.ERROR) {
      return "error";
    }
    // Must be escalation then.
    return "escalation";
  }

  private void createErrorOrEscalationStartEventSubprocessRule(
      BPMNProcess process,
      SequenceFlow inFlow,
      Pair<BPMNEventSubprocess, StartEvent> matchingStartEventAndProcess) {

    GrooveNode processInstance = deleteIncomingEndEventToken(process, inFlow);

    StartEvent startEvent = matchingStartEventAndProcess.getValue();
    // Remove all tokens from the current process instance if interrupting
    if (startEvent.isInterrupt()) {
      GrooveNode anyToken = ruleBuilder.deleteNode(TYPE_TOKEN);
      ruleBuilder.deleteEdge(TOKENS, processInstance, anyToken);

      GrooveNode forAllTokens = ruleBuilder.contextNode(FORALL);
      ruleBuilder.contextEdge(AT, anyToken, forAllTokens);
    }

    // Create event subprocess with tokens after the start event.
    BPMNEventSubprocess eventSubprocess = matchingStartEventAndProcess.getKey();
    GrooveNode eventSubProcessInstance = addProcessInstance(ruleBuilder, eventSubprocess.getName());
    ruleBuilder.addEdge(SUBPROCESS, processInstance, eventSubProcessInstance);

    addOutgoingTokensForFlowNodeToProcessInstance(startEvent, ruleBuilder, eventSubProcessInstance);
  }

  private static Optional<Pair<BPMNEventSubprocess, StartEvent>>
      findMatchingEventSubprocessWithErrorOrEscalationStartEvent(
          BPMNProcess process, EndEvent endEvent) {
    List<Pair<BPMNEventSubprocess, List<StartEvent>>> startEventsPerEventSubprocess =
        process
            .eventSubprocesses()
            .map(
                bpmnEventSubprocess -> {
                  List<StartEvent> matchingStartEvents =
                      bpmnEventSubprocess.getStartEvents().stream()
                          .filter(startEvent -> errorOrEscalationEventsMatch(endEvent, startEvent))
                          .toList();
                  return matchingStartEvents.isEmpty()
                      ? null
                      : Pair.of(bpmnEventSubprocess, matchingStartEvents);
                })
            .filter(Objects::nonNull)
            .toList();
    if (startEventsPerEventSubprocess.size() > 1) {
      throw new GrooveGenerationRuntimeException(
          multipleEventSubprocessesFoundErrorMessage(startEventsPerEventSubprocess, endEvent));
    }
    if (startEventsPerEventSubprocess.isEmpty()) {
      return Optional.empty();
    }
    Pair<BPMNEventSubprocess, List<StartEvent>> matchingPair =
        startEventsPerEventSubprocess.getFirst();
    if (matchingPair.getRight().size() > 1) {
      throw new GrooveGenerationRuntimeException(
          multipleErrorOrEscalationCatchEventsFoundMessage(endEvent, matchingPair.getRight()));
    }
    return Optional.of(Pair.of(matchingPair.getLeft(), matchingPair.getRight().getFirst()));
  }

  private static String multipleEventSubprocessesFoundErrorMessage(
      List<Pair<BPMNEventSubprocess, List<StartEvent>>> startEventsPerEventSubprocess,
      EndEvent endEvent) {
    List<StartEvent> matchingStartEvents =
        startEventsPerEventSubprocess.stream()
            .flatMap(bpmnEventSubprocessListPair -> bpmnEventSubprocessListPair.getRight().stream())
            .toList();
    return multipleErrorOrEscalationCatchEventsFoundMessage(endEvent, matchingStartEvents);
  }

  private static String multipleErrorOrEscalationCatchEventsFoundMessage(
      EndEvent endEvent, List<? extends Event> matchingStartEvents) {
    return String.format(
        "There were multiple matching %s catch events \"%s\" for the end event \"%s\"!",
        getErrorOrEscalationString(endEvent), matchingStartEvents, endEvent);
  }

  private static boolean errorOrEscalationEventsMatch(EndEvent endEvent, StartEvent startEvent) {
    StartEventType errorOrEscalation =
        endEvent.getType() == EndEventType.ERROR ? StartEventType.ERROR : StartEventType.ESCALATION;
    return startEvent.getType() == errorOrEscalation
        && startEvent.getEventDefinition().equals(endEvent.getEventDefinition());
  }

  private void createErrorOrEscalationBoundaryEventRule(
      AbstractBPMNProcess process, SequenceFlow inFlow, BoundaryEvent matchingBoundaryEvent) {

    // Add outgoing tokens to the boundary event for the father process instance.
    GrooveNode fatherProcessInstance =
        contextProcessInstance(collaboration.getParentProcess(process), ruleBuilder);
    addOutgoingTokensForFlowNodeToProcessInstance(
        matchingBoundaryEvent, ruleBuilder, fatherProcessInstance);
    // Delete incoming end event token and potentially interrupt the process.
    GrooveNode subprocess;
    if (matchingBoundaryEvent.isInterrupt()) {
      // Interrupt the subprocess since there was an error.
      subprocess = interruptSubprocess(ruleBuilder, process, fatherProcessInstance, false);
    } else {
      subprocess = contextProcessInstance(process, ruleBuilder);
      ruleBuilder.contextEdge(SUBPROCESS, fatherProcessInstance, subprocess);
    }
    deleteSequenceFlowToken(ruleBuilder, subprocess, inFlow);
  }

  private GrooveNode deleteIncomingEndEventToken(AbstractBPMNProcess process, SequenceFlow inFlow) {
    GrooveNode processInstance = contextProcessInstanceWithOnlyName(process, ruleBuilder);
    deleteSequenceFlowToken(ruleBuilder, processInstance, inFlow);
    return processInstance;
  }

  private static Optional<BoundaryEvent> findMatchingErrorOrEscalationBoundaryEvent(
      CallActivity callActivity, EndEvent endEvent) {
    List<BoundaryEvent> matchingBoundaryEvents =
        callActivity.getBoundaryEvents().stream()
            .filter(boundaryEvent -> errorOrEscalationEventsMatch(endEvent, boundaryEvent))
            .toList();
    if (matchingBoundaryEvents.isEmpty()) {
      return Optional.empty();
    }
    if (matchingBoundaryEvents.size() > 1) {
      throw new GrooveGenerationRuntimeException(
          String.format(
              "Multiple boundary error events \"%s\" found matching the error end event \"%s\"!",
              matchingBoundaryEvents.stream().map(FlowElement::getId).toList(), endEvent.getId()));
    }
    return Optional.of(matchingBoundaryEvents.getFirst());
  }

  private static boolean errorOrEscalationEventsMatch(
      EndEvent endEvent, BoundaryEvent boundaryEvent) {
    BoundaryEventType errorOrEscalation =
        endEvent.getType() == EndEventType.ERROR
            ? BoundaryEventType.ERROR
            : BoundaryEventType.ESCALATION;
    return boundaryEvent.getType() == errorOrEscalation
        && boundaryEvent.getEventDefinition().equals(endEvent.getEventDefinition());
  }

  @Override
  public void createIntermediateThrowEventRule(
      AbstractBPMNProcess process, IntermediateThrowEvent intermediateThrowEvent) {

    String ruleName =
        THROW + intermediateThrowEvent.getName() + "_" + intermediateThrowEvent.getId();
    // We currently limit to one incoming token, but we could implement an implicit exclusive
    // gateway.
    if (intermediateThrowEvent.getIncomingFlows().count() != 1) {
      throw new BPMNRuntimeException(
          "Intermediate throw events should have exactly one incoming sequence flow!");
    }
    switch (intermediateThrowEvent.getType()) {
      case NONE ->
          createIntermediateThrowNoneEventRule(
              intermediateThrowEvent, ruleName, ruleBuilder, process);
      case LINK ->
          createIntermediateThrowLinkEventRule(
              intermediateThrowEvent, ruleName, ruleBuilder, process);
      case MESSAGE ->
          createIntermediateThrowMessageEventRule(intermediateThrowEvent, ruleName, process);
      case SIGNAL ->
          createIntermediateThrowSignalEventRule(intermediateThrowEvent, ruleName, process);
      default ->
          throw new BPMNRuntimeException(
              "Unexpected throw event type: " + intermediateThrowEvent.getType());
    }
  }

  @Override
  public void createIntermediateCatchEventRule(
      AbstractBPMNProcess process, IntermediateCatchEvent intermediateCatchEvent) {
    switch (intermediateCatchEvent.getType()) {
      case MESSAGE:
        createIntermediateCatchMessageEventRule(
            intermediateCatchEvent, process, ruleBuilder, collaboration);
        break;
      case LINK, SIGNAL:
        // Done in the corresponding throw rule.
        break;
      case TIMER:
        createIntermediateCatchTimerEventRule(intermediateCatchEvent, process, ruleBuilder);
        break;
    }
  }

  private void createIntermediateCatchTimerEventRule(
      IntermediateCatchEvent intermediateCatchEvent,
      AbstractBPMNProcess process,
      GrooveRuleBuilder ruleBuilder) {

    ruleBuilder.startRule(intermediateCatchEvent.getName());
    final GrooveNode processInstance = contextProcessInstance(process, ruleBuilder);

    if (intermediateCatchEvent.getIncomingFlows().count() != 1) {
      // current restriction, again we would need implicit exclusive gateway.
      throw new BPMNRuntimeException(
          "Intermediate message catch events are only allowed to have one incoming "
              + "sequence flow!");
    }
    intermediateCatchEvent
        .getIncomingFlows()
        .forEach(inFlow -> deleteSequenceFlowToken(ruleBuilder, processInstance, inFlow));

    // Add tokens on outgoing flows.
    addOutgoingTokensForFlowNodeToProcessInstance(
        intermediateCatchEvent, ruleBuilder, processInstance);

    ruleBuilder.buildRule();
  }

  private void createIntermediateCatchMessageEventRule(
      IntermediateCatchEvent intermediateCatchEvent,
      AbstractBPMNProcess process,
      GrooveRuleBuilder ruleBuilder,
      BPMNCollaboration collaboration) {
    if (isAfterInstantiateEventBasedGateway(intermediateCatchEvent)) {
      // Not needed to create rules in this case.
      return;
    }

    collaboration
        .getIncomingMessageFlows(intermediateCatchEvent)
        .forEach(
            messageFlow -> {
              ruleBuilder.startRule(
                  createCatchMessageRuleName(messageFlow, intermediateCatchEvent, collaboration));

              GrooveNode processInstance =
                  addTokensForOutgoingFlowsToRunningInstance(
                      intermediateCatchEvent, process, ruleBuilder);
              if (intermediateCatchEvent.getIncomingFlows().count() != 1) {
                // current restriction, again we would need implicit exclusive gateway.
                throw new BPMNRuntimeException(
                    "Intermediate message catch events are only allowed to have one incoming "
                        + "sequence flow!");
              }
              //noinspection OptionalGetWithoutIsPresent Size of the stream must be 1.
              SequenceFlow incFlow = intermediateCatchEvent.getIncomingFlows().findFirst().get();
              // Consume incoming token
              if (incFlow.getSource().isExclusiveEventBasedGateway()) {
                deleteFlowNodeToken(ruleBuilder, processInstance, incFlow.getSource());
              } else {
                deleteSequenceFlowToken(ruleBuilder, processInstance, incFlow);
              }
              // Consume incoming message.
              final GrooveNode deletedMessage =
                  deleteMessageToProcessInstanceWithPosition(
                      ruleBuilder, processInstance, messageFlow.getNameOrDescriptiveName());
              // Delete all other incoming messages.
              final GrooveNode forAll = ruleBuilder.contextNode(FORALL);
              GrooveNode message = ruleBuilder.deleteNode(TYPE_MESSAGE);
              ruleBuilder.deleteEdge(MESSAGES, processInstance, message);
              ruleBuilder.contextEdge(AT, message, forAll);
              ruleBuilder.contextEdge(UNEQUALS, message, deletedMessage);

              ruleBuilder.buildRule();
            });
  }

  private String createCatchMessageRuleName(
      MessageFlow messageFlow,
      IntermediateCatchEvent intermediateCatchEvent,
      BPMNCollaboration collaboration) {
    final int amountOfIncMessages =
        collaboration.getIncomingMessageFlows(intermediateCatchEvent).size();
    if (amountOfIncMessages <= 1) {
      return intermediateCatchEvent.getName();
    }
    return intermediateCatchEvent.getName() + "_" + messageFlow.getNameOrDescriptiveName();
  }

  private void createIntermediateThrowNoneEventRule(
      IntermediateThrowEvent intermediateThrowEvent,
      String ruleName,
      GrooveRuleBuilder ruleBuilder,
      AbstractBPMNProcess process) {
    ruleBuilder.startRule(ruleName);
    GrooveNode processInstance = contextProcessInstance(process, ruleBuilder);
    // We currently limit to one incoming token, but we could implement an implicit exclusive
    // gateway.
    intermediateThrowEvent
        .getIncomingFlows()
        .forEach(
            sequenceFlow -> deleteSequenceFlowToken(ruleBuilder, processInstance, sequenceFlow));
    addOutgoingTokensForFlowNodeToProcessInstance(
        intermediateThrowEvent, ruleBuilder, processInstance);

    ruleBuilder.buildRule();
  }

  private void createIntermediateThrowSignalEventRule(
      IntermediateThrowEvent intermediateThrowEvent, String ruleName, AbstractBPMNProcess process) {
    ruleBuilder.startRule(ruleName);
    GrooveNode processInstance = contextProcessInstance(process, ruleBuilder);
    // We currently limit to one incoming token, but we could implement an implicit exclusive
    // gateway.
    intermediateThrowEvent
        .getIncomingFlows()
        .forEach(
            sequenceFlow -> deleteSequenceFlowToken(ruleBuilder, processInstance, sequenceFlow));
    addOutgoingTokensForFlowNodeToProcessInstance(
        intermediateThrowEvent, ruleBuilder, processInstance);

    createSignalThrowRulePart(intermediateThrowEvent.getEventDefinition());
    ruleBuilder.buildRule();
  }

  private void createIntermediateThrowMessageEventRule(
      IntermediateThrowEvent intermediateThrowEvent, String ruleName, AbstractBPMNProcess process) {
    ruleBuilder.startRule(ruleName);
    GrooveNode processInstance = contextProcessInstance(process, ruleBuilder);
    intermediateThrowEvent
        .getIncomingFlows()
        .forEach(sf -> deleteSequenceFlowToken(ruleBuilder, processInstance, sf));
    addOutgoingTokensForFlowNodeToProcessInstance(
        intermediateThrowEvent, ruleBuilder, processInstance);
    addSendMessageBehaviorForFlowNode(collaboration, ruleBuilder, intermediateThrowEvent);

    ruleBuilder.buildRule();
  }

  private void createIntermediateThrowLinkEventRule(
      IntermediateThrowEvent intermediateThrowEvent,
      String ruleName,
      GrooveRuleBuilder ruleBuilder,
      AbstractBPMNProcess process) {
    ruleBuilder.startRule(ruleName);
    GrooveNode processInstance = contextProcessInstance(process, ruleBuilder);
    intermediateThrowEvent
        .getIncomingFlows()
        .forEach(
            sf -> {
              deleteSequenceFlowToken(ruleBuilder, processInstance, sf);
              // Add outgoing flows for corresponding link events.
              findMatchingLinkCatchEvents(intermediateThrowEvent, process)
                  .forEach(
                      matchingLinkCatchEvent ->
                          addOutgoingTokensForFlowNodeToProcessInstance(
                              matchingLinkCatchEvent, ruleBuilder, processInstance));
            });

    ruleBuilder.buildRule();
  }

  private Stream<FlowNode> findMatchingLinkCatchEvents(
      IntermediateThrowEvent intermediateThrowEvent, AbstractBPMNProcess process) {
    return process
        .flowNodes()
        // Find corresponding link events (correct name and type)
        .filter(flowNode -> matchesLinkThrowEvent(intermediateThrowEvent, flowNode));
  }

  private void createSignalThrowRulePart(EventDefinition eventDefinition) {
    Pair<Set<Event>, Set<BoundaryEvent>> correspondingSignalCatchEvents =
        collaboration.findAllCorrespondingCatchEvents(eventDefinition);

    correspondingSignalCatchEvents.getLeft().forEach(this::createCatchSignalEventRulePart);
    correspondingSignalCatchEvents.getRight().forEach(this::createBoundarySignalCatchEventRulePart);
  }

  private void createBoundarySignalCatchEventRulePart(BoundaryEvent boundarySignalEvent) {
    Activity activity = boundarySignalEvent.getAttachedTo();
    activity.accept(
        new ActivityVisitor() {
          @Override
          public void handle(Task task) {
            createBoundarySignalCatchEventRulePartForTask(task);
          }

          @Override
          public void handle(SendTask task) {
            createBoundarySignalCatchEventRulePartForTask(task);
          }

          @Override
          public void handle(ReceiveTask task) {
            createBoundarySignalCatchEventRulePartForTask(task);
          }

          private void createBoundarySignalCatchEventRulePartForTask(AbstractTask task) {
            // Multiple boundary events can be triggered
            GrooveNode forAll = ruleBuilder.contextNode(FORALL);
            AbstractBPMNProcess process = collaboration.findProcessForFlowNode(task);
            GrooveNode processInstance =
                addTokensForOutgoingFlowsToRunningInstanceWithQuantifier(
                    boundarySignalEvent, process, ruleBuilder, forAll);

            // Delete token in task if interrupt.
            if (boundarySignalEvent.isInterrupt()) {
              GrooveNode deletedToken = deleteFlowNodeToken(ruleBuilder, processInstance, task);
              ruleBuilder.contextEdge(AT, deletedToken, forAll);
            } else {
              GrooveNode contextToken = contextFlowNodeToken(ruleBuilder, processInstance, task);
              ruleBuilder.contextEdge(AT, contextToken, forAll);
            }
          }

          @Override
          public void handle(CallActivity callActivity) {
            // Multiple boundary events can be triggered
            GrooveNode forAll = ruleBuilder.contextNode(FORALL);
            AbstractBPMNProcess process = collaboration.findProcessForFlowNode(callActivity);
            GrooveNode processInstance =
                addTokensForOutgoingFlowsToRunningInstanceWithQuantifier(
                    boundarySignalEvent, process, ruleBuilder, forAll);

            if (boundarySignalEvent.isInterrupt()) {
              interruptSubprocess(
                  ruleBuilder, callActivity.getSubProcessModel(), processInstance, true, forAll);
            } else {
              // Subprocess must be running
              GrooveNode subprocessInstance =
                  contextProcessInstanceWithQuantifier(
                      callActivity.getSubProcessModel(), ruleBuilder, forAll);
              ruleBuilder.contextEdge(SUBPROCESS, processInstance, subprocessInstance);
            }
          }
        });
  }

  private void createCatchSignalEventRulePart(Event catchSignalEvent) {
    final AbstractBPMNProcess processForEvent =
        collaboration.findProcessForFlowNode(catchSignalEvent);
    if (catchSignalEvent.isInstantiateFlowNode()
        || isAfterInstantiateEventBasedGateway(catchSignalEvent)) {
      createSignalThrowInstantiateRulePart(catchSignalEvent, processForEvent);
    } else {
      // Send a signal to all existing processes
      GrooveNode forAll = ruleBuilder.contextNode(FORALL);
      GrooveNode processInstance =
          contextProcessInstanceWithQuantifier(processForEvent, ruleBuilder, forAll);

      //
      catchSignalEvent
          .getIncomingFlows()
          .filter(distinctByKey(SequenceFlow::getDescriptiveName))
          .forEach(
              inFlow -> {
                String position;
                if (inFlow.getSource().isExclusiveEventBasedGateway()) {
                  position = inFlow.getSource().getName();
                } else {
                  position = inFlow.getDescriptiveName();
                }
                GrooveNode token = ruleBuilder.deleteNode(TYPE_TOKEN);
                ruleBuilder.contextEdge(AT, token, forAll);
                ruleBuilder.deleteEdge(TOKENS, processInstance, token);
                ruleBuilder.deleteEdge(
                    POSITION, token, ruleBuilder.contextNode(createStringNodeLabel(position)));
              });
      addOutgoingTokensForFlowNodeWithNestedRuleQuantifier(
          catchSignalEvent, forAll, processInstance);
    }
  }

  private void addOutgoingTokensForFlowNodeWithNestedRuleQuantifier(
      Event event, GrooveNode nestedRuleQuantifier, GrooveNode processInstance) {
    event
        .getOutgoingFlows()
        .forEach(
            outFlow -> {
              GrooveNode newToken = ruleBuilder.addNode(TYPE_TOKEN);
              ruleBuilder.contextEdge(AT, newToken, nestedRuleQuantifier);
              ruleBuilder.addEdge(TOKENS, processInstance, newToken);
              ruleBuilder.addEdge(
                  POSITION,
                  newToken,
                  ruleBuilder.contextNode(createStringNodeLabel(outFlow.getDescriptiveName())));
            });
  }

  private void createSignalThrowInstantiateRulePart(
      Event event, AbstractBPMNProcess processForEvent) {
    processForEvent.accept(
        new AbstractProcessVisitor() {
          @Override
          public void handle(BPMNEventSubprocess eventSubprocess) {
            createSignalThrowInstantiateRulePartForEventSubprocess(eventSubprocess, event);
          }

          @Override
          public void handle(BPMNProcess process) {
            // Create a new process instance.
            GrooveNode processInstance = addProcessInstance(ruleBuilder, processForEvent.getName());
            addOutgoingTokensForFlowNodeToProcessInstance(event, ruleBuilder, processInstance);
          }
        });
  }

  private void createSignalThrowInstantiateRulePartForEventSubprocess(
      BPMNEventSubprocess eventSubprocess, Event event) {
    event.accept(
        new EventVisitor() {
          @Override
          public void handle(StartEvent startEvent) {
            switch (startEvent.getType()) {
              case SIGNAL -> {
                if (startEvent.isInterrupt()) {
                  createSignalInterruptingStartRulePart(eventSubprocess, event);
                } else {
                  createSignalNonInterruptingStartRulePart(eventSubprocess, event);
                }
              }
              case NONE, MESSAGE -> {
                // No rules needed
              }
              default ->
                  throw new IllegalStateException("Unexpected value: " + startEvent.getType());
            }
          }

          @Override
          public void handle(IntermediateThrowEvent intermediateThrowEvent) {
            // Must be a start event if instantiate is true.
            throw new ShouldNotHappenRuntimeException();
          }

          @Override
          public void handle(IntermediateCatchEvent intermediateCatchEvent) {
            // Must be a start event if instantiate is true.
            throw new ShouldNotHappenRuntimeException();
          }

          @Override
          public void handle(EndEvent endEvent) {
            // Must be a start event if instantiate is true.
            throw new ShouldNotHappenRuntimeException();
          }
        });
  }

  private void createSignalInterruptingStartRulePart(
      BPMNEventSubprocess eventSubprocess, Event event) {
    // Everything is optional if a parent process exists.
    GrooveNode existsOptional = ruleBuilder.contextNode(EXISTS_OPTIONAL);

    // Needs a running parent process
    GrooveNode parentProcessInstance =
        contextExistsOptionalParentProcess(eventSubprocess, existsOptional);

    // Start new subprocess instance of process
    GrooveNode eventSubProcessInstance =
        startNewEventSubprocess(existsOptional, parentProcessInstance, eventSubprocess);
    // Create start tokens
    addOutgoingTokensForFlowNodeWithNestedRuleQuantifier(
        event, existsOptional, eventSubProcessInstance);

    // Interrupt parent process means deleting all its tokens.
    GrooveNode forAll = deleteAllTokensForProcess(ruleBuilder, parentProcessInstance);
    ruleBuilder.contextEdge(IN, forAll, existsOptional);
  }

  private void createSignalNonInterruptingStartRulePart(
      BPMNEventSubprocess eventSubprocess, Event event) {
    // Everything is optional if a parent process exists.
    GrooveNode existsOptional = ruleBuilder.contextNode(EXISTS_OPTIONAL);

    // Needs a running parent process
    GrooveNode parentProcessInstance =
        contextExistsOptionalParentProcess(eventSubprocess, existsOptional);

    // Start new subprocess instance of process
    GrooveNode eventSubProcessInstance =
        startNewEventSubprocess(existsOptional, parentProcessInstance, eventSubprocess);
    // Create start tokens
    addOutgoingTokensForFlowNodeWithNestedRuleQuantifier(
        event, existsOptional, eventSubProcessInstance);
  }

  private GrooveNode startNewEventSubprocess(
      GrooveNode existsOptional,
      GrooveNode parentProcessInstance,
      BPMNEventSubprocess eventSubprocess) {
    GrooveNode eventSubProcessInstance =
        addProcessInstanceWithQuantifier(ruleBuilder, eventSubprocess.getName(), existsOptional);
    ruleBuilder.addEdge(SUBPROCESS, parentProcessInstance, eventSubProcessInstance);
    return eventSubProcessInstance;
  }

  private GrooveNode contextExistsOptionalParentProcess(
      BPMNEventSubprocess eventSubprocess, GrooveNode existsOptional) {
    AbstractBPMNProcess parentProcess = collaboration.getParentProcess(eventSubprocess);
    return contextProcessInstanceWithQuantifier(parentProcess, ruleBuilder, existsOptional);
  }
}
