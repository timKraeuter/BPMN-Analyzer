package no.tk.behavior.bpmn.reader;

import no.tk.behavior.bpmn.BPMNCollaboration;
import no.tk.behavior.bpmn.activities.CallActivity;
import no.tk.behavior.bpmn.activities.tasks.ReceiveTask;
import no.tk.behavior.bpmn.auxiliary.BPMNCollaborationBuilder;
import no.tk.behavior.bpmn.auxiliary.BPMNEventSubprocessBuilder;
import no.tk.behavior.bpmn.auxiliary.BPMNModelBuilder;
import no.tk.behavior.bpmn.auxiliary.BPMNProcessBuilder;
import no.tk.behavior.bpmn.auxiliary.exceptions.BPMNRuntimeException;
import no.tk.behavior.bpmn.events.*;
import no.tk.behavior.bpmn.events.EndEvent;
import no.tk.behavior.bpmn.events.IntermediateCatchEvent;
import no.tk.behavior.bpmn.events.IntermediateThrowEvent;
import no.tk.behavior.bpmn.events.StartEvent;
import no.tk.behavior.bpmn.gateways.InclusiveGateway;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import no.tk.behavior.bpmn.reader.token.extension.TokenBPMN;
import org.apache.commons.io.FilenameUtils;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.*;
import org.camunda.bpm.model.bpmn.instance.BoundaryEvent;
import org.camunda.bpm.model.bpmn.instance.EventDefinition;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaProperties;
import org.camunda.bpm.model.xml.instance.ModelElementInstance;
import org.camunda.bpm.model.xml.type.ModelElementType;

public class BPMNFileReader {
  static {
    Bpmn.INSTANCE = new TokenBPMN();
  }

  private Function<String, String> elementNameTransformer;

  public BPMNFileReader() {}

  public BPMNFileReader(UnaryOperator<String> elementNameTransformer) {
    this.elementNameTransformer = elementNameTransformer;
  }

  public BPMNCollaboration readModelFromFilePath(Path file) throws IOException {
    String modelName = FilenameUtils.removeExtension(file.getFileName().toString());
    return readModelFromStream(modelName, Files.newInputStream(file));
  }

  public BPMNCollaboration readModelFromStream(InputStream stream) {
    return readModelFromStream("model", stream);
  }

  public BPMNCollaboration readModelFromStream(String modelName, InputStream stream) {
    BpmnModelInstance bpmnModelInstance = Bpmn.readModelFromStream(stream);
    return convertModel(modelName, bpmnModelInstance);
  }

  private BPMNCollaboration convertModel(
      String collaborationName, BpmnModelInstance bpmnModelInstance) {
    BPMNCollaborationBuilder bpmnCollaborationBuilder =
        new BPMNCollaborationBuilder().name(collaborationName);

    Map<String, no.tk.behavior.bpmn.FlowNode> mappedFlowNodes = new HashMap<>();
    Map<String, Boolean> mappedSequenceFlows = new HashMap<>();

    // Map subprocesses
    ModelElementType subprocessType = bpmnModelInstance.getModel().getType(SubProcess.class);
    Collection<ModelElementInstance> subprocesses =
        bpmnModelInstance.getModelElementsByType(subprocessType);
    subprocesses.forEach(
        subProcessModelElement -> {
          SubProcess subProcess = (SubProcess) subProcessModelElement;
          no.tk.behavior.bpmn.FlowNode mappedSubprocessIfExists = mappedFlowNodes.get(subProcess.getId());
          if (mappedSubprocessIfExists == null && !subProcess.triggeredByEvent()) {
            mapSubProcess(subProcess, mappedFlowNodes, mappedSequenceFlows);
          }
        });

    // Map participants/pools
    ModelElementType participantType = bpmnModelInstance.getModel().getType(Participant.class);
    Collection<ModelElementInstance> participants =
        bpmnModelInstance.getModelElementsByType(participantType);
    if (participants.isEmpty()) {
      bpmnCollaborationBuilder.processName(collaborationName);
      mapModelInstanceToOneParticipant(
          bpmnModelInstance, bpmnCollaborationBuilder, mappedFlowNodes, mappedSequenceFlows);
    } else {
      // Map each participant
      participants.forEach(
          modelElementInstance ->
              mapParticipant(
                  bpmnCollaborationBuilder,
                  mappedFlowNodes,
                  mappedSequenceFlows,
                  (Participant) modelElementInstance));
      // Map message flows
      mapMessageFlows(
          bpmnModelInstance, bpmnCollaborationBuilder, mappedFlowNodes, mappedSequenceFlows);
    }
    return bpmnCollaborationBuilder.build();
  }

