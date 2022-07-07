package groove.behaviortransformer.bpmn.generators;

import behavior.bpmn.Process;
import behavior.bpmn.*;
import behavior.bpmn.activities.Activity;
import behavior.bpmn.activities.CallActivity;
import behavior.bpmn.activities.tasks.AbstractTask;
import behavior.bpmn.activities.tasks.ReceiveTask;
import behavior.bpmn.activities.tasks.SendTask;
import behavior.bpmn.activities.tasks.Task;
import behavior.bpmn.auxiliary.AbstractProcessVisitor;
import behavior.bpmn.auxiliary.ActivityVisitor;
import behavior.bpmn.auxiliary.EventVisitor;
import behavior.bpmn.auxiliary.exceptions.BPMNRuntimeException;
import behavior.bpmn.auxiliary.exceptions.ShouldNotHappenRuntimeException;
import behavior.bpmn.events.*;
import groove.behaviortransformer.bpmn.BPMNRuleGenerator;
import groove.behaviortransformer.bpmn.BPMNToGrooveTransformerHelper;
import groove.graph.GrooveNode;
import groove.graph.rule.GrooveRuleBuilder;
import org.apache.commons.lang3.tuple.Pair;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import static groove.behaviortransformer.GrooveTransformer.*;
import static groove.behaviortransformer.GrooveTransformerHelper.createStringNodeLabel;
import static groove.behaviortransformer.bpmn.BPMNToGrooveTransformerConstants.*;
import static groove.behaviortransformer.bpmn.BPMNToGrooveTransformerHelper.*;

public class BPMNEventRuleGeneratorImpl implements BPMNEventRuleGenerator {
    private final BPMNCollaboration collaboration;
    private final GrooveRuleBuilder ruleBuilder;
    private boolean useSFId;

    public BPMNEventRuleGeneratorImpl(BPMNRuleGenerator bpmnRuleGenerator,
                                      GrooveRuleBuilder ruleBuilder,
                                      boolean useSFIds) {
        this.collaboration = bpmnRuleGenerator.getCollaboration();
        this.ruleBuilder = ruleBuilder;
        this.useSFId = useSFIds;
    }

    @Override
    public void createStartEventRulesForProcess(AbstractProcess process, StartEvent startEvent) {
        process.accept(new AbstractProcessVisitor() {
            @Override
            public void handle(EventSubprocess eventSubprocess) {
                // Handled elsewhere for event subprocesses.
            }

            @Override
            public void handle(Process process) {
                createStartEventRule(startEvent, process);
            }
        });
    }

    @Override
    public void createEndEventRule(AbstractProcess process, EndEvent endEvent) {
        if (endEvent.getIncomingFlows().count() != 1) {
            throw new BPMNRuntimeException("End events should have exactly one incoming flow!");
        }
        SequenceFlow incomingFlow = endEvent.getIncomingFlows().findFirst().orElseThrow(); // Throw never reached due
        // to check before.
        final String incomingFlowId = getSequenceFlowIdOrDescriptiveName(incomingFlow, this.useSFId);
        ruleBuilder.startRule(endEvent.getName());

        GrooveNode processInstance = contextProcessInstanceWithOnlyName(process, ruleBuilder);

        GrooveNode token = ruleBuilder.deleteNode(TYPE_TOKEN);
        GrooveNode position = ruleBuilder.contextNode(createStringNodeLabel(incomingFlowId));
        ruleBuilder.deleteEdge(POSITION, token, position);
        ruleBuilder.deleteEdge(TOKENS, processInstance, token);

        switch (endEvent.getType()) {
            case NONE:
                GrooveNode running = ruleBuilder.contextNode(TYPE_RUNNING);
                ruleBuilder.contextEdge(STATE, processInstance, running);
                break;
            case TERMINATION:
                GrooveNode deletedRunning = ruleBuilder.deleteNode(TYPE_RUNNING);
                ruleBuilder.deleteEdge(STATE, processInstance, deletedRunning);

                GrooveNode terminated = ruleBuilder.addNode(TYPE_TERMINATED);
                ruleBuilder.addEdge(STATE, processInstance, terminated);

                // Terminate possible subprocesses with a nested rule.
                GrooveNode subProcess = ruleBuilder.contextNode(TYPE_PROCESS_SNAPSHOT);
                ruleBuilder.contextEdge(SUBPROCESS, processInstance, subProcess);
                GrooveNode subProcessRunning = ruleBuilder.deleteNode(TYPE_RUNNING);
                ruleBuilder.deleteEdge(STATE, subProcess, subProcessRunning);
                GrooveNode subProcessTerminated = ruleBuilder.addNode(TYPE_TERMINATED);
                ruleBuilder.addEdge(STATE, subProcess, subProcessTerminated);

                GrooveNode forAll = ruleBuilder.contextNode(FORALL);
                ruleBuilder.contextEdge(AT, subProcess, forAll);
                ruleBuilder.contextEdge(AT, subProcessRunning, forAll);
                ruleBuilder.contextEdge(AT, subProcessTerminated, forAll);
                // We could also delete all tokens in the current and all subprocess instances.

                break;
            case MESSAGE:
                BPMNToGrooveTransformerHelper.addMessageFlowBehaviorForFlowNode(collaboration,
                                                                                ruleBuilder,
                                                                                endEvent,
                                                                                this.useSFId);
                break;
            case SIGNAL:
                createSignalThrowRulePart(endEvent.getEventDefinition());
                break;
        }

        ruleBuilder.buildRule();
    }

