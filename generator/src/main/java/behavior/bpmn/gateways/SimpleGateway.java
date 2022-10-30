package behavior.bpmn.gateways;

public abstract class SimpleGateway extends Gateway {
  protected SimpleGateway(String id, String name) {
    super(id, name);
  }

  @Override
  public boolean isInclusiveGateway() {
    return false;
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
