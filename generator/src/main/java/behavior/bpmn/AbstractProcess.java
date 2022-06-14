package behavior.bpmn;

import behavior.bpmn.auxiliary.AbstractProcessVisitor;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Stream;

public abstract class AbstractProcess {
    private final String name;
    private final Set<SequenceFlow> sequenceFlows;
    private final Set<EventSubprocess> eventSubprocesses;

    protected AbstractProcess(String name, Set<SequenceFlow> sequenceFlows, Set<EventSubprocess> eventSubprocesses) {
        this.name = name;
        this.sequenceFlows = sequenceFlows;
        this.eventSubprocesses = eventSubprocesses;
    }

    public Stream<EventSubprocess> getEventSubprocesses() {
        return eventSubprocesses.stream();
    }

    public Stream<SequenceFlow> getSequenceFlows() {
        return sequenceFlows.stream();
    }

    public Stream<FlowNode> getControlFlowNodes() {
        final LinkedHashSet<FlowNode> nodes = new LinkedHashSet<>();
        sequenceFlows.forEach(sequenceFlow -> {
            nodes.add(sequenceFlow.getSource());
            nodes.add(sequenceFlow.getTarget());
        });
        return nodes.stream();
    }

    public String getName() {
        return name;
    }

    public abstract void accept(AbstractProcessVisitor visitor);
}
