package behavior.bpmn;

import behavior.Behavior;
import behavior.BehaviorVisitor;
import behavior.bpmn.events.StartEvent;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Stream;

public class BPMNProcessModel implements Behavior {
    // TODO: Add pools somehow.
    private final String name;
    private final StartEvent startEvent;
    private final Set<SequenceFlow> sequenceFlows;

    public BPMNProcessModel(String name, StartEvent startEvent, Set<SequenceFlow> sequenceFlows) {
        this.name = name;
        this.startEvent = startEvent;
        this.sequenceFlows = sequenceFlows;
    }

    public StartEvent getStartEvent() {
        return this.startEvent;
    }

    public Stream<FlowNode> getControlFlowNodes() {
        final LinkedHashSet<FlowNode> nodes = new LinkedHashSet<>();
        sequenceFlows.forEach(sequenceFlow -> {
            nodes.add(sequenceFlow.getSource());
            nodes.add(sequenceFlow.getTarget());
        });
        return nodes.stream();
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public void accept(BehaviorVisitor visitor) {
        visitor.handle(this);
    }

    /*
    According to the BPMN spec the following consistency rules exist:
    - Gateways or Activities without incoming sequence flows are forbidden (p426)
     */
}