  private void mapMessageFlows(
      BpmnModelInstance bpmnModelInstance,
      BPMNCollaborationBuilder bpmnCollaborationBuilder,
      Map<String, no.tk.behavior.bpmn.FlowNode> mappedFlowNodes,
      Map<String, Boolean> mappedSequenceFlows) {
    ModelElementType mfType = bpmnModelInstance.getModel().getType(MessageFlow.class);
    Collection<ModelElementInstance> messageFlowModelElements =
        bpmnModelInstance.getModelElementsByType(mfType);
    messageFlowModelElements.forEach(
        modelElementInstance ->
            mapMessageFlow(
                (MessageFlow) modelElementInstance,
                bpmnCollaborationBuilder,
                mappedFlowNodes,
                mappedSequenceFlows));
  }

  private void mapMessageFlow(
      MessageFlow messageFlow,
      BPMNCollaborationBuilder bpmnCollaborationBuilder,
      Map<String, no.tk.behavior.bpmn.FlowNode> mappedFlowNodes,
      Map<String, Boolean> mappedSequenceFlows) {
    InteractionNode sourceInteractionNode = messageFlow.getSource();
    InteractionNode targetInteractionNode = messageFlow.getTarget();
    if (!(sourceInteractionNode instanceof FlowNode source)) {
      throw new BPMNRuntimeException(
          String.format(
              "Message flow with id \"%s\" has an invalid source with id "
                  + "\"%s\", which is not a flow node (event, activity, ...)!",
              messageFlow.getId(), sourceInteractionNode.getId()));
    }
    if (!(targetInteractionNode instanceof FlowNode target)) {
      throw new BPMNRuntimeException(
          String.format(
              "Message flow with id \"%s\" has an invalid target with id "
                  + "\"%s\", which is not a flow node (event, activity, ...)!",
              messageFlow.getId(), targetInteractionNode.getId()));
    }
    bpmnCollaborationBuilder.messageFlow(
        messageFlow.getId(),
        messageFlow.getName() == null ? "" : messageFlow.getName(),
        mapFlowNode(source, mappedFlowNodes, mappedSequenceFlows, bpmnCollaborationBuilder),
        mapFlowNode(target, mappedFlowNodes, mappedSequenceFlows, bpmnCollaborationBuilder));
  }

  private void mapModelInstanceToOneParticipant(
      BpmnModelInstance bpmnModelInstance,
      BPMNCollaborationBuilder bpmnCollaborationBuilder,
      Map<String, no.tk.behavior.bpmn.FlowNode> mappedFlowNodes,
      Map<String, Boolean> mappedSequenceFlows) {
    // Map subprocesses since they might not be connected by sequence flows (event subprocesses for
    // example).
    ModelElementType spType = bpmnModelInstance.getModel().getType(SubProcess.class);
    Collection<ModelElementInstance> subProcessModelElements =
        bpmnModelInstance.getModelElementsByType(spType);
    subProcessModelElements.forEach(
        flowNodeModelElement -> {
          FlowNode flowNode = (FlowNode) flowNodeModelElement;
          mapFlowNode(flowNode, mappedFlowNodes, mappedSequenceFlows, bpmnCollaborationBuilder);
        });
    ModelElementType sfType = bpmnModelInstance.getModel().getType(FlowElement.class);
    Collection<ModelElementInstance> sequenceFlowModelElements =
        bpmnModelInstance.getModelElementsByType(sfType);
    sequenceFlowModelElements.forEach(
        sequenceFlowModelElement -> {
          FlowElement flowElement = (FlowElement) sequenceFlowModelElement;
          mapFlowElement(
              bpmnCollaborationBuilder, mappedFlowNodes, mappedSequenceFlows, flowElement);
        });
  }

  private void mapParticipant(
      BPMNCollaborationBuilder bpmnCollaborationBuilder,
      Map<String, no.tk.behavior.bpmn.FlowNode> createdFlowNodes,
      Map<String, Boolean> mappedSequenceFlows,
      Participant participant) {
    bpmnCollaborationBuilder.processName(participant.getName());
    participant
        .getProcess()
        .getFlowElements()
        .forEach(
            flowElement ->
                mapFlowElement(
                    bpmnCollaborationBuilder, createdFlowNodes, mappedSequenceFlows, flowElement));
    bpmnCollaborationBuilder.buildProcess();
  }

