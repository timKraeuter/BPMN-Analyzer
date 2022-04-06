package behavior.bpmn.reader;

import behavior.bpmn.BPMNCollaboration;
import behavior.bpmn.activities.CallActivity;
import behavior.bpmn.activities.tasks.ReceiveTask;
import behavior.bpmn.activities.tasks.SendTask;
import behavior.bpmn.activities.tasks.Task;
import behavior.bpmn.auxiliary.BPMNCollaborationBuilder;
import behavior.bpmn.auxiliary.BPMNModelBuilder;
import behavior.bpmn.auxiliary.BPMNProcessBuilder;
import behavior.bpmn.events.EndEvent;
import behavior.bpmn.events.IntermediateCatchEvent;
import behavior.bpmn.events.IntermediateThrowEvent;
import behavior.bpmn.events.StartEvent;
import behavior.bpmn.events.*;
import behavior.bpmn.gateways.EventBasedGateway;
import behavior.bpmn.gateways.ExclusiveGateway;
import behavior.bpmn.gateways.InclusiveGateway;
import behavior.bpmn.gateways.ParallelGateway;
import org.apache.commons.io.FilenameUtils;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.EventDefinition;
import org.camunda.bpm.model.bpmn.instance.*;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaProperties;
import org.camunda.bpm.model.xml.instance.ModelElementInstance;
import org.camunda.bpm.model.xml.type.ModelElementType;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class BPMNFileReader {

    public BPMNCollaboration readModelFromFile(File file) {
        BpmnModelInstance bpmnModelInstance = Bpmn.readModelFromFile(file);

        String fileName = FilenameUtils.removeExtension(file.getName());
        BPMNCollaborationBuilder bpmnCollaborationBuilder = new BPMNCollaborationBuilder()
                .name(fileName);

        Map<String, behavior.bpmn.FlowNode> mappedFlowNodes = new HashMap<>();
        Map<String, Boolean> sequenceFlowsMapped = new HashMap<>();

        // Map subprocesses
        ModelElementType subprocessType = bpmnModelInstance.getModel().getType(SubProcess.class);
        Collection<ModelElementInstance> subprocesses = bpmnModelInstance.getModelElementsByType(subprocessType);
        subprocesses.forEach(subProcessModelElement -> {
            SubProcess subProcess = (SubProcess) subProcessModelElement;
            behavior.bpmn.FlowNode mappedSubprocessIfExists = mappedFlowNodes.get(subProcess.getId());
            if (mappedSubprocessIfExists == null) {
                mapSubProcess(subProcess, mappedFlowNodes, sequenceFlowsMapped);
            }
        });

        // Map participants/pools
        ModelElementType participantType = bpmnModelInstance.getModel().getType(Participant.class);
        Collection<ModelElementInstance> participants = bpmnModelInstance.getModelElementsByType(participantType);
        if (participants.isEmpty()) {
            bpmnCollaborationBuilder.processName(fileName);
            mapModelInstanceToOneParticipant(
                    bpmnModelInstance,
                    bpmnCollaborationBuilder,
                    mappedFlowNodes,
                    sequenceFlowsMapped);
        } else {
            // Map each participant
            participants.forEach(modelElementInstance ->
                    mapParticipant(
                            bpmnCollaborationBuilder,
                            mappedFlowNodes,
                            sequenceFlowsMapped,
                            (Participant) modelElementInstance));
            // Map message flows
            mapMessageFlows(bpmnModelInstance, bpmnCollaborationBuilder, mappedFlowNodes, sequenceFlowsMapped);
        }
        return bpmnCollaborationBuilder.build();
    }

    private void mapMessageFlows(
            BpmnModelInstance bpmnModelInstance,
            BPMNCollaborationBuilder bpmnCollaborationBuilder,
            Map<String, behavior.bpmn.FlowNode> mappedFlowNodes,
            Map<String, Boolean> sequenceFlowsMapped) {
        ModelElementType mfType = bpmnModelInstance.getModel().getType(MessageFlow.class);
        Collection<ModelElementInstance> messageFlowModelElements = bpmnModelInstance.getModelElementsByType(mfType);
        messageFlowModelElements.forEach(modelElementInstance ->
                mapMessageFlow((MessageFlow) modelElementInstance, bpmnCollaborationBuilder, mappedFlowNodes, sequenceFlowsMapped));
    }

    private void mapMessageFlow(
            MessageFlow messageFlow,
            BPMNCollaborationBuilder bpmnCollaborationBuilder,
            Map<String, behavior.bpmn.FlowNode> mappedFlowNodes,
            Map<String, Boolean> sequenceFlowsMapped) {
        InteractionNode sourceInteractionNode = messageFlow.getSource();
        InteractionNode targetInteractionNode = messageFlow.getTarget();
        if (!(sourceInteractionNode instanceof FlowNode)) {
            throw new RuntimeException(String.format(
                    "Message flow with id \"%s\" has an invalid source with id \"%s\", which is not a flow node (event, activity, ...)!",
                    messageFlow.getId(),
                    sourceInteractionNode.getId()));
        }
        if (!(targetInteractionNode instanceof FlowNode)) {
            throw new RuntimeException(String.format(
                    "Message flow with id \"%s\" has an invalid target with id \"%s\", which is not a flow node (event, activity, ...)!",
                    messageFlow.getId(),
                    targetInteractionNode.getId()));
        }
        FlowNode source = (FlowNode) sourceInteractionNode;
        FlowNode target = (FlowNode) targetInteractionNode;
        bpmnCollaborationBuilder.messageFlow(
                mapFlowNode(source, mappedFlowNodes, sequenceFlowsMapped, bpmnCollaborationBuilder),
                mapFlowNode(target, mappedFlowNodes, sequenceFlowsMapped, bpmnCollaborationBuilder));
    }

    private void mapModelInstanceToOneParticipant(
            BpmnModelInstance bpmnModelInstance,
            BPMNCollaborationBuilder bpmnCollaborationBuilder,
            Map<String, behavior.bpmn.FlowNode> mappedFlowNodes,
            Map<String, Boolean> sequenceFlowsMapped) {
        ModelElementType fnType = bpmnModelInstance.getModel().getType(FlowNode.class);
        Collection<ModelElementInstance> flowNodeModelElements = bpmnModelInstance.getModelElementsByType(fnType);
        flowNodeModelElements.forEach(flowNodeModelElement -> {
            FlowNode flowNode = (FlowNode) flowNodeModelElement;
            mapFlowNode(flowNode, mappedFlowNodes, sequenceFlowsMapped, bpmnCollaborationBuilder);
        });
        ModelElementType sfType = bpmnModelInstance.getModel().getType(SequenceFlow.class);
        Collection<ModelElementInstance> sequenceFlowModelElements = bpmnModelInstance.getModelElementsByType(sfType);
        sequenceFlowModelElements.forEach(sequenceFlowModelElement -> {
            SequenceFlow sequenceFlow = (SequenceFlow) sequenceFlowModelElement;
            mapSequenceFlow(sequenceFlow, mappedFlowNodes, sequenceFlowsMapped, bpmnCollaborationBuilder);
        });
    }

    private void mapParticipant(
            BPMNCollaborationBuilder bpmnCollaborationBuilder,
            Map<String, behavior.bpmn.FlowNode> createdFlowNodes,
            Map<String, Boolean> sequenceFlowsMapped,
            Participant participant) {
        bpmnCollaborationBuilder.processName(participant.getName());
        participant.getProcess().getFlowElements().forEach(flowElement ->
                mapFlowElement(bpmnCollaborationBuilder, createdFlowNodes, sequenceFlowsMapped, flowElement));
        bpmnCollaborationBuilder.buildProcess();
    }

    private void mapFlowElement(
            BPMNModelBuilder bpmnModelBuilder,
            Map<String, behavior.bpmn.FlowNode> mappedFlowNodes,
            Map<String, Boolean> sequenceFlowsMapped,
            FlowElement flowElement) {
        // Map flow nodes
        if (flowElement instanceof FlowNode) {
            mapFlowNode((FlowNode) flowElement, mappedFlowNodes, sequenceFlowsMapped, bpmnModelBuilder);
        }
        // Map sequence flows
        if (flowElement instanceof SequenceFlow) {
            SequenceFlow sequenceFlow = (SequenceFlow) flowElement;
            mapSequenceFlow(sequenceFlow, mappedFlowNodes, sequenceFlowsMapped, bpmnModelBuilder);
        }
    }

    private void mapSequenceFlow(
            SequenceFlow sequenceFlow,
            Map<String, behavior.bpmn.FlowNode> mappedFlowNodes,
            Map<String, Boolean> sequenceFlowsMapped,
            BPMNModelBuilder bpmnModelBuilder) {
        Boolean sequenceFlowMapped = sequenceFlowsMapped.get(sequenceFlow.getId());
        if (sequenceFlowMapped != null && sequenceFlowMapped) {
            // Sequence flow has been mapped already.
            return;
        }
        if (sequenceFlow.getName() == null || sequenceFlow.getName().isEmpty()) {
            bpmnModelBuilder.sequenceFlow(
                    mapFlowNode(
                            sequenceFlow.getSource(),
                            mappedFlowNodes,
                            sequenceFlowsMapped,
                            bpmnModelBuilder),
                    mapFlowNode(
                            sequenceFlow.getTarget(),
                            mappedFlowNodes,
                            sequenceFlowsMapped,
                            bpmnModelBuilder));
        } else {
            bpmnModelBuilder.sequenceFlow(
                    sequenceFlow.getName(),
                    mapFlowNode(
                            sequenceFlow.getSource(),
                            mappedFlowNodes,
                            sequenceFlowsMapped,
                            bpmnModelBuilder),
                    mapFlowNode(
                            sequenceFlow.getTarget(),
                            mappedFlowNodes,
                            sequenceFlowsMapped,
                            bpmnModelBuilder));
        }
        sequenceFlowsMapped.put(sequenceFlow.getId(), true);
    }

    private behavior.bpmn.FlowNode mapFlowNode(
            FlowNode flowNode,
            Map<String, behavior.bpmn.FlowNode> mappedFlowNodes,
            Map<String, Boolean> sequenceFlowsMapped,
            BPMNModelBuilder bpmnModelBuilder) {
        behavior.bpmn.FlowNode flowNodeIfExists = mappedFlowNodes.get(flowNode.getId());
        if (flowNodeIfExists != null) {
            return flowNodeIfExists;
        }
        String taskTypeName = flowNode.getElementType().getTypeName();
        // TODO: We probably need to save the ID somehow or use it instead of the name!
        behavior.bpmn.FlowNode resultingFlowNode;
        switch (taskTypeName) {
            // Events
            case "startEvent":
                StartEvent startEvent = mapStartEvent(flowNode);
                setMostAppropriateStartEvent(bpmnModelBuilder, startEvent);
                resultingFlowNode = startEvent;
                break;
            case "intermediateThrowEvent":
                resultingFlowNode = this.mapIntermediateThrowEvent(flowNode);
                break;
            case "intermediateCatchEvent":
                resultingFlowNode = this.mapIntermediateCatchEvent(flowNode);
                break;
            case "endEvent":
                resultingFlowNode = mapEndEvent(flowNode);
                break;
            // Tasks
            case "businessRuleTask":
            case "scriptTask":
            case "serviceTask":
            case "manualTask":
            case "userTask":
            case "task":
                resultingFlowNode = new Task(flowNode.getName());
                break;
            case "sendTask":
                resultingFlowNode = new SendTask(flowNode.getName());
                break;
            case "receiveTask":
                boolean instantiate = isInstantiateReceiveTask(flowNode);
                resultingFlowNode = new ReceiveTask(flowNode.getName(), instantiate);
                break;
            case "subProcess":
                resultingFlowNode = mapSubProcess((SubProcess) flowNode, mappedFlowNodes, sequenceFlowsMapped);
                break;
            case "callActivity":
                // Call Activity = Reusable sub-processes (external).
                throw new RuntimeException("External subprocesses currently not supported!");
                // Gateways
            case "parallelGateway":
                resultingFlowNode = new ParallelGateway(flowNode.getName());
                break;
            case "exclusiveGateway":
                resultingFlowNode = new ExclusiveGateway(flowNode.getName());
                break;
            case "eventBasedGateway":
                resultingFlowNode = new EventBasedGateway(flowNode.getName());
                break;
            case "inclusiveGateway":
                resultingFlowNode = new InclusiveGateway(flowNode.getName());
                break;
            default:
                throw new RuntimeException(String.format("Unknown task type \"%s\" found!", taskTypeName));
        }
        mappedFlowNodes.put(flowNode.getId(), resultingFlowNode);
        return resultingFlowNode;
    }

    private CallActivity mapSubProcess(
            SubProcess subprocess,
            Map<String, behavior.bpmn.FlowNode> mappedFlowNodes,
            Map<String, Boolean> sequenceFlowsMapped) {
        BPMNProcessBuilder subprocessBuilder = new BPMNProcessBuilder()
                .name(subprocess.getName());
        subprocess.getFlowElements().forEach(flowElement ->
                mapFlowElement(subprocessBuilder, mappedFlowNodes, sequenceFlowsMapped, flowElement));

        CallActivity subProcessCallActivity = new CallActivity(subprocessBuilder.build());
        mappedFlowNodes.put(subprocess.getId(), subProcessCallActivity);
        return subProcessCallActivity;
    }

    private void setMostAppropriateStartEvent(
            BPMNModelBuilder bpmnModelBuilder,
            StartEvent startEvent) {
        StartEvent currentStartEvent = bpmnModelBuilder.getStartEvent();
        // Prioritizes none start events over message
        if (currentStartEvent == null || currentStartEvent.getType() != StartEventType.NONE) {
            bpmnModelBuilder.startEvent(startEvent);
            return;
        }
        if (currentStartEvent.getType() == startEvent.getType()) {
            // TODO: Multiple none start events?
            throw new RuntimeException("Multiple none start events are currently not supported!");
        }
    }

    private boolean isInstantiateReceiveTask(FlowNode flowNode) {
        ExtensionElements extensionElements = flowNode.getExtensionElements();
        if (extensionElements == null) {
            return false;
        }
        // Instantiate is set as a camunda property using the camunda modeler.
        CamundaProperties properties = extensionElements
                .getElementsQuery()
                .filterByType(CamundaProperties.class)
                .singleResult();

        return properties.getCamundaProperties().stream().anyMatch(camundaProperty ->
                camundaProperty.getCamundaName().equals("instantiate")
                        && camundaProperty.getCamundaValue().equals("true")
        );
    }

    private EndEvent mapEndEvent(FlowNode flowNode) {
        org.camunda.bpm.model.bpmn.instance.EndEvent endEvent = (org.camunda.bpm.model.bpmn.instance.EndEvent) flowNode;
        Collection<EventDefinition> eventDefinitions = endEvent.getEventDefinitions();
        if (eventDefinitions.isEmpty()) {
            return new EndEvent(flowNode.getName());
        }
        if (eventDefinitions.size() == 1) {
            EventDefinition eventDefinition = eventDefinitions.iterator().next();
            EventDefinitionVisitor<EndEvent> visitor = new EventDefinitionVisitor<>() {
                @Override
                public EndEvent handle(MessageEventDefinition evDefinition) {
                    return new EndEvent(flowNode.getName(), EndEventType.MESSAGE);
                }

                @Override
                public EndEvent handle(LinkEventDefinition evDefinition) {
                    throw new RuntimeException("End event definitions should not be of type link!");
                }

                @Override
                public EndEvent handle(SignalEventDefinition evDefinition) {
                    return new EndEvent(
                            flowNode.getName(),
                            EndEventType.SIGNAL,
                            mapSignalEventDefinition(evDefinition, flowNode));
                }

                @Override
                public EndEvent handle(TerminateEventDefinition evDefinition) {
                    return new EndEvent(flowNode.getName(), EndEventType.TERMINATION);
                }

                @Override
                public EndEvent handle(TimerEventDefinition evDefinition) {
                    throw new RuntimeException("End event definitions should not be of type timer!");
                }
            };
            return this.visitDefinition(eventDefinition, visitor);
        }
        throw new RuntimeException("Start event has more than one event definition!");
    }

    private StartEvent mapStartEvent(FlowNode flowNode) {
        org.camunda.bpm.model.bpmn.instance.StartEvent startEvent = (org.camunda.bpm.model.bpmn.instance.StartEvent) flowNode;
        Collection<EventDefinition> eventDefinitions = startEvent.getEventDefinitions();
        if (eventDefinitions.isEmpty()) {
            return new StartEvent(flowNode.getName());
        }
        if (eventDefinitions.size() == 1) {
            EventDefinition eventDefinition = eventDefinitions.iterator().next();
            EventDefinitionVisitor<StartEvent> visitor = new EventDefinitionVisitor<>() {
                @Override
                public StartEvent handle(MessageEventDefinition evDefinition) {
                    return new StartEvent(flowNode.getName(), StartEventType.MESSAGE);
                }

                @Override
                public StartEvent handle(LinkEventDefinition evDefinition) {
                    throw new RuntimeException("Start event definitions should not be of type link!");
                }

                @Override
                public StartEvent handle(SignalEventDefinition evDefinition) {
                    return new StartEvent(
                            flowNode.getName(),
                            StartEventType.SIGNAL,
                            mapSignalEventDefinition(evDefinition, flowNode));
                }

                @Override
                public StartEvent handle(TerminateEventDefinition evDefinition) {
                    throw new RuntimeException("Start event definitions should not be of type terminate!");
                }

                @Override
                public StartEvent handle(TimerEventDefinition evDefinition) {
                    throw new RuntimeException("Timer start events currently not supported!");
                }
            };
            return this.visitDefinition(eventDefinition, visitor);
        }
        throw new RuntimeException("Start event has more than one event definition!");
    }

    private behavior.bpmn.events.EventDefinition mapSignalEventDefinition(
            SignalEventDefinition evDefinition,
            FlowNode flowNode) {
        if (evDefinition.getSignal() != null
                && evDefinition.getSignal().getName() != null
                && !evDefinition.getSignal().getName().isBlank()) {
            return new behavior.bpmn.events.EventDefinition(evDefinition.getSignal().getName());
        }
        return new behavior.bpmn.events.EventDefinition(flowNode.getName());
    }

    private behavior.bpmn.FlowNode mapIntermediateCatchEvent(FlowNode flowNode) {
        org.camunda.bpm.model.bpmn.instance.IntermediateCatchEvent intermediateCatchEvent = (org.camunda.bpm.model.bpmn.instance.IntermediateCatchEvent) flowNode;
        Collection<EventDefinition> eventDefinitions = intermediateCatchEvent.getEventDefinitions();
        if (eventDefinitions.isEmpty()) {
            throw new RuntimeException("Intermediate catch events need an event definition!");
        }
        if (eventDefinitions.size() == 1) {
            EventDefinition evDefinition = eventDefinitions.iterator().next();
            EventDefinitionVisitor<IntermediateCatchEvent> eventVisitor = new EventDefinitionVisitor<>() {
                @Override
                public IntermediateCatchEvent handle(MessageEventDefinition evDefinition) {
                    return new IntermediateCatchEvent(flowNode.getName(), IntermediateCatchEventType.MESSAGE);
                }

                @Override
                public IntermediateCatchEvent handle(LinkEventDefinition evDefinition) {
                    return new IntermediateCatchEvent(flowNode.getName(), IntermediateCatchEventType.LINK);
                }

                @Override
                public IntermediateCatchEvent handle(SignalEventDefinition evDefinition) {
                    return new IntermediateCatchEvent(
                            flowNode.getName(),
                            IntermediateCatchEventType.SIGNAL,
                            mapSignalEventDefinition(evDefinition, flowNode));
                }

                @Override
                public IntermediateCatchEvent handle(TerminateEventDefinition evDefinition) {
                    throw new RuntimeException("Intermediate catch event definitions should not be of type terminate!");
                }

                @Override
                public IntermediateCatchEvent handle(TimerEventDefinition evDefinition) {
                    return new IntermediateCatchEvent(flowNode.getName(), IntermediateCatchEventType.TIMER);
                }
            };
            return this.visitDefinition(evDefinition, eventVisitor);
        }
        throw new RuntimeException("Intermediate catch event has more than one event definition!");
    }

    private behavior.bpmn.FlowNode mapIntermediateThrowEvent(FlowNode flowNode) {
        org.camunda.bpm.model.bpmn.instance.IntermediateThrowEvent intermediateThrowEvent = (org.camunda.bpm.model.bpmn.instance.IntermediateThrowEvent) flowNode;
        Collection<EventDefinition> eventDefinitions = intermediateThrowEvent.getEventDefinitions();
        if (eventDefinitions.isEmpty()) {
            return new IntermediateThrowEvent(flowNode.getName(), IntermediateThrowEventType.NONE);
        }
        if (eventDefinitions.size() == 1) {
            EventDefinition evDefinition = eventDefinitions.iterator().next();
            EventDefinitionVisitor<IntermediateThrowEvent> eventVisitor = new EventDefinitionVisitor<>() {
                @Override
                public IntermediateThrowEvent handle(MessageEventDefinition evDefinition) {
                    return new IntermediateThrowEvent(flowNode.getName(), IntermediateThrowEventType.MESSAGE);
                }

                @Override
                public IntermediateThrowEvent handle(LinkEventDefinition evDefinition) {
                    return new IntermediateThrowEvent(flowNode.getName(), IntermediateThrowEventType.LINK);
                }

                @Override
                public IntermediateThrowEvent handle(SignalEventDefinition evDefinition) {
                    return new IntermediateThrowEvent(
                            flowNode.getName(),
                            IntermediateThrowEventType.SIGNAL,
                            mapSignalEventDefinition(evDefinition, flowNode));
                }

                @Override
                public IntermediateThrowEvent handle(TerminateEventDefinition evDefinition) {
                    throw new RuntimeException("Intermediate throw event definitions should not be of type terminate!");
                }

                @Override
                public IntermediateThrowEvent handle(TimerEventDefinition evDefinition) {
                    throw new RuntimeException("Intermediate throw event definitions should not be of type timer!");
                }
            };
            return this.visitDefinition(evDefinition, eventVisitor);
        }
        throw new RuntimeException("Intermediate throw event has more than one event definition!");
    }

    private <T> T visitDefinition(org.camunda.bpm.model.bpmn.instance.EventDefinition evDefinition, EventDefinitionVisitor<T> eventVisitor) {
        if (evDefinition instanceof MessageEventDefinition) {
            return eventVisitor.handle((MessageEventDefinition) evDefinition);
        }
        if (evDefinition instanceof LinkEventDefinition) {
            return eventVisitor.handle((LinkEventDefinition) evDefinition);
        }
        if (evDefinition instanceof SignalEventDefinition) {
            return eventVisitor.handle((SignalEventDefinition) evDefinition);
        }
        if (evDefinition instanceof TerminateEventDefinition) {
            return eventVisitor.handle((TerminateEventDefinition) evDefinition);
        }
        if (evDefinition instanceof TimerEventDefinition) {
            return eventVisitor.handle((TimerEventDefinition) evDefinition);
        }
        throw new RuntimeException("Unknown event definition found!" + evDefinition);
    }
}