    @Override
    public void createIntermediateThrowEventRule(AbstractProcess process,
                                                 IntermediateThrowEvent intermediateThrowEvent) {

        String ruleName = THROW + intermediateThrowEvent.getName();
        // We currently limit to one incoming token, but we could implement an implicit exclusive gateway.
        if (intermediateThrowEvent.getIncomingFlows().count() != 1) {
            throw new BPMNRuntimeException("Intermediate throw events should have exactly one incoming sequence flow!");
        }
        switch (intermediateThrowEvent.getType()) {
            case NONE:
                createIntermediateThrowNoneEventRule(intermediateThrowEvent, ruleName, ruleBuilder, process);
                break;
            case LINK:
                createIntermediateThrowLinkEventRule(intermediateThrowEvent, ruleName, ruleBuilder, process);
                break;
            case MESSAGE:
                createIntermediateThrowMessageEventRule(intermediateThrowEvent, ruleName, process);
                break;
            case SIGNAL:
                createIntermediateThrowSignalEventRule(intermediateThrowEvent, ruleName, process);
                break;
            default:
                throw new BPMNRuntimeException("Unexpected throw event type: " + intermediateThrowEvent.getType());
        }
    }

    @Override
    public void createIntermediateCatchEventRule(AbstractProcess process,
                                                 IntermediateCatchEvent intermediateCatchEvent) {
        String ruleName = CATCH + intermediateCatchEvent.getName();
        switch (intermediateCatchEvent.getType()) {
            case LINK:
                createIntermediateCatchLinkEventRule(intermediateCatchEvent, process, ruleName, ruleBuilder);
                break;
            case MESSAGE:
                createIntermediateCatchMessageEventRule(intermediateCatchEvent, process, ruleBuilder, collaboration);
                break;
            case SIGNAL:
                // Done in the corresponding throw rule.
                break;
            case TIMER:
                createIntermediateCatchTimerEventRule(intermediateCatchEvent, process, ruleBuilder);
                break;
        }
    }

    private void createIntermediateCatchTimerEventRule(IntermediateCatchEvent intermediateCatchEvent,
                                                       AbstractProcess process,
                                                       GrooveRuleBuilder ruleBuilder) {

        ruleBuilder.startRule(intermediateCatchEvent.getName());
        final GrooveNode processInstance = BPMNToGrooveTransformerHelper.contextProcessInstance(process, ruleBuilder);

        if (intermediateCatchEvent.getIncomingFlows().count() != 1) {
            // current restriction, again we would need implicit exclusive gateway.
            throw new BPMNRuntimeException("Intermediate message catch events are only allowed to have one incoming " +
                                           "sequence flow!");
        }
        intermediateCatchEvent.getIncomingFlows().forEach(inFlow -> deleteTokenWithPosition(ruleBuilder,
                                                                                            processInstance,
                                                                                            getSequenceFlowIdOrDescriptiveName(
                                                                                                    inFlow,
                                                                                                    this.useSFId)));

        // Add tokens on outgoing flows.
        BPMNToGrooveTransformerHelper.addOutgoingTokensForFlowNodeToProcessInstance(intermediateCatchEvent,
                                                                                    ruleBuilder,
                                                                                    processInstance,
                                                                                    this.useSFId);

        ruleBuilder.buildRule();
    }

