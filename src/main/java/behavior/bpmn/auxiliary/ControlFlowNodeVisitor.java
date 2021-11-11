package behavior.bpmn.auxiliary;

import behavior.bpmn.*;

public interface ControlFlowNodeVisitor {
    void handle(StartEvent startEvent);

    void handle(Activity activity);

    void handle(ExclusiveGateway exclusiveGateway);

    void handle(ParallelGateway parallelGateway);

    void handle(EndEvent endEvent);
}
