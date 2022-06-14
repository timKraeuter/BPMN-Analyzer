package behavior.bpmn.auxiliary;

import behavior.bpmn.gateways.EventBasedGateway;
import behavior.bpmn.gateways.ExclusiveGateway;
import behavior.bpmn.gateways.InclusiveGateway;
import behavior.bpmn.gateways.ParallelGateway;

public interface FlowNodeVisitor extends EventVisitor, ActivityVisitor {
    void handle(ExclusiveGateway exclusiveGateway);

    void handle(ParallelGateway parallelGateway);

    void handle(InclusiveGateway inclusiveGateway);

    void handle(EventBasedGateway eventBasedGateway);
}
