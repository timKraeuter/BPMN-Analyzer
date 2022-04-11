package groove.behaviorTransformer.bpmn.generators;

import behavior.bpmn.Process;
import behavior.bpmn.*;
import behavior.bpmn.activities.CallActivity;
import behavior.bpmn.activities.tasks.ReceiveTask;
import behavior.bpmn.activities.tasks.SendTask;
import behavior.bpmn.activities.tasks.Task;
import behavior.bpmn.auxiliary.AbstractProcessVisitor;
import behavior.bpmn.auxiliary.FlowNodeVisitor;
import behavior.bpmn.events.*;
import behavior.bpmn.gateways.EventBasedGateway;
import behavior.bpmn.gateways.ExclusiveGateway;
import behavior.bpmn.gateways.InclusiveGateway;
import behavior.bpmn.gateways.ParallelGateway;
import groove.behaviorTransformer.bpmn.BPMNToGrooveTransformerHelper;
import groove.graph.GrooveNode;
import groove.graph.rule.GrooveRuleBuilder;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

import static groove.behaviorTransformer.GrooveTransformer.*;
import static groove.behaviorTransformer.GrooveTransformerHelper.createStringNodeLabel;
import static groove.behaviorTransformer.bpmn.BPMNToGrooveTransformerConstants.*;
import static groove.behaviorTransformer.bpmn.BPMNToGrooveTransformerHelper.*;

public class BPMNEventRuleGeneratorImpl implements BPMNEventRuleGenerator {
    private final BPMNCollaboration collaboration;
    private final GrooveRuleBuilder ruleBuilder;

    public BPMNEventRuleGeneratorImpl(BPMNCollaboration collaboration, GrooveRuleBuilder ruleBuilder) {
        this.collaboration = collaboration;
        this.ruleBuilder = ruleBuilder;
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
            throw new RuntimeException("End events should have exactly one incoming flow!");
        }
        //noinspection OptionalGetWithoutIsPresent size of the stream is 1
        final String incomingFlowId = endEvent.getIncomingFlows().findFirst().get().getID();
        ruleBuilder.startRule(endEvent.getName());

        GrooveNode processInstance = createProcessInstanceWithName(process, ruleBuilder);

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
                GrooveNode delete_running = ruleBuilder.deleteNode(TYPE_RUNNING);
                ruleBuilder.deleteEdge(STATE, processInstance, delete_running);

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
                BPMNToGrooveTransformerHelper.addOutgoingMessagesForFlowNode(collaboration, ruleBuilder, endEvent);
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
            throw new RuntimeException("Intermediate throw events should have exactly one incoming sequence flow!");
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
                throw new RuntimeException("Unexpected throw event type: " + intermediateThrowEvent.getType());
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
        final GrooveNode processInstance = BPMNToGrooveTransformerHelper.createContextRunningProcessInstance(process,
                                                                                                             ruleBuilder);

        if (intermediateCatchEvent.getIncomingFlows().count() != 1) {
            // current restriction, again we would need implicit exclusive gateway.
            throw new RuntimeException("Intermediate message catch events are only allowed to have one incoming " +
                                               "sequence flow!");
        }
        intermediateCatchEvent.getIncomingFlows().forEach(inFlow -> BPMNToGrooveTransformerHelper.deleteTokenWithPosition(
                ruleBuilder,
                processInstance,
                inFlow.getID()));

        // Add tokens on outgoing flows.
        BPMNToGrooveTransformerHelper.addOutgoingTokensForFlowNodeToProcessInstance(intermediateCatchEvent,
                                                                                    ruleBuilder,
                                                                                    processInstance);