  private void mapFlowElement(
      BPMNModelBuilder bpmnModelBuilder,
      Map<String, no.tk.behavior.bpmn.FlowNode> mappedFlowNodes,
      Map<String, Boolean> mappedSequenceFlows,
      FlowElement flowElement) {
    // Map flow nodes
    if (flowElement instanceof FlowNode flowNode) {
      mapFlowNode(flowNode, mappedFlowNodes, mappedSequenceFlows, bpmnModelBuilder);
    }
    // Map sequence flows
    if (flowElement instanceof SequenceFlow sequenceFlow) {
      mapSequenceFlow(sequenceFlow, mappedFlowNodes, mappedSequenceFlows, bpmnModelBuilder);
    }
  }

  private void mapSequenceFlow(
      SequenceFlow sequenceFlow,
      Map<String, no.tk.behavior.bpmn.FlowNode> mappedFlowNodes,
      Map<String, Boolean> mappedSequenceFlows,
      BPMNModelBuilder bpmnModelBuilder) {
    Boolean sequenceFlowMapped = mappedSequenceFlows.get(sequenceFlow.getId());
    if (sequenceFlowMapped != null && sequenceFlowMapped) {
      // Sequence flow has been mapped already.
      return;
    }
    if (sequenceFlow.getName() == null || sequenceFlow.getName().isEmpty()) {
      bpmnModelBuilder.sequenceFlow(
          sequenceFlow.getId(),
          mapFlowNode(
              sequenceFlow.getSource(), mappedFlowNodes, mappedSequenceFlows, bpmnModelBuilder),
          mapFlowNode(
              sequenceFlow.getTarget(), mappedFlowNodes, mappedSequenceFlows, bpmnModelBuilder));
    } else {
      bpmnModelBuilder.sequenceFlow(
          sequenceFlow.getId(),
          getFlowElementName(sequenceFlow),
          mapFlowNode(
              sequenceFlow.getSource(), mappedFlowNodes, mappedSequenceFlows, bpmnModelBuilder),
          mapFlowNode(
              sequenceFlow.getTarget(), mappedFlowNodes, mappedSequenceFlows, bpmnModelBuilder));
    }
    mappedSequenceFlows.put(sequenceFlow.getId(), true);
  }

  private no.tk.behavior.bpmn.FlowNode mapFlowNode(
      FlowNode flowNode,
      Map<String, no.tk.behavior.bpmn.FlowNode> mappedFlowNodes,
      Map<String, Boolean> mappedSequenceFlows,
      BPMNModelBuilder bpmnModelBuilder) {
    no.tk.behavior.bpmn.FlowNode flowNodeIfExists = mappedFlowNodes.get(flowNode.getId());
    if (flowNodeIfExists != null) {
      return flowNodeIfExists;
    }
    String taskTypeName = flowNode.getElementType().getTypeName();
    no.tk.behavior.bpmn.FlowNode resultingFlowNode;
    switch (taskTypeName) {
        // Events
      case "startEvent" -> {
        StartEvent startEvent = mapStartEvent(flowNode);
        bpmnModelBuilder.startEvent(startEvent);
        resultingFlowNode = startEvent;
      }
      case "intermediateThrowEvent" -> resultingFlowNode = this.mapIntermediateThrowEvent(flowNode);
      case "intermediateCatchEvent" -> resultingFlowNode = this.mapIntermediateCatchEvent(flowNode);
      case "endEvent" -> resultingFlowNode = mapEndEvent(flowNode);

        // Tasks
      case "businessRuleTask",
          "scriptTask",
          "serviceTask",
          "manualTask",
          "userTask",
          "task" -> resultingFlowNode = new no.tk.behavior.bpmn.activities.tasks.Task(flowNode.getId(), getFlowElementName(flowNode));
      case "sendTask" -> resultingFlowNode =
          new no.tk.behavior.bpmn.activities.tasks.SendTask(flowNode.getId(), getFlowElementName(flowNode));
      case "receiveTask" -> {
        boolean instantiate = hasInstantiateCamundaProperty(flowNode);
        resultingFlowNode =
            new ReceiveTask(flowNode.getId(), getFlowElementName(flowNode), instantiate);
      }
      case "subProcess" -> {
        SubProcess subprocess = (SubProcess) flowNode;
        if (subprocess.triggeredByEvent()) {
          handleEventSubProcess(subprocess, mappedFlowNodes, mappedSequenceFlows, bpmnModelBuilder);
          resultingFlowNode = null;
          // This is ok since no sequence flows can start or end in an event subprocess.
        } else {
          resultingFlowNode = mapSubProcess(subprocess, mappedFlowNodes, mappedSequenceFlows);
        }
      }
      case "callActivity" ->
      // Call Activity = Reusable sub-processes (external).
      throw new BPMNRuntimeException("External subprocesses currently not supported!");

        // Gateways
      case "parallelGateway" -> resultingFlowNode =
          new no.tk.behavior.bpmn.gateways.ParallelGateway(flowNode.getId(), getFlowElementName(flowNode));
      case "exclusiveGateway" -> resultingFlowNode =
          new no.tk.behavior.bpmn.gateways.ExclusiveGateway(flowNode.getId(), getFlowElementName(flowNode));
      case "eventBasedGateway" -> {
        boolean instantiateGateway = hasInstantiateCamundaProperty(flowNode);
        resultingFlowNode =
            new no.tk.behavior.bpmn.gateways.EventBasedGateway(
                flowNode.getId(), getFlowElementName(flowNode), instantiateGateway);
      }
      case "inclusiveGateway" -> resultingFlowNode =
          new InclusiveGateway(flowNode.getId(), getFlowElementName(flowNode));
      case "boundaryEvent" -> resultingFlowNode =
          handleBoundaryEvent(flowNode, mappedFlowNodes, mappedSequenceFlows, bpmnModelBuilder);
      default -> throw new BPMNRuntimeException(
          String.format("Unknown task type \"%s\" found!", taskTypeName));
    }
    if (resultingFlowNode != null) {
      bpmnModelBuilder.flowNode(resultingFlowNode);
      mappedFlowNodes.put(flowNode.getId(), resultingFlowNode);
    }
    return resultingFlowNode;
  }