    private void createIntermediateCatchMessageEventRule(IntermediateCatchEvent intermediateCatchEvent,
                                                         AbstractProcess process,
                                                         GrooveRuleBuilder ruleBuilder,
                                                         BPMNCollaboration collaboration) {
        if (BPMNToGrooveTransformerHelper.isAfterInstantiateEventBasedGateway(intermediateCatchEvent)) {
            // Not needed to create rules in this case.
            return;
        }

        collaboration.getIncomingMessageFlows(intermediateCatchEvent).forEach(messageFlow -> {
            ruleBuilder.startRule(createCatchMessageRuleName(messageFlow, intermediateCatchEvent, collaboration));

            GrooveNode processInstance = addTokensForOutgoingFlowsToRunningInstance(intermediateCatchEvent,
                                                                                    process,
                                                                                    ruleBuilder, useSFId);
            if (intermediateCatchEvent.getIncomingFlows().count() != 1) {
                // current restriction, again we would need implicit exclusive gateway.
                throw new BPMNRuntimeException(
                        "Intermediate message catch events are only allowed to have one incoming " + "sequence flow!");
            }
            //noinspection OptionalGetWithoutIsPresent Size of the stream must be 1.
            SequenceFlow incFlow = intermediateCatchEvent.getIncomingFlows().findFirst().get();
            // Consume incoming token
            if (incFlow.getSource().isExclusiveEventBasedGateway()) {
                deleteTokenWithPosition(ruleBuilder, processInstance, incFlow.getSource().getName());
            } else {
                deleteTokenWithPosition(ruleBuilder,
                                        processInstance,
                                        getSequenceFlowIdOrDescriptiveName(incFlow, this.useSFId));
            }
            // Consume incoming message.
            final GrooveNode deletedMessage = BPMNToGrooveTransformerHelper.deleteMessageToProcessInstanceWithPosition(
                    ruleBuilder,
                    processInstance,
                    messageFlow.getName());
            // Delete all other incoming messages.
            final GrooveNode forAll = ruleBuilder.contextNode(FORALL);
            GrooveNode message = ruleBuilder.deleteNode(TYPE_MESSAGE);
            ruleBuilder.deleteEdge(MESSAGES, processInstance, message);
            ruleBuilder.contextEdge(AT, message, forAll);
            ruleBuilder.contextEdge(UNEQUALS, message, deletedMessage);

            ruleBuilder.buildRule();

        });
    }

    private String createCatchMessageRuleName(MessageFlow messageFlow,
                                              IntermediateCatchEvent intermediateCatchEvent,
                                              BPMNCollaboration collaboration) {
        final int amountOfIncMessages = collaboration.getIncomingMessageFlows(intermediateCatchEvent).size();
        if (amountOfIncMessages <= 1) {
            return intermediateCatchEvent.getName();
        }
        return intermediateCatchEvent.getName() + "_" + messageFlow.getName();
    }

    private void createIntermediateCatchLinkEventRule(IntermediateCatchEvent intermediateCatchEvent,
                                                      AbstractProcess process,
                                                      String ruleName,
                                                      GrooveRuleBuilder ruleBuilder) {
        ruleBuilder.startRule(ruleName);

        GrooveNode processInstance = addTokensForOutgoingFlowsToRunningInstance(intermediateCatchEvent,
                                                                                process,
                                                                                ruleBuilder, useSFId);
        if (intermediateCatchEvent.getIncomingFlows().findAny().isPresent()) {
            throw new BPMNRuntimeException("Link intermediate catch events are not allowed to have incoming sequence " +
                                           "flows!");
        }
        deleteTokenWithPosition(ruleBuilder, processInstance, intermediateCatchEvent.getName());

        ruleBuilder.buildRule();
    }

