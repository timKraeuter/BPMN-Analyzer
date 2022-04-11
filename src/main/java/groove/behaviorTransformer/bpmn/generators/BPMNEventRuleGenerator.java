package groove.behaviorTransformer.bpmn.generators;

import behavior.bpmn.AbstractProcess;
import behavior.bpmn.events.StartEvent;

public interface BPMNEventRuleGenerator {
    void createStartEventRulesForProcess(AbstractProcess process, StartEvent startEvent);
}
