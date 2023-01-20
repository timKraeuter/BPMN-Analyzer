package groove.behaviortransformer.bpmn.generators;

import static groove.behaviortransformer.GrooveTransformer.*;
import static groove.behaviortransformer.GrooveTransformerHelper.createStringNodeLabel;
import static groove.behaviortransformer.bpmn.BPMNToGrooveTransformerConstants.*;
import static groove.behaviortransformer.bpmn.BPMNToGrooveTransformerHelper.*;

import behavior.bpmn.*;
import behavior.bpmn.activities.Activity;
import behavior.bpmn.activities.CallActivity;
import behavior.bpmn.activities.tasks.AbstractTask;
import behavior.bpmn.activities.tasks.ReceiveTask;
import behavior.bpmn.activities.tasks.SendTask;
import behavior.bpmn.activities.tasks.Task;
import behavior.bpmn.auxiliary.exceptions.BPMNRuntimeException;
import behavior.bpmn.auxiliary.exceptions.GrooveGenerationRuntimeException;
import behavior.bpmn.auxiliary.exceptions.ShouldNotHappenRuntimeException;
import behavior.bpmn.auxiliary.visitors.AbstractProcessVisitor;
import behavior.bpmn.auxiliary.visitors.ActivityVisitor;
import behavior.bpmn.auxiliary.visitors.EventVisitor;
import behavior.bpmn.events.*;
import behavior.bpmn.events.definitions.EventDefinition;
import groove.behaviortransformer.bpmn.BPMNRuleGenerator;
import groove.graph.GrooveNode;
import groove.graph.rule.GrooveRuleBuilder;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.lang3.tuple.Pair;

public class BPMNEventRuleGeneratorImpl implements BPMNEventRuleGenerator {

  private final BPMNCollaboration collaboration;
  private final GrooveRuleBuilder ruleBuilder;
  private final boolean useSFId;

  public BPMNEventRuleGeneratorImpl(
      BPMNRuleGenerator bpmnRuleGenerator, GrooveRuleBuilder ruleBuilder, boolean useSFIds) {
    this.collaboration = bpmnRuleGenerator.getCollaboration();
    this.ruleBuilder = ruleBuilder;
    this.useSFId = useSFIds;
  }

  @Override
  public void createStartEventRulesForProcess(AbstractBPMNProcess process, StartEvent startEvent) {
    process.accept(
        new AbstractProcessVisitor() {
          @Override
          public void handle(BPMNEventSubprocess eventSubprocess) {
            // Handled elsewhere for event subprocesses.
          }

          @Override
          public void handle(BPMNProcess process) {
            createStartEventRule(startEvent, process);
          }
        });
  }

