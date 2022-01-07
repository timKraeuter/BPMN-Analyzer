package behavior.bpmn.gateways;

import behavior.bpmn.auxiliary.ControlFlowNodeVisitor;

public class ParallelGateway extends Gateway {
    public ParallelGateway(String name) {
        super(name);
    }

    @Override
    public void accept(ControlFlowNodeVisitor visitor) {
        visitor.handle(this);
    }

}
