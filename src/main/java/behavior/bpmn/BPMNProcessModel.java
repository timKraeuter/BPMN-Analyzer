package behavior.bpmn;

import behavior.Behavior;
import behavior.BehaviorVisitor;
import behavior.bpmn.events.EndEvent;
import behavior.bpmn.events.StartEvent;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Stream;

public class BPMNProcessModel implements Behavior {
    private final String name;
    private final StartEvent startEvent;
    private final Set<EndEvent> endEvents;
    private final Set<SequenceFlow> sequenceFlows;

    public BPMNProcessModel(String name, StartEvent startEvent, Set<EndEvent> endEvents, Set<SequenceFlow> sequenceFlows) {
        this.name = name;
        this.startEvent = startEvent;
        this.endEvents = endEvents;
        this.sequenceFlows = sequenceFlows;
    }

    public StartEvent getStartEvent() {
        return this.startEvent;
    }

    public Stream<ControlFlowNode> getControlFlowNodes() {
        final LinkedHashSet<ControlFlowNode> nodes = new LinkedHashSet<>();
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

    public Set<EndEvent> getEndEvents() {
        return this.endEvents;
    }

    /*
    According to the BPMN spec the following consistency rules exist:
    - Gateways or Activities without incoming sequence flows are forbidden (p426)
     */
}
