package behavior.bpmn.reader;

import behavior.bpmn.BPMNCollaboration;
import behavior.bpmn.activities.tasks.ReceiveTask;
import behavior.bpmn.activities.tasks.SendTask;
import behavior.bpmn.activities.tasks.Task;
import behavior.bpmn.auxiliary.BPMNCollaborationBuilder;
import behavior.bpmn.events.EndEvent;
import behavior.bpmn.events.IntermediateCatchEvent;
import behavior.bpmn.events.IntermediateThrowEvent;
import behavior.bpmn.events.StartEvent;
import behavior.bpmn.events.*;
import behavior.bpmn.gateways.EventBasedGateway;
import behavior.bpmn.gateways.ExclusiveGateway;
import behavior.bpmn.gateways.ParallelGateway;
import org.apache.commons.io.FilenameUtils;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.EventDefinition;
import org.camunda.bpm.model.bpmn.instance.*;
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

        // Map participants/pools
        Map<String, behavior.bpmn.FlowNode> mappedFlowNodes = new HashMap<>();
        ModelElementType participantType = bpmnModelInstance.getModel().getType(Participant.class);
        Collection<ModelElementInstance> participants = bpmnModelInstance.getModelElementsByType(participantType);
        if (participants.isEmpty()) {
            bpmnCollaborationBuilder.processName(fileName);
            mapModelInstanceToOneParticipant(bpmnModelInstance, bpmnCollaborationBuilder, mappedFlowNodes);
        } else {
            // Map each participant
            participants.forEach(modelElementInstance ->
                    mapParticipant(bpmnCollaborationBuilder, mappedFlowNodes, (Participant) modelElementInstance));
            // Map message flows
            mapMessageFlows(bpmnModelInstance, bpmnCollaborationBuilder, mappedFlowNodes);
        }
        return bpmnCollaborationBuilder.build();
    }

    private void mapMessageFlows(
            BpmnModelInstance bpmnModelInstance,
            BPMNCollaborationBuilder bpmnCollaborationBuilder,
            Map<String, behavior.bpmn.FlowNode> mappedFlowNodes) {
        ModelElementType mfType = bpmnModelInstance.getModel().getType(MessageFlow.class);
        Collection<ModelElementInstance> messageFlowModelElements = bpmnModelInstance.getModelElementsByType(mfType);
        messageFlowModelElements.forEach(modelElementInstance ->
                mapMessageFlow((MessageFlow) modelElementInstance, bpmnCollaborationBuilder, mappedFlowNodes));
    }

    private void mapMessageFlow(
            MessageFlow messageFlow,
            BPMNCollaborationBuilder bpmnCollaborationBuilder,
            Map<String, behavior.bpmn.FlowNode> mappedFlowNodes) {
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
                mapFlowNode(source, mappedFlowNodes, bpmnCollaborationBuilder),
                mapFlowNode(target, mappedFlowNodes, bpmnCollaborationBuilder));
    }

    private void mapModelInstanceToOneParticipant(
            BpmnModelInstance bpmnModelInstance,
            BPMNCollaborationBuilder bpmnCollaborationBuilder,
            Map<String, behavior.bpmn.FlowNode> mappedFlowNodes) {
        ModelElementType fnType = bpmnModelInstance.getModel().getType(FlowNode.class);
        Collection<ModelElementInstance> flowNodeModelElements = bpmnModelInstance.getModelElementsByType(fnType);
        flowNodeModelElements.forEach(flowNodeModelElement -> {
            FlowNode flowNode = (FlowNode) flowNodeModelElement;
            mapFlowNode(flowNode, mappedFlowNodes, bpmnCollaborationBuilder);
        });
        ModelElementType sfType = bpmnModelInstance.getModel().getType(SequenceFlow.class);
        Collection<ModelElementInstance> sequenceFlowModelElements = bpmnModelInstance.getModelElementsByType(sfType);
        sequenceFlowModelElements.forEach(sequenceFlowModelElement -> {
            SequenceFlow sequenceFlow = (SequenceFlow) sequenceFlowModelElement;
            mapSequenceFlow(sequenceFlow, mappedFlowNodes, bpmnCollaborationBuilder);
        });
    }

    private void mapParticipant(
            BPMNCollaborationBuilder bpmnCollaborationBuilder,
            Map<String, behavior.bpmn.FlowNode> createdFlowNodes,
            Participant participant) {
        bpmnCollaborationBuilder.processName(participant.getName());
        participant.getProcess().getFlowElements().forEach(flowElement -> {
            // Map flow nodes
            if (flowElement instanceof FlowNode) {
                mapFlowNode((FlowNode) flowElement, createdFlowNodes, bpmnCollaborationBuilder);
            }
            // Map sequence flows
            if (flowElement instanceof SequenceFlow) {
                SequenceFlow sequenceFlow = (SequenceFlow) flowElement;
                mapSequenceFlow(sequenceFlow, createdFlowNodes, bpmnCollaborationBuilder);
            }
        });
        bpmnCollaborationBuilder.buildProcess();
    }

    private void mapSequenceFlow(
            SequenceFlow sequenceFlow,
            Map<String, behavior.bpmn.FlowNode> mappedFlowNodes,
            BPMNCollaborationBuilder bpmnCollaborationBuilder) {
        if (sequenceFlow.getName() == null || sequenceFlow.getName().isEmpty()) {
            bpmnCollaborationBuilder.sequenceFlow(mapFlowNode(sequenceFlow.getSource(), mappedFlowNodes, bpmnCollaborationBuilder), mapFlowNode(sequenceFlow.getTarget(), mappedFlowNodes, bpmnCollaborationBuilder));
        } else {
            bpmnCollaborationBuilder.sequenceFlow(sequenceFlow.getName(), mapFlowNode(sequenceFlow.getSource(), mappedFlowNodes, bpmnCollaborationBuilder), mapFlowNode(sequenceFlow.getTarget(), mappedFlowNodes, bpmnCollaborationBuilder));
        }
    }

    private behavior.bpmn.FlowNode mapFlowNode(
            FlowNode flowNode,
            Map<String, behavior.bpmn.FlowNode> mappedFlowNodes,
            BPMNCollaborationBuilder bpmnCollaborationBuilder) {
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
                // TODO: Multiple start events?
                bpmnCollaborationBuilder.startEvent(startEvent);
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
                resultingFlowNode = new ReceiveTask(flowNode.getName());
                break;
            case "subProcess":
                // TODO: Should be embedded. How do we handle this?
                resultingFlowNode = new Task(flowNode.getName());
                break;
            case "callActivity":
                // Call Activity = Reusable sub-processes (external).
                // TODO: how to get the subprocess models? calledElement attribute --> Read a set of files simultaneously?
                resultingFlowNode = new Task(flowNode.getName());
                break;
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
            default:
                throw new RuntimeException(String.format("Unknown task type \"%s\" found!", taskTypeName));
        }
        mappedFlowNodes.put(flowNode.getId(), resultingFlowNode);
        return resultingFlowNode;
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
        throw new RuntimeException("Unknown event definition found!" + evDefinition);
    }
}
