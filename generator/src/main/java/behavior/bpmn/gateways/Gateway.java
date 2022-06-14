package behavior.bpmn.gateways;

import behavior.bpmn.FlowNode;

public abstract class Gateway extends FlowNode {
    protected Gateway(String name) {
        super(name);
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
