package behavior.bpmn.gateways;

import behavior.bpmn.auxiliary.FlowNodeVisitor;

public class ExclusiveGateway extends Gateway {
    public ExclusiveGateway(String id, String name) {
        super(id, name);
    }

    @Override
    public void accept(FlowNodeVisitor visitor) {
        visitor.handle(this);
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
