package behavior.bpmn.auxiliary;

import behavior.bpmn.Process;
import behavior.bpmn.*;
import behavior.bpmn.events.StartEvent;

import java.util.LinkedHashSet;
import java.util.Set;

public class BPMNCollaborationBuilder {
    private final Set<MessageFlow> messageFlows;
    private final Set<Process> participants;
    private String name;

    private String processName;
    private StartEvent startEvent;
    private Set<SequenceFlow> sequenceFlows;

    public BPMNCollaborationBuilder() {
        this.sequenceFlows = new LinkedHashSet<>();
        messageFlows = new LinkedHashSet<>();
        participants = new LinkedHashSet<>();
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
        // We could check that this sequence flow is connected to the already created part of the model.
        final SequenceFlow sequenceFlow = new SequenceFlow(name, from, to);
        this.sequenceFlows.add(sequenceFlow);
        from.addOutgoingSequenceFlow(sequenceFlow);
        to.addIncomingSequenceFlow(sequenceFlow);
        return this;
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
        return new BPMNCollaboration(name, participants, messageFlows);
    }
}
