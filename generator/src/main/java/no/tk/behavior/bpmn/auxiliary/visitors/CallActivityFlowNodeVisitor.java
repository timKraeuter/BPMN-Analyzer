package no.tk.behavior.bpmn.auxiliary.visitors;

import no.tk.behavior.bpmn.activities.CallActivity;

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
