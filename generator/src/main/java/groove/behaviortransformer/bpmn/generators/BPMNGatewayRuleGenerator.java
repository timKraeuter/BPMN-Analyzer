package groove.behaviortransformer.bpmn.generators;

import behavior.bpmn.AbstractBPMNProcess;
import behavior.bpmn.gateways.EventBasedGateway;
import behavior.bpmn.gateways.ExclusiveGateway;
import behavior.bpmn.gateways.InclusiveGateway;
import behavior.bpmn.gateways.ParallelGateway;

public interface BPMNGatewayRuleGenerator {
  void createExclusiveGatewayRules(AbstractBPMNProcess process, ExclusiveGateway exclusiveGateway);

  void createParallelGatewayRule(AbstractBPMNProcess process, ParallelGateway parallelGateway);

  void createEventBasedGatewayRule(
      EventBasedGateway eventBasedGateway, AbstractBPMNProcess process);

  void createInclusiveGatewayRules(AbstractBPMNProcess process, InclusiveGateway inclusiveGateway);
}
