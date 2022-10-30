package groove.behaviortransformer.bpmn.generators;

import behavior.bpmn.AbstractBPMNProcess;
import behavior.bpmn.activities.CallActivity;

public interface BPMNSubprocessRuleGenerator {
  void createCallActivityRulesForProcess(AbstractBPMNProcess process, CallActivity callActivity);

  default boolean subprocessHasStartEvents(CallActivity callActivity) {
    return !callActivity.getSubProcessModel().getStartEvents().isEmpty();
  }
}
