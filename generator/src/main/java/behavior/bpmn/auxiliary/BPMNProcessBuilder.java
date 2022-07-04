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
    private final Set<FlowNode> flowNodes;
    private final Set<EventSubprocess> eventSubprocesses;
    private String name;
    private final Set<StartEvent> startEvents;

    public BPMNProcessBuilder() {
        this.sequenceFlows = new LinkedHashSet<>();
        eventSubprocesses = new LinkedHashSet<>();
        flowNodes = new LinkedHashSet<>();
        startEvents = new LinkedHashSet<>();
    }

    @Override
    public Set<StartEvent> getStartEvents() {
        return startEvents;
    }

    public Set<SequenceFlow> getSequenceFlows() {
        return sequenceFlows;
    }

    public Set<FlowNode> getFlowNodes() {
        return flowNodes;
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
    public BPMNProcessBuilder sequenceFlow(String id, String name, FlowNode from, FlowNode to) {
        final SequenceFlow sequenceFlow = new SequenceFlow(id, name, from, to);
        this.sequenceFlows.add(sequenceFlow);
        from.addOutgoingSequenceFlow(sequenceFlow);
        to.addIncomingSequenceFlow(sequenceFlow);

        flowNodes.add(from);
        flowNodes.add(to);
        return this;
    }

    @Override
    public BPMNProcessBuilder sequenceFlow(String id, FlowNode from, FlowNode to) {
        return sequenceFlow(id, "", from, to);
    }

    @Override
    public BPMNModelBuilder flowNode(FlowNode flowNode) {
        flowNodes.add(flowNode);
        return this;
    }

    public Process build() {
        return new Process(name, startEvents, sequenceFlows, flowNodes, eventSubprocesses);
    }
}
