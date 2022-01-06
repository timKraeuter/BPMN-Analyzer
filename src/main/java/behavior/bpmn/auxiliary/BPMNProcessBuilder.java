package behavior.bpmn.auxiliary;

import behavior.bpmn.BPMNProcessModel;
import behavior.bpmn.ControlFlowNode;
import behavior.bpmn.SequenceFlow;
import behavior.bpmn.events.EndEvent;
import behavior.bpmn.events.StartEvent;

import java.util.LinkedHashSet;
import java.util.Set;

public class BPMNProcessBuilder {
    private String name;
    private StartEvent startEvent;
    private final Set<EndEvent> endEvents;
    private final Set<SequenceFlow> sequenceFlows;

    public BPMNProcessBuilder() {
        this.endEvents = new LinkedHashSet<>();
        this.sequenceFlows = new LinkedHashSet<>();
    }

    public BPMNProcessBuilder name(String name) {
        this.name = name;
        return this;
    }

    public BPMNProcessBuilder startEvent(StartEvent event) {
        this.startEvent = event;
        return this;
    }

    public BPMNProcessBuilder endEvent(EndEvent event) {
        this.endEvents.add(event);
        return this;
    }

    public BPMNProcessBuilder sequenceFlow(String name, ControlFlowNode from, ControlFlowNode to) {
        // We could check that this sequence flow is connected to the already created part of the model.
        final SequenceFlow sequenceFlow = new SequenceFlow(name, from, to);
        this.sequenceFlows.add(sequenceFlow);
        from.addOutgoingSequenceFlow(sequenceFlow);
        to.addIncomingSequenceFlow(sequenceFlow);
        return this;
    }

    public BPMNProcessModel build() {
        return new BPMNProcessModel(this.name, this.startEvent, this.endEvents, this.sequenceFlows);
    }
}
