package groove.behaviorTransformer.bpmn.generators;

import behavior.bpmn.AbstractProcess;
import behavior.bpmn.events.EndEvent;
import behavior.bpmn.events.IntermediateCatchEvent;
import behavior.bpmn.events.IntermediateThrowEvent;
import behavior.bpmn.events.StartEvent;

public interface BPMNEventRuleGenerator {
    void createStartEventRulesForProcess(AbstractProcess process, StartEvent startEvent);

    void createEndEventRule(AbstractProcess process, EndEvent endEvent);

    void createIntermediateThrowEventRule(AbstractProcess process, IntermediateThrowEvent intermediateThrowEvent);

    void createIntermediateCatchEventRule(AbstractProcess process, IntermediateCatchEvent intermediateCatchEvent);
}
