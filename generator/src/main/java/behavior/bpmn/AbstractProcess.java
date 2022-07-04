package behavior.bpmn;

import behavior.bpmn.auxiliary.AbstractProcessVisitor;

import java.util.Set;
import java.util.stream.Stream;

public abstract class AbstractProcess {
    private final String name;
    private final Set<SequenceFlow> sequenceFlows;
    private final Set<FlowNode> flowNodes;
    private final Set<EventSubprocess> eventSubprocesses;

    protected AbstractProcess(String name,
                              Set<SequenceFlow> sequenceFlows,
                              Set<FlowNode> flowNodes,
                              Set<EventSubprocess> eventSubprocesses) {
        this.name = name;
        this.sequenceFlows = sequenceFlows;
        this.flowNodes = flowNodes;
        this.eventSubprocesses = eventSubprocesses;
    }

    public Stream<EventSubprocess> getEventSubprocesses() {
        return eventSubprocesses.stream();
    }

    public Stream<SequenceFlow> getSequenceFlows() {
        return sequenceFlows.stream();
    }

    public Stream<FlowNode> getFlowNodes() {
        return flowNodes.stream();
    }

    public String getName() {
        return name;
    }

    public abstract void accept(AbstractProcessVisitor visitor);
}
