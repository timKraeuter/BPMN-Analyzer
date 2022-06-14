package groove.behaviortransformer.bpmn.generators;

import behavior.bpmn.AbstractProcess;

public interface BPMNEventSubprocessRuleGenerator {
    void generateRulesForEventSubprocesses(AbstractProcess process);
}
