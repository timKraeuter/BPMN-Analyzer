package groove.behaviortransformer.bpmn.generators;

import behavior.bpmn.AbstractProcess;
import behavior.bpmn.gateways.EventBasedGateway;
import behavior.bpmn.gateways.ExclusiveGateway;
import behavior.bpmn.gateways.InclusiveGateway;
import behavior.bpmn.gateways.ParallelGateway;

public interface BPMNGatewayRuleGenerator {
    void createExclusiveGatewayRules(AbstractProcess process, ExclusiveGateway exclusiveGateway);

    void createParallelGatewayRule(AbstractProcess process, ParallelGateway parallelGateway);

    void createEventBasedGatewayRule(EventBasedGateway eventBasedGateway, AbstractProcess process);

    void createInclusiveGatewayRules(AbstractProcess process, InclusiveGateway inclusiveGateway);
}
