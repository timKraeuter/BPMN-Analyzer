package no.tk.groove.behaviortransformer.bpmn.generators;

import no.tk.behavior.bpmn.AbstractBPMNProcess;
import no.tk.behavior.bpmn.activities.CallActivity;

public interface BPMNSubprocessRuleGenerator {
  void createCallActivityRulesForProcess(AbstractBPMNProcess process, CallActivity callActivity);

  default boolean subprocessHasStartEvents(CallActivity callActivity) {
    return !callActivity.getSubProcessModel().getStartEvents().isEmpty();
  }
}