    private void createIntermediateThrowNoneEventRule(IntermediateThrowEvent intermediateThrowEvent,
                                                      String ruleName,
                                                      GrooveRuleBuilder ruleBuilder,
                                                      AbstractProcess process) {
        ruleBuilder.startRule(ruleName);
        GrooveNode processInstance = BPMNToGrooveTransformerHelper.contextProcessInstance(process, ruleBuilder);
        // We currently limit to one incoming token, but we could implement an implicit exclusive gateway.
        intermediateThrowEvent.getIncomingFlows().forEach(sequenceFlow -> deleteTokenWithPosition(ruleBuilder,
                                                                                                  processInstance,
                                                                                                  getSequenceFlowIdOrDescriptiveName(
                                                                                                          sequenceFlow,
                                                                                                          this.useSFId)));
        BPMNToGrooveTransformerHelper.addOutgoingTokensForFlowNodeToProcessInstance(intermediateThrowEvent,
                                                                                    ruleBuilder,
                                                                                    processInstance,
                                                                                    useSFId);

        ruleBuilder.buildRule();
    }

    private void createIntermediateThrowSignalEventRule(IntermediateThrowEvent intermediateThrowEvent,
                                                        String ruleName,
                                                        AbstractProcess process) {
        ruleBuilder.startRule(ruleName);
        GrooveNode processInstance = BPMNToGrooveTransformerHelper.contextProcessInstance(process, ruleBuilder);
        // We currently limit to one incoming token, but we could implement an implicit exclusive gateway.
        intermediateThrowEvent.getIncomingFlows().forEach(sequenceFlow -> deleteTokenWithPosition(ruleBuilder,
                                                                                                  processInstance,
                                                                                                  getSequenceFlowIdOrDescriptiveName(
                                                                                                          sequenceFlow,
                                                                                                          this.useSFId)));
        BPMNToGrooveTransformerHelper.addOutgoingTokensForFlowNodeToProcessInstance(intermediateThrowEvent,
                                                                                    ruleBuilder,
                                                                                    processInstance,
                                                                                    useSFId);

        createSignalThrowRulePart(intermediateThrowEvent.getEventDefinition());
        ruleBuilder.buildRule();
    }

    private void createIntermediateThrowMessageEventRule(IntermediateThrowEvent intermediateThrowEvent,
                                                         String ruleName,
                                                         AbstractProcess process) {
        ruleBuilder.startRule(ruleName);
        GrooveNode processInstance = BPMNToGrooveTransformerHelper.contextProcessInstance(process, ruleBuilder);
        intermediateThrowEvent.getIncomingFlows().forEach(sequenceFlow -> deleteTokenWithPosition(ruleBuilder,
                                                                                                  processInstance,
                                                                                                  getSequenceFlowIdOrDescriptiveName(
                                                                                                          sequenceFlow,
                                                                                                          this.useSFId)));
        BPMNToGrooveTransformerHelper.addOutgoingTokensForFlowNodeToProcessInstance(intermediateThrowEvent,
                                                                                    ruleBuilder,
                                                                                    processInstance,
                                                                                    useSFId);
        BPMNToGrooveTransformerHelper.addMessageFlowBehaviorForFlowNode(collaboration,
                                                                        ruleBuilder,
                                                                        intermediateThrowEvent,
                                                                        useSFId);

        ruleBuilder.buildRule();
    }

    private void createIntermediateThrowLinkEventRule(IntermediateThrowEvent intermediateThrowEvent,
                                                      String ruleName,
                                                      GrooveRuleBuilder ruleBuilder,
                                                      AbstractProcess process) {
        ruleBuilder.startRule(ruleName);
        GrooveNode processInstance = BPMNToGrooveTransformerHelper.contextProcessInstance(process, ruleBuilder);
        intermediateThrowEvent.getIncomingFlows().forEach(sequenceFlow -> deleteTokenWithPosition(ruleBuilder,
                                                                                                  processInstance,
                                                                                                  getSequenceFlowIdOrDescriptiveName(
                                                                                                          sequenceFlow,
                                                                                                          this.useSFId)));
        BPMNToGrooveTransformerHelper.addTokenWithPosition(ruleBuilder,
                                                           processInstance,
                                                           intermediateThrowEvent.getName());

        ruleBuilder.buildRule();
    }

