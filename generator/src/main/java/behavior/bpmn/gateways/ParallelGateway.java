package behavior.bpmn.gateways;

import behavior.bpmn.auxiliary.FlowNodeVisitor;

public class ParallelGateway extends SimpleGateway {
    public ParallelGateway(String id, String name) {
        super(id, name);
    }

    @Override
    public void accept(FlowNodeVisitor visitor) {
        visitor.handle(this);
    }
}