  private String getFlowElementName(FlowElement flowElement) {
    if (flowElement.getName() == null) {
      return flowElement.getId();
    }
    if (this.elementNameTransformer != null) {
      return elementNameTransformer.apply(flowElement.getName());
    }
    return flowElement.getName();
  }

  private no.tk.behavior.bpmn.FlowNode handleBoundaryEvent(
      FlowNode flowNode,
      Map<String, no.tk.behavior.bpmn.FlowNode> mappedFlowNodes,
      Map<String, Boolean> mappedSequenceFlows,
      BPMNModelBuilder bpmnModelBuilder) {
    BoundaryEvent event = (BoundaryEvent) flowNode;
    Collection<EventDefinition> eventDefinitions = event.getEventDefinitions();
    no.tk.behavior.bpmn.events.BoundaryEvent boundaryEvent;
    if (eventDefinitions.size() != 1) {
      boundaryEvent =
          new no.tk.behavior.bpmn.events.BoundaryEvent(
              flowNode.getId(),
              getFlowElementName(event),
              BoundaryEventType.NONE,
              event.cancelActivity());
    } else {
      boundaryEvent = getBoundaryEventWithDefinition(event, eventDefinitions.iterator().next());
    }
    // AttachedTo must be an activity
    no.tk.behavior.bpmn.activities.Activity attachedTo =
        (no.tk.behavior.bpmn.activities.Activity)
            mapFlowNode(
                event.getAttachedTo(), mappedFlowNodes, mappedSequenceFlows, bpmnModelBuilder);
    attachedTo.attachBoundaryEvent(boundaryEvent);
    return boundaryEvent;
  }

