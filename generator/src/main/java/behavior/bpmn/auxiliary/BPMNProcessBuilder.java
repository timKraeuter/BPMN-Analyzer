package behavior.bpmn.auxiliary;

import behavior.bpmn.EventSubprocess;
import behavior.bpmn.FlowNode;
import behavior.bpmn.Process;
import behavior.bpmn.SequenceFlow;
import behavior.bpmn.events.StartEvent;

import java.util.LinkedHashSet;
import java.util.Set;

public class BPMNProcessBuilder implements BPMNModelBuilder {
    private final Set<SequenceFlow> sequenceFlows;
    private final Set<EventSubprocess> eventSubprocesses;
    private String name;
    private final Set<StartEvent> startEvents;

    public BPMNProcessBuilder() {
        this.sequenceFlows = new LinkedHashSet<>();
        eventSubprocesses = new LinkedHashSet<>();
        startEvents = new LinkedHashSet<>();
    }

    @Override
    public Set<StartEvent> getStartEvents() {
        return startEvents;
    }

    public Set<SequenceFlow> getSequenceFlows() {
        return sequenceFlows;
    }

    public BPMNProcessBuilder name(String name) {
        this.name = name;
        return this;
    }

    @Override
    public BPMNProcessBuilder startEvent(StartEvent event) {
        this.startEvents.add(event);
        return this;
    }

    @Override
    public BPMNProcessBuilder eventSubprocess(EventSubprocess eventSubprocess) {
        eventSubprocesses.add(eventSubprocess);
        return this;
    }

    @Override
    public BPMNProcessBuilder sequenceFlow(String name, FlowNode from, FlowNode to) {
        final SequenceFlow sequenceFlow = new SequenceFlow(name, from, to);
        this.sequenceFlows.add(sequenceFlow);
        from.addOutgoingSequenceFlow(sequenceFlow);
        to.addIncomingSequenceFlow(sequenceFlow);
        return this;
    }

    @Override
    public BPMNProcessBuilder sequenceFlow(FlowNode from, FlowNode to) {
        return sequenceFlow("", from, to);
    }

    public Process build() {
        return new Process(name, startEvents, sequenceFlows, eventSubprocesses);
    }
}