  @Override
  public void createEndEventRule(AbstractBPMNProcess process, EndEvent endEvent) {
    if (endEvent.getIncomingFlows().count() != 1) {
      throw new BPMNRuntimeException("End events should have exactly one incoming flow!");
    }
    ruleBuilder.startRule(endEvent.getName());
    switch (endEvent.getType()) {
      case NONE:
        {
          GrooveNode processInstance = deleteIncomingEndEventToken(process, endEvent);
          GrooveNode noneRunning = ruleBuilder.contextNode(TYPE_RUNNING);
          ruleBuilder.contextEdge(STATE, processInstance, noneRunning);
        }
        break;
      case TERMINATION:
        {
          GrooveNode processInstance = deleteIncomingEndEventToken(process, endEvent);
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
        break;
      case MESSAGE:
        {
          GrooveNode processInstance = deleteIncomingEndEventToken(process, endEvent);
          GrooveNode messageRunning = ruleBuilder.contextNode(TYPE_RUNNING);
          ruleBuilder.contextEdge(STATE, processInstance, messageRunning);
          addSendMessageBehaviorForFlowNode(collaboration, ruleBuilder, endEvent, this.useSFId);
          break;
        }
      case ERROR:
      case ESCALATION:
        createErrorOrEscalationEndEventRule(endEvent);
        break;
      case SIGNAL:
        {
          GrooveNode processInstance = deleteIncomingEndEventToken(process, endEvent);
          GrooveNode signalRunning = ruleBuilder.contextNode(TYPE_RUNNING);
          ruleBuilder.contextEdge(STATE, processInstance, signalRunning);
          createSignalThrowRulePart(endEvent.getEventDefinition());
          break;
        }
    }

    ruleBuilder.buildRule();
  }

  private void createErrorOrEscalationEndEventRule(EndEvent endEvent) {
    // Find container process and see if a matching error/escalation catch event can be found.
    AbstractBPMNProcess endEventProcess = this.collaboration.findProcessForFlowNode(endEvent);
    endEventProcess.accept(
        new AbstractProcessVisitor() {
          @Override
          public void handle(BPMNEventSubprocess eventSubprocess) {
            createErrorOrEscalationEndEventRuleForEventSubprocess(eventSubprocess, endEvent);
          }

          @Override
          public void handle(BPMNProcess process) {
            createErrorOrEscalationEndEventRule(process, endEvent);
          }
        });
  }

  private void createErrorOrEscalationEndEventRuleForEventSubprocess(
      BPMNEventSubprocess eventSubprocess, EndEvent errorOrEscalationEndEvent) {
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
                addOutgoingTokensForFlowNodeToProcessInstance(
                    matchingBoundaryEvent.get(), ruleBuilder, parentParentProcessInstance, useSFId);
                // TODO: fix this. And add to test case.
                // Interrupt the parent process and ev process since there was an error/escalation.
                GrooveNode parentProcessInstance =
                    interruptSubprocess(
                        ruleBuilder, parentProcess, parentParentProcessInstance, false);
                GrooveNode evProcessInstance =
                    interruptSubprocess(ruleBuilder, eventSubprocess, parentProcessInstance, false);

                deleteIncomingEndEventToken(errorOrEscalationEndEvent, evProcessInstance);
                return;
              }
            }
            throw new GrooveGenerationRuntimeException(
                noMatchingErrorOrEscalationCatchEventFoundFor(errorOrEscalationEndEvent));
          }
        });
  }

  private void createErrorOrEscalationEndEventRule(BPMNProcess process, EndEvent errorEndEvent) {
    CallActivity callActivity = process.getCallActivityIfExists();
    if (callActivity != null) {
      createErrorOrEscalationEndEventInSubprocessRule(process, errorEndEvent, callActivity);
      return;
    }
    Optional<Pair<BPMNEventSubprocess, StartEvent>> eSubProcessAndMatchingStartEvent =
        findMatchingEventSubprocessWithErrorOrEscalationStartEvent(process, errorEndEvent);
    if (eSubProcessAndMatchingStartEvent.isPresent()) {
      createErrorOrEscalationStartEventSubprocessRule(
          process, errorEndEvent, eSubProcessAndMatchingStartEvent.get());
      return;
    }
    throw new GrooveGenerationRuntimeException(
        noMatchingErrorOrEscalationCatchEventFoundFor(errorEndEvent));
  }

  private void createErrorOrEscalationEndEventInSubprocessRule(
      BPMNProcess process, EndEvent errorEndEvent, CallActivity callActivity) {
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
        boundaryEvent ->
            createErrorOrEscalationBoundaryEventRule(process, errorEndEvent, boundaryEvent));
    eSubProcessAndMatchingStartEvent.ifPresent(
        bpmnEventSubprocessStartEventPair ->
            createErrorOrEscalationStartEventSubprocessRule(
                process, errorEndEvent, bpmnEventSubprocessStartEventPair));
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
      EndEvent endEvent,
      Pair<BPMNEventSubprocess, StartEvent> matchingStartEventAndProcess) {

    GrooveNode processInstance = deleteIncomingEndEventToken(process, endEvent);

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

    addOutgoingTokensForFlowNodeToProcessInstance(
        startEvent, ruleBuilder, eventSubProcessInstance, useSFId);
  }

  private static Optional<Pair<BPMNEventSubprocess, StartEvent>>
      findMatchingEventSubprocessWithErrorOrEscalationStartEvent(
          BPMNProcess process, EndEvent endEvent) {
    List<Pair<BPMNEventSubprocess, List<StartEvent>>> startEventsPerEventSubprocess =
        process
            .getEventSubprocesses()
            .map(
                bpmnEventSubprocess -> {
                  List<StartEvent> matchingStartEvents =
                      bpmnEventSubprocess.getStartEvents().stream()
                          .filter(startEvent -> errorOrEscalationEventsMatch(endEvent, startEvent))
                          .collect(Collectors.toList());
                  return matchingStartEvents.isEmpty()
                      ? null
                      : Pair.of(bpmnEventSubprocess, matchingStartEvents);
                })
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    if (startEventsPerEventSubprocess.size() > 1) {
      throw new GrooveGenerationRuntimeException(
          multipleEventSubprocessesFoundErrorMessage(startEventsPerEventSubprocess, endEvent));
    }
    if (startEventsPerEventSubprocess.isEmpty()) {
      return Optional.empty();
    }
    Pair<BPMNEventSubprocess, List<StartEvent>> matchingPair = startEventsPerEventSubprocess.get(0);
    if (matchingPair.getRight().size() > 1) {
      throw new GrooveGenerationRuntimeException(
          multipleErrorOrEscalationCatchEventsFoundMessage(endEvent, matchingPair.getRight()));
    }
    return Optional.of(Pair.of(matchingPair.getLeft(), matchingPair.getRight().get(0)));
  }

  private static String multipleEventSubprocessesFoundErrorMessage(
      List<Pair<BPMNEventSubprocess, List<StartEvent>>> startEventsPerEventSubprocess,
      EndEvent endEvent) {
    List<StartEvent> matchingStartEvents =
        startEventsPerEventSubprocess.stream()
            .flatMap(bpmnEventSubprocessListPair -> bpmnEventSubprocessListPair.getRight().stream())
            .collect(Collectors.toList());
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
      AbstractBPMNProcess process, EndEvent endEvent, BoundaryEvent matchingBoundaryEvent) {

    // Add outgoing tokens to the boundary event for the father process instance.
    GrooveNode fatherProcessInstance =
        contextProcessInstance(collaboration.getParentProcess(process), ruleBuilder);
    addOutgoingTokensForFlowNodeToProcessInstance(
        matchingBoundaryEvent, ruleBuilder, fatherProcessInstance, useSFId);
    // Delete incoming end event token and potentially interrupt the process.
    GrooveNode subprocess;
    if (matchingBoundaryEvent.isInterrupt()) {
      // Interrupt the subprocess since there was an error.
      subprocess = interruptSubprocess(ruleBuilder, process, fatherProcessInstance, false);
    } else {
      subprocess = contextProcessInstance(process, ruleBuilder);
      ruleBuilder.contextEdge(SUBPROCESS, fatherProcessInstance, subprocess);
    }
    deleteIncomingEndEventToken(endEvent, subprocess);
  }

  private GrooveNode deleteIncomingEndEventToken(AbstractBPMNProcess process, EndEvent endEvent) {
    GrooveNode processInstance = contextProcessInstanceWithOnlyName(process, ruleBuilder);
    deleteIncomingEndEventToken(endEvent, processInstance);
    return processInstance;
  }

  private void deleteIncomingEndEventToken(EndEvent endEvent, GrooveNode processInstance) {
    GrooveNode token = ruleBuilder.deleteNode(TYPE_TOKEN);
    SequenceFlow incomingFlow = endEvent.getIncomingFlows().findFirst().orElseThrow();
    final String incomingFlowId =
        getSequenceFlowIdOrDescriptiveName(incomingFlow, BPMNEventRuleGeneratorImpl.this.useSFId);
    GrooveNode position = ruleBuilder.contextNode(createStringNodeLabel(incomingFlowId));
    ruleBuilder.deleteEdge(POSITION, token, position);
    ruleBuilder.deleteEdge(TOKENS, processInstance, token);
  }

  private static Optional<BoundaryEvent> findMatchingErrorOrEscalationBoundaryEvent(
      CallActivity callActivity, EndEvent endEvent) {
    List<BoundaryEvent> matchingBoundaryEvents =
        callActivity.getBoundaryEvents().stream()
            .filter(boundaryEvent -> errorOrEscalationEventsMatch(endEvent, boundaryEvent))
            .collect(Collectors.toList());
    if (matchingBoundaryEvents.isEmpty()) {
      return Optional.empty();
    }
    if (matchingBoundaryEvents.size() > 1) {
      throw new GrooveGenerationRuntimeException(
          String.format(
              "Multiple boundary error events \"%s\" found matching the error end event \"%s\"!",
              matchingBoundaryEvents.stream().map(FlowElement::getId).collect(Collectors.toList()),
              endEvent.getId()));
    }
    return Optional.of(matchingBoundaryEvents.get(0));
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

    String ruleName = THROW + intermediateThrowEvent.getName();
    // We currently limit to one incoming token, but we could implement an implicit exclusive
    // gateway.
    if (intermediateThrowEvent.getIncomingFlows().count() != 1) {
      throw new BPMNRuntimeException(
          "Intermediate throw events should have exactly one incoming sequence flow!");
    }
    switch (intermediateThrowEvent.getType()) {
      case NONE:
        createIntermediateThrowNoneEventRule(
            intermediateThrowEvent, ruleName, ruleBuilder, process);
        break;
      case LINK:
        createIntermediateThrowLinkEventRule(
            intermediateThrowEvent, ruleName, ruleBuilder, process);
        break;
      case MESSAGE:
        createIntermediateThrowMessageEventRule(intermediateThrowEvent, ruleName, process);
        break;
      case SIGNAL:
        createIntermediateThrowSignalEventRule(intermediateThrowEvent, ruleName, process);
        break;
      default:
        throw new BPMNRuntimeException(
            "Unexpected throw event type: " + intermediateThrowEvent.getType());
    }
  }

  @Override
  public void createIntermediateCatchEventRule(
      AbstractBPMNProcess process, IntermediateCatchEvent intermediateCatchEvent) {
    String ruleName = CATCH + intermediateCatchEvent.getName();
    switch (intermediateCatchEvent.getType()) {
      case LINK:
        createIntermediateCatchLinkEventRule(
            intermediateCatchEvent, process, ruleName, ruleBuilder);
        break;
      case MESSAGE:
        createIntermediateCatchMessageEventRule(
            intermediateCatchEvent, process, ruleBuilder, collaboration);
        break;
      case SIGNAL:
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
        .forEach(
            inFlow ->
                deleteTokenWithPosition(
                    ruleBuilder,
                    processInstance,
                    getSequenceFlowIdOrDescriptiveName(inFlow, this.useSFId)));

    // Add tokens on outgoing flows.
    addOutgoingTokensForFlowNodeToProcessInstance(
        intermediateCatchEvent, ruleBuilder, processInstance, this.useSFId);

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
                      intermediateCatchEvent, process, ruleBuilder, useSFId);
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
                deleteTokenWithPosition(
                    ruleBuilder, processInstance, incFlow.getSource().getName());
              } else {
                deleteTokenWithPosition(
                    ruleBuilder,
                    processInstance,
                    getSequenceFlowIdOrDescriptiveName(incFlow, this.useSFId));
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

  private void createIntermediateCatchLinkEventRule(
      IntermediateCatchEvent intermediateCatchEvent,
      AbstractBPMNProcess process,
      String ruleName,
      GrooveRuleBuilder ruleBuilder) {
    ruleBuilder.startRule(ruleName);

    GrooveNode processInstance =
        addTokensForOutgoingFlowsToRunningInstance(
            intermediateCatchEvent, process, ruleBuilder, useSFId);
    if (intermediateCatchEvent.getIncomingFlows().findAny().isPresent()) {
      throw new BPMNRuntimeException(
          "Link intermediate catch events are not allowed to have incoming sequence " + "flows!");
    }
    deleteTokenWithPosition(ruleBuilder, processInstance, intermediateCatchEvent.getName());

    ruleBuilder.buildRule();
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
            sequenceFlow ->
                deleteTokenWithPosition(
                    ruleBuilder,
                    processInstance,
                    getSequenceFlowIdOrDescriptiveName(sequenceFlow, this.useSFId)));
    addOutgoingTokensForFlowNodeToProcessInstance(
        intermediateThrowEvent, ruleBuilder, processInstance, useSFId);

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
            sequenceFlow ->
                deleteTokenWithPosition(
                    ruleBuilder,
                    processInstance,
                    getSequenceFlowIdOrDescriptiveName(sequenceFlow, this.useSFId)));
    addOutgoingTokensForFlowNodeToProcessInstance(
        intermediateThrowEvent, ruleBuilder, processInstance, useSFId);

    createSignalThrowRulePart(intermediateThrowEvent.getEventDefinition());
    ruleBuilder.buildRule();
  }

  private void createIntermediateThrowMessageEventRule(
      IntermediateThrowEvent intermediateThrowEvent, String ruleName, AbstractBPMNProcess process) {
    ruleBuilder.startRule(ruleName);
    GrooveNode processInstance = contextProcessInstance(process, ruleBuilder);
    intermediateThrowEvent
        .getIncomingFlows()
        .forEach(
            sequenceFlow ->
                deleteTokenWithPosition(
                    ruleBuilder,
                    processInstance,
                    getSequenceFlowIdOrDescriptiveName(sequenceFlow, this.useSFId)));
    addOutgoingTokensForFlowNodeToProcessInstance(
        intermediateThrowEvent, ruleBuilder, processInstance, useSFId);
    addSendMessageBehaviorForFlowNode(collaboration, ruleBuilder, intermediateThrowEvent, useSFId);

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
            sequenceFlow ->
                deleteTokenWithPosition(
                    ruleBuilder,
                    processInstance,
                    getSequenceFlowIdOrDescriptiveName(sequenceFlow, this.useSFId)));
    addTokenWithPosition(ruleBuilder, processInstance, intermediateThrowEvent.getName());

    ruleBuilder.buildRule();
  }

  private void createSignalThrowRulePart(EventDefinition eventDefinition) {
    Pair<Set<Event>, Set<BoundaryEvent>> correspondingSignalCatchEvents =
        collaboration.findAllCorrespondingSignalCatchEvents(eventDefinition);

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
                    boundarySignalEvent, process, ruleBuilder, forAll, useSFId);

            // Delete token in task if interrupt.
            if (boundarySignalEvent.isInterrupt()) {
              GrooveNode deletedToken =
                  deleteTokenWithPosition(ruleBuilder, processInstance, task.getName());
              ruleBuilder.contextEdge(AT, deletedToken, forAll);
            } else {
              GrooveNode contextToken =
                  contextTokenWithPosition(ruleBuilder, processInstance, task.getName());
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
                    boundarySignalEvent, process, ruleBuilder, forAll, useSFId);

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

      catchSignalEvent
          .getIncomingFlows()
          .forEach(
              inFlow -> {
                String position;
                if (inFlow.getSource().isExclusiveEventBasedGateway()) {
                  position = inFlow.getSource().getName();
                } else {
                  position = getSequenceFlowIdOrDescriptiveName(inFlow, this.useSFId);
                }
                GrooveNode token = ruleBuilder.deleteNode(TYPE_TOKEN);
                ruleBuilder.contextEdge(AT, token, forAll);
                ruleBuilder.deleteEdge(TOKENS, processInstance, token);
                ruleBuilder.deleteEdge(
                    POSITION, token, ruleBuilder.contextNode(createStringNodeLabel(position)));

                addOutgoingTokensForFlowNodeWithNestedRuleQuantifier(
                    catchSignalEvent, forAll, processInstance);
              });
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
                  ruleBuilder.contextNode(
                      createStringNodeLabel(
                          getSequenceFlowIdOrDescriptiveName(outFlow, this.useSFId))));
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
            addOutgoingTokensForFlowNodeToProcessInstance(
                event, ruleBuilder, processInstance, useSFId);
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
              case SIGNAL:
                if (startEvent.isInterrupt()) {
                  createSignalInterruptingStartRulePart(eventSubprocess, event);
                } else {
                  createSignalNonInterruptingStartRulePart(eventSubprocess, event);
                }
                break;
              case NONE:
              case MESSAGE:
              default:
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

  void createStartEventRule(StartEvent startEvent, BPMNProcess process) {
    switch (startEvent.getType()) {
      case NONE:
        createNoneStartEventRule(startEvent, process);
        break;
      case SIGNAL:
      case ERROR:
      case MESSAGE:
      case ESCALATION:
        // Done in the corresponding throw rule.
        break;
    }
  }

  private void createNoneStartEventRule(StartEvent startEvent, BPMNProcess process) {
    ruleBuilder.startRule(startEvent.getName());
    GrooveNode processInstance = contextProcessInstance(process, ruleBuilder);
    addOutgoingTokensForFlowNodeToProcessInstance(
        startEvent, ruleBuilder, processInstance, useSFId);
    deleteTokenWithPosition(
        ruleBuilder, processInstance, getStartEventTokenName(process, startEvent));
    ruleBuilder.buildRule();
  }
}