  private no.tk.behavior.bpmn.events.BoundaryEvent getBoundaryEventWithDefinition(
      BoundaryEvent event, EventDefinition eventDefinition) {
    no.tk.behavior.bpmn.events.BoundaryEvent boundaryEvent;
    // Create the boundary event.
    EventDefinitionVisitor<no.tk.behavior.bpmn.events.BoundaryEvent> visitor =
        new EventDefinitionVisitor<>() {
          @Override
          public no.tk.behavior.bpmn.events.BoundaryEvent handle(MessageEventDefinition evDefinition) {
            return new no.tk.behavior.bpmn.events.BoundaryEvent(
                event.getId(),
                getFlowElementName(event),
                BoundaryEventType.MESSAGE,
                event.cancelActivity());
          }

          @Override
          public no.tk.behavior.bpmn.events.BoundaryEvent handle(LinkEventDefinition evDefinition) {
            throw new BPMNRuntimeException("A boundary event cannot be a linked event!");
          }

          @Override
          public no.tk.behavior.bpmn.events.BoundaryEvent handle(SignalEventDefinition evDefinition) {
            return new no.tk.behavior.bpmn.events.BoundaryEvent(
                event.getId(),
                getFlowElementName(event),
                BoundaryEventType.SIGNAL,
                event.cancelActivity(),
                mapSignalEventDefinition(evDefinition, event));
          }

          @Override
          public no.tk.behavior.bpmn.events.BoundaryEvent handle(TerminateEventDefinition evDefinition) {
            throw new BPMNRuntimeException("A boundary event cannot be a terminate event!");
          }

          @Override
          public no.tk.behavior.bpmn.events.BoundaryEvent handle(TimerEventDefinition evDefinition) {
            return new no.tk.behavior.bpmn.events.BoundaryEvent(
                event.getId(),
                getFlowElementName(event),
                BoundaryEventType.TIMER,
                event.cancelActivity());
          }

          @Override
          public no.tk.behavior.bpmn.events.BoundaryEvent handle(ErrorEventDefinition evDefinition) {
            return new no.tk.behavior.bpmn.events.BoundaryEvent(
                event.getId(),
                getFlowElementName(event),
                BoundaryEventType.ERROR,
                event.cancelActivity(),
                mapErrorEventDefinition(evDefinition));
          }

          @Override
          public no.tk.behavior.bpmn.events.BoundaryEvent handle(EscalationEventDefinition evDefinition) {
            return new no.tk.behavior.bpmn.events.BoundaryEvent(
                event.getId(),
                getFlowElementName(event),
                BoundaryEventType.ESCALATION,
                event.cancelActivity(),
                mapEscalationEventDefinition(evDefinition));
          }
        };
    boundaryEvent = this.visitDefinition(eventDefinition, visitor);
    return boundaryEvent;
  }

  private no.tk.behavior.bpmn.events.definitions.EventDefinition mapEscalationEventDefinition(
      EscalationEventDefinition evDefinition) {
    if (evDefinition.getEscalation() != null
        && evDefinition.getEscalation().getEscalationCode() != null
        && !evDefinition.getEscalation().getEscalationCode().isBlank()) {
      return new no.tk.behavior.bpmn.events.definitions.EscalationEventDefinition(
          evDefinition.getEscalation().getEscalationCode());
    }
    return no.tk.behavior.bpmn.events.definitions.EventDefinition.empty();
  }

  private no.tk.behavior.bpmn.events.definitions.EventDefinition mapErrorEventDefinition(
      ErrorEventDefinition evDefinition) {
    if (evDefinition.getError() != null
        && evDefinition.getError().getErrorCode() != null
        && !evDefinition.getError().getErrorCode().isBlank()) {
      return new no.tk.behavior.bpmn.events.definitions.ErrorEventDefinition(
          evDefinition.getError().getErrorCode());
    }
    return no.tk.behavior.bpmn.events.definitions.EventDefinition.empty();
  }

  private void handleEventSubProcess(
      SubProcess subprocess,
      Map<String, no.tk.behavior.bpmn.FlowNode> mappedFlowNodes,
      Map<String, Boolean> mappedSequenceFlows,
      BPMNModelBuilder bpmnModelBuilder) {
    BPMNEventSubprocessBuilder subprocessBuilder =
        new BPMNEventSubprocessBuilder().name(getFlowElementName(subprocess));
    subprocess
        .getFlowElements()
        .forEach(
            flowElement ->
                mapFlowElement(
                    subprocessBuilder, mappedFlowNodes, mappedSequenceFlows, flowElement));

    bpmnModelBuilder.eventSubprocess(subprocessBuilder.build());
  }

  private no.tk.behavior.bpmn.activities.CallActivity mapSubProcess(
      SubProcess subprocess,
      Map<String, no.tk.behavior.bpmn.FlowNode> mappedFlowNodes,
      Map<String, Boolean> mappedSequenceFlows) {
    BPMNProcessBuilder subprocessBuilder =
        new BPMNProcessBuilder().name(getFlowElementName(subprocess));
    subprocess
        .getFlowElements()
        .forEach(
            flowElement ->
                mapFlowElement(
                    subprocessBuilder, mappedFlowNodes, mappedSequenceFlows, flowElement));

    no.tk.behavior.bpmn.activities.CallActivity subProcessCallActivity =
        new CallActivity(subprocess.getId(), subprocessBuilder.build());
    mappedFlowNodes.put(subprocess.getId(), subProcessCallActivity);
    return subProcessCallActivity;
  }

