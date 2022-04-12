package behavior.bpmn.auxiliary;

import behavior.bpmn.activities.CallActivity;
import behavior.bpmn.activities.tasks.ReceiveTask;
import behavior.bpmn.activities.tasks.SendTask;
import behavior.bpmn.activities.tasks.Task;
import behavior.bpmn.gateways.EventBasedGateway;
import behavior.bpmn.gateways.ExclusiveGateway;
import behavior.bpmn.gateways.InclusiveGateway;
import behavior.bpmn.gateways.ParallelGateway;

public interface FlowNodeVisitor extends EventVisitor {
    void handle(Task task);

    void handle(SendTask task);

    void handle(ReceiveTask task);

    void handle(CallActivity callActivity);

    void handle(ExclusiveGateway exclusiveGateway);

    void handle(ParallelGateway parallelGateway);

    void handle(InclusiveGateway inclusiveGateway);

    void handle(EventBasedGateway eventBasedGateway);
}
