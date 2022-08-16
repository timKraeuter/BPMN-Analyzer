package behavior.bpmn.auxiliary.visitors;

import behavior.bpmn.activities.CallActivity;

import java.util.function.Consumer;

public class CallActivityFlowNodeVisitor extends DoNothingFlowNodeVisitor {

    private final Consumer<CallActivity> callActivityConsumer;

    public CallActivityFlowNodeVisitor(Consumer<CallActivity> callActivityConsumer) {
        this.callActivityConsumer = callActivityConsumer;
    }

    @Override
    public void handle(CallActivity callActivity) {
        callActivityConsumer.accept(callActivity);
    }
}