  private boolean hasInstantiateCamundaProperty(FlowNode flowNode) {
    ExtensionElements extensionElements = flowNode.getExtensionElements();
    if (extensionElements == null) {
      return false;
    }
    // Instantiate is set as a camunda property using the camunda modeler.
    List<CamundaProperties> propertiesList =
        extensionElements.getElementsQuery().filterByType(CamundaProperties.class).list();

    return propertiesList.stream()
        .anyMatch(
            camundaProperties ->
                camundaProperties.getCamundaProperties().stream()
                    .anyMatch(
                        camundaProperty ->
                            camundaProperty.getCamundaName().equals("instantiate")
                                && camundaProperty.getCamundaValue().equals("true")));
  }

  private EndEvent mapEndEvent(FlowNode flowNode) {
    org.camunda.bpm.model.bpmn.instance.EndEvent endEvent =
        (org.camunda.bpm.model.bpmn.instance.EndEvent) flowNode;
    Collection<EventDefinition> eventDefinitions = endEvent.getEventDefinitions();
    if (eventDefinitions.isEmpty()) {
      return new EndEvent(flowNode.getId(), getFlowElementName(flowNode));
    }
    if (eventDefinitions.size() == 1) {
      EventDefinition eventDefinition = eventDefinitions.iterator().next();
      EventDefinitionVisitor<EndEvent> visitor =
          new EventDefinitionVisitor<>() {
            @Override
            public EndEvent handle(MessageEventDefinition evDefinition) {
              return new EndEvent(
                  flowNode.getId(), getFlowElementName(flowNode), EndEventType.MESSAGE);
            }

            @Override
            public EndEvent handle(LinkEventDefinition evDefinition) {
              throw new BPMNRuntimeException("End event definitions should not be of type link!");
            }

            @Override
            public EndEvent handle(SignalEventDefinition evDefinition) {
              return new EndEvent(
                  flowNode.getId(),
                  getFlowElementName(flowNode),
                  EndEventType.SIGNAL,
                  mapSignalEventDefinition(evDefinition, flowNode));
            }

            @Override
            public EndEvent handle(TerminateEventDefinition evDefinition) {
              return new EndEvent(
                  flowNode.getId(), getFlowElementName(flowNode), EndEventType.TERMINATION);
            }

            @Override
            public EndEvent handle(ErrorEventDefinition evDefinition) {
              return new EndEvent(
                  flowNode.getId(),
                  getFlowElementName(flowNode),
                  EndEventType.ERROR,
                  mapErrorEventDefinition(evDefinition));
            }

            @Override
            public EndEvent handle(EscalationEventDefinition evDefinition) {
              return new EndEvent(
                  flowNode.getId(),
                  getFlowElementName(flowNode),
                  EndEventType.ESCALATION,
                  mapEscalationEventDefinition(evDefinition));
            }

            @Override
            public EndEvent handle(TimerEventDefinition evDefinition) {
              throw new BPMNRuntimeException("End event definitions should not be of type timer!");
            }
          };
      return this.visitDefinition(eventDefinition, visitor);
    }
    throw new BPMNRuntimeException("Start event has more than one event definition!");
  }

