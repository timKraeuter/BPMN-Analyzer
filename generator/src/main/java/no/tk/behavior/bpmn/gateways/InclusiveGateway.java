package no.tk.behavior.bpmn.gateways;

import no.tk.behavior.bpmn.auxiliary.visitors.FlowNodeVisitor;

public class InclusiveGateway extends Gateway {
  public InclusiveGateway(String id, String name) {
    super(id, name);
  }

  @Override
  public void accept(FlowNodeVisitor visitor) {
    visitor.handle(this);
  }

  @Override
  public boolean isInclusiveGateway() {
    return true;
  }

  @Override
  public boolean isInstantiateFlowNode() {
    return false;
  }

  @Override
  public boolean isExclusiveEventBasedGateway() {
    return false;
  }
}
