package behavior.bpmn.auxiliary;

import behavior.bpmn.*;

public interface ControlFlowNodeVisitor {
    void handle(StartEvent startEvent);

    void handle(Activity activity);

    void handle(AlternativeGateway alternativeGateway);

    void handle(ParallelGateway parallelGateway);

    void handle(EndEvent endEvent);
}
