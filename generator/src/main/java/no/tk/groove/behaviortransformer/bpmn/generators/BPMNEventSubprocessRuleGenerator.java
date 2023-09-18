package no.tk.groove.behaviortransformer.bpmn.generators;

import no.tk.behavior.bpmn.AbstractBPMNProcess;

public interface BPMNEventSubprocessRuleGenerator {
  void generateRulesForEventSubprocesses(AbstractBPMNProcess process);
}
