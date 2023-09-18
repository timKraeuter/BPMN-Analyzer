package no.tk.groove.behaviortransformer.bpmn.generators;

import no.tk.behavior.bpmn.AbstractBPMNProcess;
import no.tk.behavior.bpmn.events.EndEvent;
import no.tk.behavior.bpmn.events.IntermediateCatchEvent;
import no.tk.behavior.bpmn.events.IntermediateThrowEvent;
import no.tk.behavior.bpmn.events.StartEvent;

public interface BPMNEventRuleGenerator {
  void createStartEventRulesForProcess(AbstractBPMNProcess process, StartEvent startEvent);

  void createEndEventRule(AbstractBPMNProcess process, EndEvent endEvent);

  void createIntermediateThrowEventRule(
      AbstractBPMNProcess process, IntermediateThrowEvent intermediateThrowEvent);

  void createIntermediateCatchEventRule(
      AbstractBPMNProcess process, IntermediateCatchEvent intermediateCatchEvent);
}
