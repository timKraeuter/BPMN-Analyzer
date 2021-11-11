package behavior.bpmn.auxiliary;

import behavior.bpmn.*;

public interface StartParallelOrElseControlFlowNodeVisitor extends ControlFlowNodeVisitor{
    default void handle(Activity activity) {
        handleRest(activity);
    }

    default void handle(ExclusiveGateway exclusiveGateway) {
        handleRest(exclusiveGateway);
    }

    default void handle(EndEvent endEvent) {
        handleRest(endEvent);
    }

    /**
     * Gets called for controle flows which are not a start event or a parallel gateway.
     */
    void handleRest(ControlFlowNode node);
}
