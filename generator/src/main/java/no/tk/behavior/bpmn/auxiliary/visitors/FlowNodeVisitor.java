package no.tk.behavior.bpmn.auxiliary.visitors;

import no.tk.behavior.bpmn.gateways.EventBasedGateway;
import no.tk.behavior.bpmn.gateways.ExclusiveGateway;
import no.tk.behavior.bpmn.gateways.InclusiveGateway;
import no.tk.behavior.bpmn.gateways.ParallelGateway;

public interface FlowNodeVisitor extends EventVisitor, ActivityVisitor {
  void handle(ExclusiveGateway exclusiveGateway);

  void handle(ParallelGateway parallelGateway);

  void handle(InclusiveGateway inclusiveGateway);

  void handle(EventBasedGateway eventBasedGateway);
}
