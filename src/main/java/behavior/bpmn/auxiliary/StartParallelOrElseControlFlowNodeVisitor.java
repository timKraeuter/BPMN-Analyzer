package behavior.bpmn.auxiliary;

import behavior.bpmn.Activity;
import behavior.bpmn.ControlFlowNode;
import behavior.bpmn.events.EndEvent;
import behavior.bpmn.gateways.ExclusiveGateway;

public interface StartParallelOrElseControlFlowNodeVisitor extends ControlFlowNodeVisitor{
    @Override
    default void handle(Activity activity) {
        this.handleRest(activity);
    }

    @Override
    default void handle(ExclusiveGateway exclusiveGateway) {
        this.handleRest(exclusiveGateway);
    }

    @Override
    default void handle(EndEvent endEvent) {
        this.handleRest(endEvent);
    }

    /**
     * Gets called for controle flows which are not a start event or a parallel gateway.
     */
    void handleRest(ControlFlowNode node);
}
