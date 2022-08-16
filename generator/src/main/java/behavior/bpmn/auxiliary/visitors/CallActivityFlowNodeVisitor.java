package behavior.bpmn.auxiliary.visitors;

import behavior.bpmn.activities.CallActivity;
import behavior.bpmn.activities.tasks.ReceiveTask;
import behavior.bpmn.activities.tasks.SendTask;
import behavior.bpmn.activities.tasks.Task;
import behavior.bpmn.events.EndEvent;
import behavior.bpmn.events.IntermediateCatchEvent;
import behavior.bpmn.events.IntermediateThrowEvent;
import behavior.bpmn.events.StartEvent;
import behavior.bpmn.gateways.EventBasedGateway;
import behavior.bpmn.gateways.ExclusiveGateway;
import behavior.bpmn.gateways.InclusiveGateway;
import behavior.bpmn.gateways.ParallelGateway;

import java.util.function.Consumer;

public class CallActivityFlowNodeVisitor implements FlowNodeVisitor {

    private final Consumer<CallActivity> callActivityConsumer;

    public CallActivityFlowNodeVisitor(Consumer<CallActivity> callActivityConsumer) {
        this.callActivityConsumer = callActivityConsumer;
    }

    @Override
    public void handle(CallActivity callActivity) {
        callActivityConsumer.accept(callActivity);
    }

    @Override
    public void handle(Task task) {
        // Not a call activity
    }

    @Override
    public void handle(SendTask task) {
        // Not a call activity
    }

    @Override
    public void handle(ReceiveTask task) {
        // Not a call activity
    }

    @Override
    public void handle(ExclusiveGateway exclusiveGateway) {
        // Not a call activity
    }

    @Override
    public void handle(ParallelGateway parallelGateway) {
        // Not a call activity
    }

    @Override
    public void handle(InclusiveGateway inclusiveGateway) {
        // Not a call activity
    }

    @Override
    public void handle(StartEvent startEvent) {
        // Not a call activity
    }

    @Override
    public void handle(IntermediateThrowEvent intermediateThrowEvent) {
        // Not a call activity
    }

    @Override
    public void handle(IntermediateCatchEvent intermediateCatchEvent) {
        // Not a call activity
    }

    @Override
    public void handle(EndEvent endEvent) {
        // Not a call activity
    }

    @Override
    public void handle(EventBasedGateway eventBasedGateway) {
        // Not a call activity
    }

}
