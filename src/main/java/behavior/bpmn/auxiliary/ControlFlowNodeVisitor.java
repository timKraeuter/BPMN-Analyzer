package behavior.bpmn.auxiliary;

import behavior.bpmn.Task;
import behavior.bpmn.events.EndEvent;
import behavior.bpmn.events.LinkEvent;
import behavior.bpmn.events.StartEvent;
import behavior.bpmn.gateways.ExclusiveGateway;
import behavior.bpmn.gateways.InclusiveGateway;
import behavior.bpmn.gateways.ParallelGateway;

public interface ControlFlowNodeVisitor {
    void handle(Task task);

    void handle(ExclusiveGateway exclusiveGateway);

    void handle(ParallelGateway parallelGateway);

    void handle(InclusiveGateway inclusiveGateway);

    void handle(StartEvent startEvent);

    void handle(LinkEvent linkEvent);

    void handle(EndEvent endEvent);
}
