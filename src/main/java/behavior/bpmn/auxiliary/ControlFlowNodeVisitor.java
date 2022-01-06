package behavior.bpmn.auxiliary;

import behavior.bpmn.Activity;
import behavior.bpmn.events.EndEvent;
import behavior.bpmn.events.StartEvent;
import behavior.bpmn.gateways.ExclusiveGateway;
import behavior.bpmn.gateways.ParallelGateway;

public interface ControlFlowNodeVisitor {
    void handle(StartEvent startEvent);

    void handle(Activity activity);

    void handle(ExclusiveGateway exclusiveGateway);

    void handle(ParallelGateway parallelGateway);

    void handle(EndEvent endEvent);
}