        ruleBuilder.buildRule();
    }

    private void createIntermediateCatchMessageEventRule(IntermediateCatchEvent intermediateCatchEvent,
                                                         AbstractProcess process,
                                                         GrooveRuleBuilder ruleBuilder,
                                                         BPMNCollaboration collaboration) {
        collaboration.getIncomingMessageFlows(intermediateCatchEvent).forEach(messageFlow -> {
            ruleBuilder.startRule(createCatchMessageRuleName(messageFlow, intermediateCatchEvent, collaboration));

            GrooveNode processInstance = addTokensForOutgoingFlowsToRunningInstance(intermediateCatchEvent,
                                                                                    process,
                                                                                    ruleBuilder);
            if (intermediateCatchEvent.getIncomingFlows().count() != 1) {
                // current restriction, again we would need implicit exclusive gateway.
                throw new RuntimeException("Intermediate message catch events are only allowed to have one incoming " + "sequence flow!");
            }
            //noinspection OptionalGetWithoutIsPresent Size of the stream must be 1.
            SequenceFlow incFlow = intermediateCatchEvent.getIncomingFlows().findFirst().get();
            // Consume incoming token
            if (incFlow.getSource().isExclusiveEventBasedGateway()) {
                BPMNToGrooveTransformerHelper.deleteTokenWithPosition(ruleBuilder,
                                                                      processInstance,
                                                                      incFlow.getSource().getName());
            } else {
                BPMNToGrooveTransformerHelper.deleteTokenWithPosition(ruleBuilder, processInstance, incFlow.getID());
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
                                                                                ruleBuilder);
        if (intermediateCatchEvent.getIncomingFlows().findAny().isPresent()) {
            throw new RuntimeException("Link intermediate catch events are not allowed to have incoming sequence " +
                                               "flows!");
        }
        BPMNToGrooveTransformerHelper.deleteTokenWithPosition(ruleBuilder,
                                                              processInstance,
                                                              intermediateCatchEvent.getName());

        ruleBuilder.buildRule();
    }

    private void createIntermediateThrowNoneEventRule(IntermediateThrowEvent intermediateThrowEvent,
                                                      String ruleName,
                                                      GrooveRuleBuilder ruleBuilder,
                                                      AbstractProcess process) {
        ruleBuilder.startRule(ruleName);
        GrooveNode processInstance = BPMNToGrooveTransformerHelper.createContextRunningProcessInstance(process,
                                                                                                       ruleBuilder);
        // We currently limit to one incoming token, but we could implement an implicit exclusive gateway.
        intermediateThrowEvent.getIncomingFlows().forEach(sequenceFlow -> BPMNToGrooveTransformerHelper.deleteTokenWithPosition(
                ruleBuilder,
                processInstance,
                sequenceFlow.getID()));
        BPMNToGrooveTransformerHelper.addOutgoingTokensForFlowNodeToProcessInstance(intermediateThrowEvent,
                                                                                    ruleBuilder,
                                                                                    processInstance);

        ruleBuilder.buildRule();
    }

    private void createIntermediateThrowSignalEventRule(IntermediateThrowEvent intermediateThrowEvent,
                                                        String ruleName,
                                                        AbstractProcess process) {
        ruleBuilder.startRule(ruleName);
        GrooveNode processInstance = BPMNToGrooveTransformerHelper.createContextRunningProcessInstance(process,
                                                                                                       ruleBuilder);
        // We currently limit to one incoming token, but we could implement an implicit exclusive gateway.
        intermediateThrowEvent.getIncomingFlows().forEach(sequenceFlow -> BPMNToGrooveTransformerHelper.deleteTokenWithPosition(
                ruleBuilder,
                processInstance,
                sequenceFlow.getID()));
        BPMNToGrooveTransformerHelper.addOutgoingTokensForFlowNodeToProcessInstance(intermediateThrowEvent,
                                                                                    ruleBuilder,
                                                                                    processInstance);

        createSignalThrowRulePart(intermediateThrowEvent.getEventDefinition());
        ruleBuilder.buildRule();
    }

    private void createIntermediateThrowMessageEventRule(IntermediateThrowEvent intermediateThrowEvent,
                                                         String ruleName,
                                                         AbstractProcess process) {
        ruleBuilder.startRule(ruleName);
        GrooveNode processInstance = BPMNToGrooveTransformerHelper.createContextRunningProcessInstance(process,
                                                                                                       ruleBuilder);
        intermediateThrowEvent.getIncomingFlows().forEach(sequenceFlow -> BPMNToGrooveTransformerHelper.deleteTokenWithPosition(
                ruleBuilder,
                processInstance,
                sequenceFlow.getID()));
        BPMNToGrooveTransformerHelper.addOutgoingTokensForFlowNodeToProcessInstance(intermediateThrowEvent,
                                                                                    ruleBuilder,
                                                                                    processInstance);
        BPMNToGrooveTransformerHelper.addOutgoingMessagesForFlowNode(collaboration, ruleBuilder, intermediateThrowEvent);

        ruleBuilder.buildRule();
    }

    private void createIntermediateThrowLinkEventRule(IntermediateThrowEvent intermediateThrowEvent,
                                                      String ruleName,
                                                      GrooveRuleBuilder ruleBuilder,
                                                      AbstractProcess process) {
        ruleBuilder.startRule(ruleName);
        GrooveNode processInstance = BPMNToGrooveTransformerHelper.createContextRunningProcessInstance(process,
                                                                                                       ruleBuilder);
        intermediateThrowEvent.getIncomingFlows().forEach(sequenceFlow -> BPMNToGrooveTransformerHelper.deleteTokenWithPosition(
                ruleBuilder,
                processInstance,
                sequenceFlow.getID()));
        BPMNToGrooveTransformerHelper.addTokenWithPosition(ruleBuilder,
                                                           processInstance,
                                                           intermediateThrowEvent.getName());

        ruleBuilder.buildRule();
    }

    private void createSignalThrowRulePart(EventDefinition eventDefinition) {
        Set<Event> correspondingSignalCatchEvents = this.findAllCorrespondingSignalCatchEvents(eventDefinition);

        correspondingSignalCatchEvents.forEach(event -> {
            final Process processForEvent = findProcessForEvent(event);
            if (event.isInstantiateFlowNode()) {
                // Create a new process instance.
                GrooveNode processInstance = createNewProcessInstance(ruleBuilder, processForEvent.getName());
                BPMNToGrooveTransformerHelper.addOutgoingTokensForFlowNodeToProcessInstance(event,
                                                                                            ruleBuilder,
                                                                                            processInstance);
            } else {
                // Send a signal only if the process instance exists.
                GrooveNode existsOptional = ruleBuilder.contextNode(EXISTS_OPTIONAL);
                GrooveNode processInstance = createRunningExistsOptionalProcessInstance(ruleBuilder,
                                                                                        processForEvent,
                                                                                        existsOptional);

                event.getIncomingFlows().forEach(inFlow -> {
                    String position;
                    if (inFlow.getSource().isExclusiveEventBasedGateway()) {
                        position = inFlow.getSource().getName();
                    } else {
                        position = inFlow.getID();
                    }
                    GrooveNode token = ruleBuilder.deleteNode(TYPE_TOKEN);
                    ruleBuilder.contextEdge(AT, token, existsOptional);
                    ruleBuilder.deleteEdge(TOKENS, processInstance, token);
                    ruleBuilder.deleteEdge(POSITION, token, ruleBuilder.contextNode(createStringNodeLabel(position)));

                    event.getOutgoingFlows().forEach(outFlow -> {
                        GrooveNode newToken = ruleBuilder.addNode(TYPE_TOKEN);
                        ruleBuilder.contextEdge(AT, newToken, existsOptional);
                        ruleBuilder.addEdge(TOKENS, processInstance, newToken);
                        ruleBuilder.addEdge(POSITION,
                                            newToken,
                                            ruleBuilder.contextNode(createStringNodeLabel(outFlow.getID())));
                    });
                });
            }
        });
    }

    private Process findProcessForEvent(Event event) {
        for (Process participant : collaboration.getParticipants()) {
            final boolean processFound = participant.getControlFlowNodes().anyMatch(flowNode -> flowNode.equals(event));
            if (processFound) {
                return participant;
            }
            Optional<Process> optionalProcess =
                    participant.getSubProcesses().filter(process -> process.getControlFlowNodes().anyMatch(
                    flowNode -> flowNode.equals(event))).findFirst();
            if (optionalProcess.isPresent()) {
                return optionalProcess.get();
            }
        }
        // Should not happen.
        throw new RuntimeException(String.format("No process for the event %s found!", event));
    }

    private Set<Event> findAllCorrespondingSignalCatchEvents(EventDefinition eventDefinition) {
        Set<Event> signalCatchEvents = new LinkedHashSet<>();
        Set<Process> seenProcesses = new HashSet<>();
        collaboration.getParticipants().forEach(process -> signalCatchEvents.addAll(
                findAllCorrespondingSignalCatchEvents(process, eventDefinition, seenProcesses)));
        return signalCatchEvents;
    }

    private Set<Event> findAllCorrespondingSignalCatchEvents(Process process,
                                                             EventDefinition eventDefinition,
                                                             Set<Process> seenProcesses) {
        Set<Event> signalCatchEvents = new LinkedHashSet<>();
        if (seenProcesses.contains(process)) {
            return signalCatchEvents;
        }
        seenProcesses.add(process);

        String globalSignalName = eventDefinition.getGlobalSignalName();
        process.getControlFlowNodes().forEach(flowNode -> flowNode.accept(new FlowNodeVisitor() {
            @Override
            public void handle(Task task) {
                // not relevant
            }

            @Override
            public void handle(SendTask task) {
                // not relevant
            }

            @Override
            public void handle(ReceiveTask task) {
                // not relevant

            }

            @Override
            public void handle(CallActivity callActivity) {
                // not relevant
                signalCatchEvents.addAll(findAllCorrespondingSignalCatchEvents(callActivity.getSubProcessModel(),
                                                                               eventDefinition,
                                                                               seenProcesses));
            }

            @Override
            public void handle(ExclusiveGateway exclusiveGateway) {
                // not relevant
            }

            @Override
            public void handle(ParallelGateway parallelGateway) {
                // not relevant
            }

            @Override
            public void handle(InclusiveGateway inclusiveGateway) {
                // not relevant
            }

            @Override
            public void handle(StartEvent startEvent) {
                if (startEvent.getType() == StartEventType.SIGNAL && startEvent.getEventDefinition().getGlobalSignalName().equals(
                        globalSignalName)) {
                    signalCatchEvents.add(startEvent);
                }
            }

            @Override
            public void handle(IntermediateThrowEvent intermediateThrowEvent) {
                // not relevant
            }

            @Override
            public void handle(IntermediateCatchEvent intermediateCatchEvent) {
                if (intermediateCatchEvent.getType() == IntermediateCatchEventType.SIGNAL && intermediateCatchEvent.getEventDefinition().getGlobalSignalName().equals(
                        globalSignalName)) {
                    signalCatchEvents.add(intermediateCatchEvent);
                }
            }

            @Override
            public void handle(EndEvent endEvent) {
                // not relevant
            }

            @Override
            public void handle(EventBasedGateway eventBasedGateway) {
                // not relevant
            }
        }));
        return signalCatchEvents;
    }

    void createStartEventRule(StartEvent startEvent, Process process) {
        switch (startEvent.getType()) {
            case NONE:
                createNoneStartEventRule(startEvent, process);
                break;
            case MESSAGE:
                createMessageStartEventRule(startEvent);
                break;
            case SIGNAL:
                // Done in the corresponding throw rule.
                break;
        }
    }

    private void createNoneStartEventRule(StartEvent startEvent, Process process) {
        ruleBuilder.startRule(startEvent.getName());
        GrooveNode processInstance = createProcessInstanceAndAddTokens(startEvent, ruleBuilder, process);
        BPMNToGrooveTransformerHelper.deleteTokenWithPosition(ruleBuilder,
                                                              processInstance,
                                                              getStartEventTokenName(process));
        ruleBuilder.buildRule();
    }

    private String getStartEventTokenName(Process process) {
        return process.getName() + "_" + process.getStartEvent().getName();
    }

    private void createMessageStartEventRule(StartEvent startEvent) {
        Set<MessageFlow> incomingMessageFlows = collaboration.getIncomingMessageFlows(startEvent);
        incomingMessageFlows.forEach(incomingMessageFlow -> {
            ruleBuilder.startRule(incomingMessageFlows.size() > 1 ? incomingMessageFlow.getName() :
                                          startEvent.getName());
            GrooveNode processInstance = BPMNToGrooveTransformerHelper.deleteIncomingMessageAndCreateProcessInstance(
                    incomingMessageFlow,
                    collaboration,
                    ruleBuilder);
            addOutgoingTokensForFlowNodeToProcessInstance(startEvent, ruleBuilder, processInstance);
            ruleBuilder.buildRule();
        });
    }
}
