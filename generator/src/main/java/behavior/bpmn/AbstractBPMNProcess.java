package behavior.bpmn;

import behavior.bpmn.auxiliary.visitors.AbstractProcessVisitor;

import java.util.Set;
import java.util.stream.Stream;

public abstract class AbstractBPMNProcess {
    private final String name;
    private final Set<SequenceFlow> sequenceFlows;
    private final Set<FlowNode> flowNodes;
    private final Set<BPMNEventSubprocess> eventSubprocesses;

    protected AbstractBPMNProcess(String name,
                                  Set<SequenceFlow> sequenceFlows,
                                  Set<FlowNode> flowNodes,
                                  Set<BPMNEventSubprocess> eventSubprocesses) {
        this.name = name;
        this.sequenceFlows = sequenceFlows;
        this.flowNodes = flowNodes;
        this.eventSubprocesses = eventSubprocesses;
    }

    public Stream<BPMNEventSubprocess> getEventSubprocesses() {
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

    public abstract boolean isEventSubprocess();
}
