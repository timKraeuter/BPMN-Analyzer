package behavior.bpmn.gateways;

import behavior.bpmn.auxiliary.visitors.FlowNodeVisitor;

public class ExclusiveGateway extends SimpleGateway {
  public ExclusiveGateway(String id, String name) {
    super(id, name);
  }

  @Override
  public void accept(FlowNodeVisitor visitor) {
    visitor.handle(this);
  }
}
