package no.tk.groove.behaviortransformer.bpmn.generators;

import no.tk.behavior.bpmn.AbstractBPMNProcess;
import no.tk.behavior.bpmn.gateways.EventBasedGateway;
import no.tk.behavior.bpmn.gateways.ExclusiveGateway;
import no.tk.behavior.bpmn.gateways.InclusiveGateway;
import no.tk.behavior.bpmn.gateways.ParallelGateway;

public interface BPMNGatewayRuleGenerator {
  void createExclusiveGatewayRules(AbstractBPMNProcess process, ExclusiveGateway exclusiveGateway);

  void createParallelGatewayRule(AbstractBPMNProcess process, ParallelGateway parallelGateway);

  void createEventBasedGatewayRule(
          EventBasedGateway eventBasedGateway, AbstractBPMNProcess process);

  void createInclusiveGatewayRules(AbstractBPMNProcess process, InclusiveGateway inclusiveGateway);
}
