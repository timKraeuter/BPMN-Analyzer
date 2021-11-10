package behavior.bpmn.auxiliary;

import behavior.bpmn.*;

import java.util.LinkedHashSet;
import java.util.Set;

public class BPMNProcessBuilder {
    private String name;
    private StartEvent startEvent;
    private final Set<EndEvent> endEvents;
    private final Set<SequenceFlow> sequenceFlows;

    public BPMNProcessBuilder() {
        endEvents = new LinkedHashSet<>();
        sequenceFlows = new LinkedHashSet<>();
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

    public BPMNProcessBuilder sequenceFlow(ControleFlowNode from, ControleFlowNode to) {
        // We could check that this sequence flow is connected to the already created part of the model.
        final SequenceFlow sequenceFlow = new SequenceFlow(from, to);
        this.sequenceFlows.add(sequenceFlow);
        return this;
    }

    public BPMNProcessModel build() {
        return new BPMNProcessModel(name, startEvent, endEvents, sequenceFlows);
    }
}
