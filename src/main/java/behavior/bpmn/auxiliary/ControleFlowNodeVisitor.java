package behavior.bpmn.auxiliary;

import behavior.bpmn.*;

public interface ControleFlowNodeVisitor {
    void handle(EndEvent endEvent);

    void handle(AlternativeGateway alternativeGateway);

    void handle(Activity activity);

    void handle(ParallelGateway parallelGateway);

    void handle(StartEvent startEvent);
}
