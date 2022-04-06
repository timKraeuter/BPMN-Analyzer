package behavior.bpmn.auxiliary;

import behavior.bpmn.BPMNCollaboration;
import behavior.bpmn.FlowNode;
import behavior.bpmn.MessageFlow;
import behavior.bpmn.Process;
import behavior.bpmn.activities.CallActivity;
import behavior.bpmn.activities.tasks.ReceiveTask;
import behavior.bpmn.activities.tasks.SendTask;
import behavior.bpmn.activities.tasks.Task;
import behavior.bpmn.events.EndEvent;
import behavior.bpmn.events.IntermediateCatchEvent;
import behavior.bpmn.events.IntermediateThrowEvent;
import behavior.bpmn.events.StartEvent;
import behavior.bpmn.gateways.EventBasedGateway;
import behavior.bpmn.gateways.ExclusiveGateway;
import behavior.bpmn.gateways.InclusiveGateway;
import behavior.bpmn.gateways.ParallelGateway;

import java.util.LinkedHashSet;
import java.util.Set;

public class BPMNCollaborationBuilder implements BPMNModelBuilder {
    private final Set<MessageFlow> messageFlows;
    private final Set<Process> participants;
    private final Set<Process> subprocesses;
    private String name;

    private BPMNProcessBuilder currentProcessBuilder;

    public BPMNCollaborationBuilder() {
        messageFlows = new LinkedHashSet<>();
        participants = new LinkedHashSet<>();
        subprocesses = new LinkedHashSet<>();

        currentProcessBuilder = new BPMNProcessBuilder();
    }

    @Override
    public StartEvent getStartEvent() {
        return currentProcessBuilder.getStartEvent();
    }

    public BPMNCollaborationBuilder name(String name) {
        this.name = name;
        return this;
    }

    public BPMNCollaborationBuilder processName(String processName) {
        currentProcessBuilder.name(processName);
        return this;
    }

    @Override
    public BPMNCollaborationBuilder startEvent(StartEvent event) {
        currentProcessBuilder.startEvent(event);
        return this;
    }

    @Override
    public BPMNCollaborationBuilder sequenceFlow(String name, FlowNode from, FlowNode to) {
        currentProcessBuilder.sequenceFlow(name, from, to);

        this.findAndAddSubProcessIfPresent(from);
        this.findAndAddSubProcessIfPresent(to);
        return this;
    }

    private void findAndAddSubProcessIfPresent(FlowNode node) {
        node.accept(new FlowNodeVisitor() {
            @Override
            public void handle(Task task) {
            }

            @Override
            public void handle(SendTask task) {
            }

            @Override
            public void handle(ReceiveTask task) {
            }

            @Override
            public void handle(CallActivity callActivity) {
                Process subProcessModel = callActivity.getSubProcessModel();
                if (!subprocesses.contains(subProcessModel) || !participants.contains(subProcessModel)) {
                    subprocesses.add(subProcessModel);
                    subProcessModel.getControlFlowNodes().forEach(flowNode -> findAndAddSubProcessIfPresent(flowNode));
                }
            }

            @Override
            public void handle(ExclusiveGateway exclusiveGateway) {
            }

            @Override
            public void handle(ParallelGateway parallelGateway) {
            }

            @Override
            public void handle(InclusiveGateway inclusiveGateway) {
            }

            @Override
            public void handle(StartEvent startEvent) {
            }

            @Override
            public void handle(IntermediateThrowEvent intermediateThrowEvent) {
            }

            @Override
            public void handle(IntermediateCatchEvent intermediateCatchEvent) {
            }

            @Override
            public void handle(EndEvent endEvent) {
            }

            @Override
            public void handle(EventBasedGateway eventBasedGateway) {
            }
        });
    }

    @Override
    public BPMNCollaborationBuilder sequenceFlow(FlowNode from, FlowNode to) {
        return sequenceFlow("", from, to);
    }

    public BPMNCollaborationBuilder buildProcess() {
        this.participants.add(currentProcessBuilder.build());
        currentProcessBuilder = new BPMNProcessBuilder();
        return this;
    }

    public BPMNCollaborationBuilder messageFlow(FlowNode source, FlowNode target) {
        this.messageFlows.add(new MessageFlow("", source, target));
        return this;
    }

    public BPMNCollaboration build() {
        if (currentProcessBuilder.getStartEvent() != null || currentProcessBuilder.getSequenceFlows().size() >= 1) {
            this.buildProcess();
        }
        return new BPMNCollaboration(name, participants, subprocesses, messageFlows);
    }
}
