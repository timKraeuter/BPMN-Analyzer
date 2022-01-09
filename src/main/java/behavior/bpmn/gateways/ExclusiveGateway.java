package behavior.bpmn.gateways;

import behavior.bpmn.auxiliary.ControlFlowNodeVisitor;

public class ExclusiveGateway extends Gateway {
    public ExclusiveGateway(String name) {
        super(name);
    }

    @Override
    public void accept(ControlFlowNodeVisitor visitor) {
        visitor.handle(this);
    }

    @Override
    public boolean isInclusiveGateway() {
        return false;
    }

}
