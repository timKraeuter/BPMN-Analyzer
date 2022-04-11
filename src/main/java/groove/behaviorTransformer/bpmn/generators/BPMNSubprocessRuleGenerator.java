package groove.behaviorTransformer.bpmn.generators;

import behavior.bpmn.AbstractProcess;
import behavior.bpmn.activities.CallActivity;

public interface BPMNSubprocessRuleGenerator {
    void createCallActivityRulesForProcess(AbstractProcess process, CallActivity callActivity);
}
