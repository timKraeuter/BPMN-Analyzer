package behavior.bpmn.auxiliary;

import behavior.bpmn.Process;
import behavior.bpmn.*;
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

public class BPMNCollaborationBuilder {
    private final Set<MessageFlow> messageFlows;
    private final Set<Process> participants;
    private final Set<Process> subprocesses;
    private String name;

    private String processName;
    private StartEvent startEvent;
    private Set<SequenceFlow> sequenceFlows;

    public BPMNCollaborationBuilder() {
        this.sequenceFlows = new LinkedHashSet<>();
        messageFlows = new LinkedHashSet<>();
        participants = new LinkedHashSet<>();
        subprocesses = new LinkedHashSet<>();
    }

    public BPMNCollaborationBuilder name(String name) {
        this.name = name;
        return this;
    }

    public BPMNCollaborationBuilder processName(String processName) {
        this.processName = processName;
        return this;
    }

    public BPMNCollaborationBuilder startEvent(StartEvent event) {
        this.startEvent = event;
        return this;
    }

    public BPMNCollaborationBuilder sequenceFlow(String name, FlowNode from, FlowNode to) {
        final SequenceFlow sequenceFlow = new SequenceFlow(name, from, to);
        this.sequenceFlows.add(sequenceFlow);
        from.addOutgoingSequenceFlow(sequenceFlow);
        to.addIncomingSequenceFlow(sequenceFlow);

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

    public BPMNCollaborationBuilder sequenceFlow(FlowNode from, FlowNode to) {
        return sequenceFlow("", from, to);
    }

    public BPMNCollaborationBuilder buildProcess() {
        this.participants.add(new Process(processName, startEvent, sequenceFlows));
        processName = "";
        startEvent = null;
        sequenceFlows = new LinkedHashSet<>();
        return this;
    }

    public BPMNCollaborationBuilder messageFlow(FlowNode source, FlowNode target) {
        this.messageFlows.add(new MessageFlow("", source, target));
        return this;
    }

    public BPMNCollaboration build() {
        if (startEvent != null || sequenceFlows.size() >= 1) {
            this.buildProcess();
        }
        return new BPMNCollaboration(name, participants, subprocesses, messageFlows);
    }
}
