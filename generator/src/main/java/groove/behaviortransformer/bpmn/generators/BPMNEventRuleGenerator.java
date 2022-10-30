package groove.behaviortransformer.bpmn.generators;

import behavior.bpmn.AbstractBPMNProcess;
import behavior.bpmn.events.EndEvent;
import behavior.bpmn.events.IntermediateCatchEvent;
import behavior.bpmn.events.IntermediateThrowEvent;
import behavior.bpmn.events.StartEvent;

public interface BPMNEventRuleGenerator {
  void createStartEventRulesForProcess(AbstractBPMNProcess process, StartEvent startEvent);

  void createEndEventRule(AbstractBPMNProcess process, EndEvent endEvent);

  void createIntermediateThrowEventRule(
      AbstractBPMNProcess process, IntermediateThrowEvent intermediateThrowEvent);

  void createIntermediateCatchEventRule(
      AbstractBPMNProcess process, IntermediateCatchEvent intermediateCatchEvent);
}
