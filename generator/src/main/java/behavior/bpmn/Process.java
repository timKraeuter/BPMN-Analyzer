package behavior.bpmn;

import behavior.bpmn.auxiliary.visitors.AbstractProcessVisitor;
import behavior.bpmn.auxiliary.visitors.CallActivityFlowNodeVisitor;
import behavior.bpmn.events.StartEvent;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Represents a process modeled in BPMN.
 */
public class Process extends AbstractProcess {
    private final Set<StartEvent> startEvents;

    public Process(String name,
                   Set<StartEvent> startEvents,
                   Set<SequenceFlow> sequenceFlows,
                   Set<FlowNode> flowNodes,
                   Set<EventSubprocess> eventSubprocesses) {
        super(name, sequenceFlows, flowNodes, eventSubprocesses);
        this.startEvents = startEvents;
    }

    public Set<StartEvent> getStartEvents() {
        return this.startEvents;
    }

    public Stream<Process> getSubProcesses() {
        final LinkedHashSet<Process> subProcesses = new LinkedHashSet<>();
        getFlowNodes().forEach(flowNode -> flowNode.accept(new CallActivityFlowNodeVisitor(callActivity -> {
            subProcesses.add(callActivity.getSubProcessModel());
            subProcesses.addAll(callActivity.getSubProcessModel().getSubProcesses().collect(Collectors.toList()));
        })));
        return subProcesses.stream();
    }

    @Override
    public void accept(AbstractProcessVisitor visitor) {
        visitor.handle(this);
    }

    /*
    According to the BPMN spec the following consistency rules exist:
    - Gateways or Activities without incoming sequence flows are forbidden (p426)
     */
}
