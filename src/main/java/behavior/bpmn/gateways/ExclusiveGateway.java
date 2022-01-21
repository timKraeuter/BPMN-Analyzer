package behavior.bpmn.gateways;

import behavior.bpmn.auxiliary.FlowNodeVisitor;

public class ExclusiveGateway extends Gateway {
    public ExclusiveGateway(String name) {
        super(name);
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
    public boolean isExclusiveEventBasedGateway() {
        return false;
    }

}
