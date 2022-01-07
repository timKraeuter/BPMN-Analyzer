package behavior.bpmn.gateways;

import behavior.bpmn.auxiliary.ControlFlowNodeVisitor;

public class InclusiveGateway extends Gateway {
    public InclusiveGateway(String name) {
        super(name);
    }

    @Override
    public void accept(ControlFlowNodeVisitor visitor) {
        visitor.handle(this);
    }

}