    private void createSignalThrowRulePart(EventDefinition eventDefinition) {
        Pair<Set<Event>, Set<BoundaryEvent>> correspondingSignalCatchEvents =
                this.findAllCorrespondingSignalCatchEvents(
                eventDefinition);

        correspondingSignalCatchEvents.getLeft().forEach(this::createCatchSignalEventRulePart);
        correspondingSignalCatchEvents.getRight().forEach(this::createBoundarySignalCatchEventRulePart);
    }

    private void createBoundarySignalCatchEventRulePart(BoundaryEvent boundarySignalEvent) {
        Activity activity = boundarySignalEvent.getAttachedTo();
        activity.accept(new ActivityVisitor() {
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
                AbstractProcess process = collaboration.findProcessForFlowNode(task);
                // Must also be forAll
                GrooveNode processInstance = addTokensForOutgoingFlowsToRunningInstanceWithQuantifier(
                        boundarySignalEvent,
                        process,
                        ruleBuilder,
                        forAll, useSFId);

                // Delete token in task if interrupt.
                if (boundarySignalEvent.isInterrupt()) {
                    GrooveNode deletedToken = deleteTokenWithPosition(ruleBuilder, processInstance, task.getName());
                    ruleBuilder.contextEdge(AT, deletedToken, forAll);
                } else {
                    GrooveNode contextToken = contextTokenWithPosition(ruleBuilder, processInstance, task.getName());
                    ruleBuilder.contextEdge(AT, contextToken, forAll);
                }
            }

            @Override
            public void handle(CallActivity callActivity) {
                // Multiple boundary events can be triggered
                GrooveNode forAll = ruleBuilder.contextNode(FORALL);
                AbstractProcess process = collaboration.findProcessForFlowNode(callActivity);
                GrooveNode processInstance =
                        BPMNToGrooveTransformerHelper.addTokensForOutgoingFlowsToRunningInstanceWithQuantifier(
                                boundarySignalEvent,
                                process,
                                ruleBuilder,
                                forAll, useSFId);

                if (boundarySignalEvent.isInterrupt()) {
                    interruptSubprocess(ruleBuilder, callActivity, processInstance, forAll);
                } else {
                    // Subprocess must be running
                    GrooveNode subprocessInstance = BPMNToGrooveTransformerHelper.contextProcessInstanceWithQuantifier(
                            callActivity.getSubProcessModel(),
                            ruleBuilder,
                            forAll);
                    ruleBuilder.contextEdge(SUBPROCESS, processInstance, subprocessInstance);
                }
            }
        });
    }

    private void createCatchSignalEventRulePart(Event catchSignalEvent) {
        final AbstractProcess processForEvent = collaboration.findProcessForFlowNode(catchSignalEvent);
        if (catchSignalEvent.isInstantiateFlowNode() || isAfterInstantiateEventBasedGateway(catchSignalEvent)) {
            createSignalThrowInstantiateRulePart(catchSignalEvent, processForEvent);
        } else {
            // Send a signal only if the process instance exists.
            GrooveNode existsOptional = ruleBuilder.contextNode(EXISTS_OPTIONAL);
            GrooveNode processInstance = contextProcessInstanceWithQuantifier(processForEvent,
                                                                              ruleBuilder,
                                                                              existsOptional);

            catchSignalEvent.getIncomingFlows().forEach(inFlow -> {
                String position;
                if (inFlow.getSource().isExclusiveEventBasedGateway()) {
                    position = inFlow.getSource().getName();
                } else {
                    position = getSequenceFlowIdOrDescriptiveName(inFlow, this.useSFId);
                }
                GrooveNode token = ruleBuilder.deleteNode(TYPE_TOKEN);
                ruleBuilder.contextEdge(AT, token, existsOptional);
                ruleBuilder.deleteEdge(TOKENS, processInstance, token);
                ruleBuilder.deleteEdge(POSITION, token, ruleBuilder.contextNode(createStringNodeLabel(position)));

                addExistsOptionalOutgoingTokensForFlowNode(catchSignalEvent, existsOptional, processInstance);
            });
        }
    }

    private void addExistsOptionalOutgoingTokensForFlowNode(Event event,
                                                            GrooveNode existsOptional,
                                                            GrooveNode processInstance) {
        event.getOutgoingFlows().forEach(outFlow -> {
            GrooveNode newToken = ruleBuilder.addNode(TYPE_TOKEN);
            ruleBuilder.contextEdge(AT, newToken, existsOptional);
            ruleBuilder.addEdge(TOKENS, processInstance, newToken);
            ruleBuilder.addEdge(POSITION,
                                newToken,
                                ruleBuilder.contextNode(createStringNodeLabel(getSequenceFlowIdOrDescriptiveName(outFlow,
                                                                                                                 this.useSFId))));
        });
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
                GrooveNode processInstance = addProcessInstance(ruleBuilder, processForEvent.getName());
                BPMNToGrooveTransformerHelper.addOutgoingTokensForFlowNodeToProcessInstance(event,
                                                                                            ruleBuilder,
                                                                                            processInstance,
                                                                                            useSFId);
            }
        });
    }

    private void createSignalThrowInstantiateRulePartForEventSubprocess(EventSubprocess eventSubprocess, Event event) {
        event.accept(new EventVisitor() {
            @Override
            public void handle(StartEvent startEvent) {
                switch (startEvent.getType()) {
                    case SIGNAL:
                        createSignalInterruptingStartRulePart(eventSubprocess, event);
                        break;
                    case SIGNAL_NON_INTERRUPTING:
                        createSignalNonInterruptingStartRulePart(eventSubprocess, event);
                        break;
                    // Must be a signal event!
                    case NONE:
                    case MESSAGE:
                    case MESSAGE_NON_INTERRUPTING:
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

    private void createSignalInterruptingStartRulePart(EventSubprocess eventSubprocess, Event event) {
        // Everything is optional if a parent process exists.
        GrooveNode existsOptional = ruleBuilder.contextNode(EXISTS_OPTIONAL);

        // Needs a running parent process
        GrooveNode parentProcessInstance = contextExistsOptionalParentProcess(eventSubprocess, existsOptional);

        // Start new subprocess instance of process
        GrooveNode eventSubProcessInstance = startNewEventSubprocess(existsOptional,
                                                                     parentProcessInstance,
                                                                     eventSubprocess);
        // Create start tokens
        addExistsOptionalOutgoingTokensForFlowNode(event, existsOptional, eventSubProcessInstance);

        // Interrupt parent process means deleting all its tokens.
        GrooveNode forAll = deleteAllTokensForProcess(ruleBuilder, parentProcessInstance);
        ruleBuilder.contextEdge(IN, forAll, existsOptional);
    }

    private void createSignalNonInterruptingStartRulePart(EventSubprocess eventSubprocess, Event event) {
        // Everything is optional if a parent process exists.
        GrooveNode existsOptional = ruleBuilder.contextNode(EXISTS_OPTIONAL);

        // Needs a running parent process
        GrooveNode parentProcessInstance = contextExistsOptionalParentProcess(eventSubprocess, existsOptional);

        // Start new subprocess instance of process
        GrooveNode eventSubProcessInstance = startNewEventSubprocess(existsOptional,
                                                                     parentProcessInstance,
                                                                     eventSubprocess);
        // Create start tokens
        addExistsOptionalOutgoingTokensForFlowNode(event, existsOptional, eventSubProcessInstance);
    }

    private GrooveNode startNewEventSubprocess(GrooveNode existsOptional,
                                               GrooveNode parentProcessInstance,
                                               EventSubprocess eventSubprocess) {
        GrooveNode eventSubProcessInstance = BPMNToGrooveTransformerHelper.addProcessInstanceWithQuantifier(ruleBuilder,
                                                                                                            eventSubprocess.getName(),
                                                                                                            existsOptional);
        ruleBuilder.addEdge(SUBPROCESS, parentProcessInstance, eventSubProcessInstance);
        return eventSubProcessInstance;
    }

    private GrooveNode contextExistsOptionalParentProcess(EventSubprocess eventSubprocess, GrooveNode existsOptional) {
        AbstractProcess parentProcess = collaboration.getParentProcessForEventSubprocess(eventSubprocess);
        return BPMNToGrooveTransformerHelper.contextProcessInstanceWithQuantifier(parentProcess,
                                                                                  ruleBuilder,
                                                                                  existsOptional);
    }

    private Pair<Set<Event>, Set<BoundaryEvent>> findAllCorrespondingSignalCatchEvents(EventDefinition eventDefinition) {
        Set<Event> signalCatchEvents = new LinkedHashSet<>();
        Set<BoundaryEvent> signalBoundaryCatchEvents = new LinkedHashSet<>();
        Set<Process> seenProcesses = new HashSet<>();
        collaboration.getParticipants().forEach(process -> {
            Pair<Set<Event>, Set<BoundaryEvent>> signalAndSignalBoundaryCatchEvents =
                    findAllCorrespondingSignalCatchEvents(
                    process,
                    eventDefinition,
                    seenProcesses);
            signalCatchEvents.addAll(signalAndSignalBoundaryCatchEvents.getLeft());
            signalBoundaryCatchEvents.addAll(signalAndSignalBoundaryCatchEvents.getRight());
        });
        return Pair.of(signalCatchEvents, signalBoundaryCatchEvents);
    }

    Pair<Set<Event>, Set<BoundaryEvent>> findAllCorrespondingSignalCatchEvents(Process process,
                                                                               EventDefinition eventDefinition,
                                                                               Set<Process> seenProcesses) {
        Set<Event> signalCatchEvents = new LinkedHashSet<>();
        Set<BoundaryEvent> signalBoundaryCatchEvents = new LinkedHashSet<>();
        if (seenProcesses.contains(process)) {
            return Pair.of(signalCatchEvents, signalBoundaryCatchEvents);
        }
        seenProcesses.add(process);

        process.getFlowNodes().forEach(flowNode -> flowNode.accept(new SignalCatchEventFlowNodeVisitor(this,
                                                                                                       eventDefinition,
                                                                                                       signalCatchEvents,
                                                                                                       signalBoundaryCatchEvents,
                                                                                                       seenProcesses)

        ));
        process.getEventSubprocesses().forEach(eventSubprocess -> eventSubprocess.getFlowNodes().forEach(flowNode -> flowNode.accept(
                new SignalCatchEventFlowNodeVisitor(this,
                                                    eventDefinition,
                                                    signalCatchEvents,
                                                    signalBoundaryCatchEvents,
                                                    seenProcesses))));
        return Pair.of(signalCatchEvents, signalBoundaryCatchEvents);
    }

    void createStartEventRule(StartEvent startEvent, Process process) {
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

    private void createNoneStartEventRule(StartEvent startEvent, Process process) {
        ruleBuilder.startRule(startEvent.getName());
        GrooveNode processInstance = contextProcessInstance(process, ruleBuilder);
        addOutgoingTokensForFlowNodeToProcessInstance(startEvent, ruleBuilder, processInstance, useSFId);
        deleteTokenWithPosition(ruleBuilder, processInstance, getStartEventTokenName(process, startEvent));
        ruleBuilder.buildRule();
    }

    private void createMessageStartEventRule(StartEvent startEvent) {
        Set<MessageFlow> incomingMessageFlows = collaboration.getIncomingMessageFlows(startEvent);
        incomingMessageFlows.forEach(incomingMessageFlow -> {
            ruleBuilder.startRule(incomingMessageFlows.size() >
                                  1 ? incomingMessageFlow.getName() : startEvent.getName());
            GrooveNode processInstance = BPMNToGrooveTransformerHelper.deleteIncomingMessageAndCreateProcessInstance(
                    incomingMessageFlow,
                    collaboration,
                    ruleBuilder);
            addOutgoingTokensForFlowNodeToProcessInstance(startEvent, ruleBuilder, processInstance, useSFId);
            ruleBuilder.buildRule();
        });
    }

}