  private StartEvent mapStartEvent(FlowNode flowNode) {
    org.camunda.bpm.model.bpmn.instance.StartEvent startEvent =
        (org.camunda.bpm.model.bpmn.instance.StartEvent) flowNode;
    Collection<EventDefinition> eventDefinitions = startEvent.getEventDefinitions();
    if (eventDefinitions.isEmpty()) {
      return new StartEvent(flowNode.getId(), getFlowElementName(flowNode));
    }
    if (eventDefinitions.size() == 1) {
      EventDefinition eventDefinition = eventDefinitions.iterator().next();
      EventDefinitionVisitor<StartEvent> visitor =
          new EventDefinitionVisitor<>() {
            @Override
            public StartEvent handle(MessageEventDefinition evDefinition) {
              return new StartEvent(
                  flowNode.getId(),
                  getFlowElementName(flowNode),
                  StartEventType.MESSAGE,
                  no.tk.behavior.bpmn.events.definitions.EventDefinition.empty(),
                  startEvent.isInterrupting());
            }

            @Override
            public StartEvent handle(LinkEventDefinition evDefinition) {
              throw new BPMNRuntimeException("Start event definitions should not be of type link!");
            }

            @Override
            public StartEvent handle(SignalEventDefinition evDefinition) {
              return new StartEvent(
                  flowNode.getId(),
                  getFlowElementName(flowNode),
                  StartEventType.SIGNAL,
                  mapSignalEventDefinition(evDefinition, flowNode),
                  startEvent.isInterrupting());
            }

            @Override
            public StartEvent handle(TimerEventDefinition evDefinition) {
              throw new BPMNRuntimeException("Timer start events currently not supported!");
            }

            @Override
            public StartEvent handle(TerminateEventDefinition evDefinition) {
              throw new BPMNRuntimeException(
                  "Start event definitions should not be of type terminate!");
            }

            @Override
            public StartEvent handle(ErrorEventDefinition evDefinition) {
              return new StartEvent(
                  flowNode.getId(),
                  getFlowElementName(flowNode),
                  StartEventType.ERROR,
                  mapErrorEventDefinition(evDefinition),
                  startEvent.isInterrupting());
            }

            @Override
            public StartEvent handle(EscalationEventDefinition evDefinition) {
              return new StartEvent(
                  flowNode.getId(),
                  getFlowElementName(flowNode),
                  StartEventType.ESCALATION,
                  mapEscalationEventDefinition(evDefinition),
                  startEvent.isInterrupting());
            }
          };
      return this.visitDefinition(eventDefinition, visitor);
    }
    throw new BPMNRuntimeException("Start event has more than one event definition!");
  }

  private no.tk.behavior.bpmn.events.definitions.SignalEventDefinition mapSignalEventDefinition(
      SignalEventDefinition evDefinition, FlowNode flowNode) {
    if (evDefinition.getSignal() != null
        && evDefinition.getSignal().getName() != null
        && !evDefinition.getSignal().getName().isBlank()) {
      return new no.tk.behavior.bpmn.events.definitions.SignalEventDefinition(
          evDefinition.getSignal().getName());
    }
    return new no.tk.behavior.bpmn.events.definitions.SignalEventDefinition(getFlowElementName(flowNode));
  }

  private no.tk.behavior.bpmn.FlowNode mapIntermediateCatchEvent(FlowNode flowNode) {
    org.camunda.bpm.model.bpmn.instance.IntermediateCatchEvent intermediateCatchEvent =
        (org.camunda.bpm.model.bpmn.instance.IntermediateCatchEvent) flowNode;
    Collection<EventDefinition> eventDefinitions = intermediateCatchEvent.getEventDefinitions();
    if (eventDefinitions.isEmpty()) {
      throw new BPMNRuntimeException("Intermediate catch events need an event definition!");
    }
    if (eventDefinitions.size() == 1) {
      EventDefinition evDefinition = eventDefinitions.iterator().next();
      EventDefinitionVisitor<IntermediateCatchEvent> eventVisitor =
          new EventDefinitionVisitor<>() {
            @Override
            public IntermediateCatchEvent handle(MessageEventDefinition evDefinition) {
              return new IntermediateCatchEvent(
                  flowNode.getId(),
                  getFlowElementName(flowNode),
                  IntermediateCatchEventType.MESSAGE);
            }

            @Override
            public IntermediateCatchEvent handle(LinkEventDefinition evDefinition) {
              return new IntermediateCatchEvent(
                  flowNode.getId(), getFlowElementName(flowNode), IntermediateCatchEventType.LINK);
            }

            @Override
            public IntermediateCatchEvent handle(SignalEventDefinition evDefinition) {
              return new IntermediateCatchEvent(
                  flowNode.getId(),
                  getFlowElementName(flowNode),
                  IntermediateCatchEventType.SIGNAL,
                  mapSignalEventDefinition(evDefinition, flowNode));
            }

            @Override
            public IntermediateCatchEvent handle(TimerEventDefinition evDefinition) {
              return new IntermediateCatchEvent(
                  flowNode.getId(), getFlowElementName(flowNode), IntermediateCatchEventType.TIMER);
            }

            @Override
            public IntermediateCatchEvent handle(TerminateEventDefinition evDefinition) {
              throw new BPMNRuntimeException(
                  "Intermediate catch event definitions should not be of type terminate!");
            }

            @Override
            public IntermediateCatchEvent handle(ErrorEventDefinition evDefinition) {
              throw new BPMNRuntimeException(
                  "Intermediate catch event definitions should not be of type error!");
            }

            @Override
            public IntermediateCatchEvent handle(EscalationEventDefinition evDefinition) {
              throw new BPMNRuntimeException(
                  "Intermediate catch event definitions should not be of type escalation!");
            }
          };
      return this.visitDefinition(evDefinition, eventVisitor);
    }
    throw new BPMNRuntimeException("Intermediate catch event has more than one event definition!");
  }

