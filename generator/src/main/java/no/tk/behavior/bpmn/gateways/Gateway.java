package no.tk.behavior.bpmn.gateways;

import no.tk.behavior.bpmn.FlowNode;

public abstract class Gateway extends FlowNode {
  protected Gateway(String id, String name) {
    super(id, name);
  }

  @Override
  public boolean isGateway() {
    return true;
  }

  @Override
  public boolean isTask() {
    return false;
  }
}
