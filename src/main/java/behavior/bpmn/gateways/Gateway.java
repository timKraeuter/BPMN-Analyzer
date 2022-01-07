package behavior.bpmn.gateways;

import behavior.bpmn.ControlFlowNode;

public abstract class Gateway extends ControlFlowNode {
    public Gateway(String name) {
        super(name);
    }
}