  private no.tk.behavior.bpmn.FlowNode mapIntermediateThrowEvent(FlowNode flowNode) {
    org.camunda.bpm.model.bpmn.instance.IntermediateThrowEvent intermediateThrowEvent =
        (org.camunda.bpm.model.bpmn.instance.IntermediateThrowEvent) flowNode;
    Collection<EventDefinition> eventDefinitions = intermediateThrowEvent.getEventDefinitions();
    if (eventDefinitions.isEmpty()) {
      return new IntermediateThrowEvent(
          flowNode.getId(), getFlowElementName(flowNode), IntermediateThrowEventType.NONE);
    }
    if (eventDefinitions.size() == 1) {
      EventDefinition evDefinition = eventDefinitions.iterator().next();
      EventDefinitionVisitor<IntermediateThrowEvent> eventVisitor =
          new EventDefinitionVisitor<>() {
            @Override
            public IntermediateThrowEvent handle(MessageEventDefinition evDefinition) {
              return new IntermediateThrowEvent(
                  flowNode.getId(),
                  getFlowElementName(flowNode),
                  IntermediateThrowEventType.MESSAGE);
            }

            @Override
            public IntermediateThrowEvent handle(LinkEventDefinition evDefinition) {
              return new IntermediateThrowEvent(
                  flowNode.getId(), getFlowElementName(flowNode), IntermediateThrowEventType.LINK);
            }

            @Override
            public IntermediateThrowEvent handle(SignalEventDefinition evDefinition) {
              return new IntermediateThrowEvent(
                  flowNode.getId(),
                  getFlowElementName(flowNode),
                  IntermediateThrowEventType.SIGNAL,
                  mapSignalEventDefinition(evDefinition, flowNode));
            }

            @Override
            public IntermediateThrowEvent handle(EscalationEventDefinition evDefinition) {
              return new IntermediateThrowEvent(
                  flowNode.getId(),
                  getFlowElementName(flowNode),
                  IntermediateThrowEventType.ESCALATION,
                  mapEscalationEventDefinition(evDefinition));
            }

            @Override
            public IntermediateThrowEvent handle(ErrorEventDefinition evDefinition) {
              throw new BPMNRuntimeException(
                  "Intermediate throw event definitions should not be of type error!");
            }

            @Override
            public IntermediateThrowEvent handle(TerminateEventDefinition evDefinition) {
              throw new BPMNRuntimeException(
                  "Intermediate throw event definitions should not be of type terminate!");
            }

            @Override
            public IntermediateThrowEvent handle(TimerEventDefinition evDefinition) {
              throw new BPMNRuntimeException(
                  "Intermediate throw event definitions should not be of type timer!");
            }
          };
      return this.visitDefinition(evDefinition, eventVisitor);
    }
    throw new BPMNRuntimeException("Intermediate throw event has more than one event definition!");
  }

  private <T> T visitDefinition(
      org.camunda.bpm.model.bpmn.instance.EventDefinition evDefinition,
      EventDefinitionVisitor<T> eventVisitor) {
    if (evDefinition instanceof MessageEventDefinition messageEventDefinition) {
      return eventVisitor.handle(messageEventDefinition);
    }
    if (evDefinition instanceof LinkEventDefinition linkEventDefinition) {
      return eventVisitor.handle(linkEventDefinition);
    }
    if (evDefinition instanceof SignalEventDefinition signalEventDefinition) {
      return eventVisitor.handle(signalEventDefinition);
    }
    if (evDefinition instanceof TerminateEventDefinition terminateEventDefinition) {
      return eventVisitor.handle(terminateEventDefinition);
    }
    if (evDefinition instanceof TimerEventDefinition timerEventDefinition) {
      return eventVisitor.handle(timerEventDefinition);
    }
    if (evDefinition instanceof ErrorEventDefinition errEvDef) {
      return eventVisitor.handle(errEvDef);
    }
    if (evDefinition instanceof EscalationEventDefinition escEvDef) {
      return eventVisitor.handle(escEvDef);
    }
    throw new BPMNRuntimeException("Unknown event definition found!" + evDefinition);
  }
}
