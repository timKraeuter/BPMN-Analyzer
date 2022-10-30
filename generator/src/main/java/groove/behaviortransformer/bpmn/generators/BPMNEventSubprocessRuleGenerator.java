package groove.behaviortransformer.bpmn.generators;

import behavior.bpmn.AbstractBPMNProcess;

public interface BPMNEventSubprocessRuleGenerator {
  void generateRulesForEventSubprocesses(AbstractBPMNProcess process);
}
