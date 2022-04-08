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
import util.ValueWrapper;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class EventSubprocess extends AbstractProcess {
    public EventSubprocess(String name, Set<SequenceFlow> sequenceFlows, Set<EventSubprocess> eventSubprocesses) {
        super(name, sequenceFlows, eventSubprocesses);
    }

    @Override
    public void accept(AbstractProcessVisitor visitor) {
        visitor.handle(this);
    }

    public Set<StartEvent> getStartEvents() {
        return this.getControlFlowNodes().map(flowNode -> {
                       ValueWrapper<StartEvent> valueWrapper = new ValueWrapper<>();
                       flowNode.accept(new FlowNodeVisitor() {
                           @Override
                           public void handle(Task task) {
                           }

                           @Override
                           public void handle(SendTask task) {
                           }

                           @Override
                           public void handle(ReceiveTask task) {
                           }

                           @Override
                           public void handle(CallActivity callActivity) {
                           }

                           @Override
                           public void handle(ExclusiveGateway exclusiveGateway) {
                           }

                           @Override
                           public void handle(ParallelGateway parallelGateway) {
                           }

                           @Override
                           public void handle(InclusiveGateway inclusiveGateway) {
                           }

                           @Override
                           public void handle(StartEvent startEvent) {
                               valueWrapper.setValue(startEvent);
                           }

                           @Override
                           public void handle(IntermediateThrowEvent intermediateThrowEvent) {
                           }

                           @Override
                           public void handle(IntermediateCatchEvent intermediateCatchEvent) {
                           }

                           @Override
                           public void handle(EndEvent endEvent) {
                           }

                           @Override
                           public void handle(EventBasedGateway eventBasedGateway) {
                           }
                       });
                       return valueWrapper.getValueIfExists();
                   })
                   .filter(Objects::nonNull)
                   .collect(Collectors.toCollection(LinkedHashSet::new));
    }
}
