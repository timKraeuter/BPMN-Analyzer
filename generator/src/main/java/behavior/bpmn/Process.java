package behavior.bpmn;

import behavior.bpmn.activities.CallActivity;
import behavior.bpmn.activities.tasks.ReceiveTask;
import behavior.bpmn.activities.tasks.SendTask;
import behavior.bpmn.activities.tasks.Task;
import behavior.bpmn.auxiliary.AbstractProcessVisitor;
import behavior.bpmn.auxiliary.FlowNodeVisitor;
import behavior.bpmn.events.EndEvent;
import behavior.bpmn.events.IntermediateCatchEvent;
import behavior.bpmn.events.IntermediateThrowEvent;
import behavior.bpmn.events.StartEvent;
import behavior.bpmn.gateways.EventBasedGateway;
import behavior.bpmn.gateways.ExclusiveGateway;
import behavior.bpmn.gateways.InclusiveGateway;
import behavior.bpmn.gateways.ParallelGateway;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Represents a process modeled in BPMN.
 */
public class Process extends AbstractProcess {
    private final Set<StartEvent> startEvents;

    public Process(
            String name,
            Set<StartEvent> startEvents,
            Set<SequenceFlow> sequenceFlows,
            Set<EventSubprocess> eventSubprocesses) {
        super(name, sequenceFlows, eventSubprocesses);
        this.startEvents = startEvents;
    }

    public Set<StartEvent> getStartEvents() {
        return this.startEvents;
    }

    public Stream<Process> getSubProcesses() {
        final LinkedHashSet<Process> subProcesses = new LinkedHashSet<>();
        getControlFlowNodes().forEach(flowNode -> flowNode.accept(new FlowNodeVisitor() {
            @Override
            public void handle(Task task) {
                // Not a subprocess
            }

            @Override
            public void handle(SendTask task) {
                // Not a subprocess
            }

            @Override
            public void handle(ReceiveTask task) {
                // Not a subprocess
            }

            @Override
            public void handle(CallActivity callActivity) {
                subProcesses.add(callActivity.getSubProcessModel());
                subProcesses.addAll(callActivity.getSubProcessModel().getSubProcesses().collect(Collectors.toList()));
            }

            @Override
            public void handle(ExclusiveGateway exclusiveGateway) {
                // Not a subprocess
            }

            @Override
            public void handle(ParallelGateway parallelGateway) {
                // Not a subprocess
            }

            @Override
            public void handle(InclusiveGateway inclusiveGateway) {
                // Not a subprocess
            }

            @Override
            public void handle(StartEvent startEvent) {
                // Not a subprocess
            }

            @Override
            public void handle(IntermediateThrowEvent intermediateThrowEvent) {
                // Not a subprocess
            }

            @Override
            public void handle(IntermediateCatchEvent intermediateCatchEvent) {
                // Not a subprocess
            }

            @Override
            public void handle(EndEvent endEvent) {
                // Not a subprocess
            }

            @Override
            public void handle(EventBasedGateway eventBasedGateway) {
                // Not a subprocess
            }
        }));
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
