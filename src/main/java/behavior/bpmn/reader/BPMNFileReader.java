package behavior.bpmn.reader;

import behavior.bpmn.BPMNCollaboration;
import behavior.bpmn.activities.tasks.ReceiveTask;
import behavior.bpmn.activities.tasks.SendTask;
import behavior.bpmn.activities.tasks.Task;
import behavior.bpmn.auxiliary.BPMNCollaborationBuilder;
import behavior.bpmn.events.EndEvent;
import behavior.bpmn.events.StartEvent;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.FlowNode;
import org.camunda.bpm.model.bpmn.instance.SequenceFlow;
import org.camunda.bpm.model.xml.instance.ModelElementInstance;
import org.camunda.bpm.model.xml.type.ModelElementType;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class BPMNFileReader {

    public BPMNCollaboration readModelFromFile(String filePath) {
        return readModelFromFile(new File(filePath));
    }

    public BPMNCollaboration readModelFromFile(File file) {
        BPMNCollaborationBuilder bpmnCollaborationBuilder = new BPMNCollaborationBuilder();
        BpmnModelInstance bpmnModelInstance = Bpmn.readModelFromFile(file);
        // TODO: Read general information such as name?
        // TODO: Do this for each pool?
        // Convert sequenceFlows
        convertSequenceFlows(bpmnModelInstance, bpmnCollaborationBuilder);
        return bpmnCollaborationBuilder.build();
    }

    private void convertSequenceFlows(BpmnModelInstance bpmnModelInstance, BPMNCollaborationBuilder bpmnCollaborationBuilder) {
        Map<String, behavior.bpmn.FlowNode> createdFlowNodes = new HashMap<>();
        ModelElementType sequenceFlowType = bpmnModelInstance.getModel().getType(SequenceFlow.class);
        Collection<ModelElementInstance> taskInstances = bpmnModelInstance.getModelElementsByType(sequenceFlowType);
        taskInstances.forEach(sequenceFlowElement -> {
            SequenceFlow sequenceFlow = (SequenceFlow) sequenceFlowElement;
            if (sequenceFlow.getName() == null || sequenceFlow.getName().isEmpty()) {
                bpmnCollaborationBuilder.sequenceFlow(
                        createFlowNodeEquivalent(sequenceFlow.getSource(), createdFlowNodes, bpmnCollaborationBuilder),
                        createFlowNodeEquivalent(sequenceFlow.getTarget(), createdFlowNodes, bpmnCollaborationBuilder)
                );
            } else {
                bpmnCollaborationBuilder.sequenceFlow(
                        sequenceFlow.getName(),
                        createFlowNodeEquivalent(sequenceFlow.getSource(), createdFlowNodes, bpmnCollaborationBuilder),
                        createFlowNodeEquivalent(sequenceFlow.getTarget(), createdFlowNodes, bpmnCollaborationBuilder)
                );
            }
        });
    }

    private behavior.bpmn.FlowNode createFlowNodeEquivalent(
            FlowNode flowNode,
            Map<String, behavior.bpmn.FlowNode> createdFlowNodes,
            BPMNCollaborationBuilder bpmnCollaborationBuilder) {
        behavior.bpmn.FlowNode flowNodeIfExists = createdFlowNodes.get(flowNode.getId());
        if (flowNodeIfExists != null) {
            return flowNodeIfExists;
        }
        String taskTypeName = flowNode.getElementType().getTypeName();
        // TODO: We probably need to save the ID somehow or use it instead of the name!
        behavior.bpmn.FlowNode resultingFlowNode;
        switch (taskTypeName) {
            case "startEvent":
                // TODO: Start event types???
                StartEvent startEvent = new StartEvent(flowNode.getName());
                // TODO: Multiple start events?
                bpmnCollaborationBuilder.startEvent(startEvent);
                resultingFlowNode = startEvent;
                break;
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
            case "endEvent":
                resultingFlowNode = new EndEvent(flowNode.getName());
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
            default:
                throw new RuntimeException(String.format("Unknown task type \"%s\" found!", taskTypeName));
        }
        createdFlowNodes.put(flowNode.getId(), resultingFlowNode);
        return resultingFlowNode;
    }
}
