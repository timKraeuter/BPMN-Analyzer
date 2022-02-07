package behavior.bpmn.gateways;

import behavior.bpmn.FlowNode;

public abstract class Gateway extends FlowNode {
    public Gateway(String name) {
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

    @Override
    public boolean isInstantiateReceiveTask() {
        return false;
    }

    @Override
    public boolean isMessageOrSignalStartEvent() {
        return false;
    }
}
