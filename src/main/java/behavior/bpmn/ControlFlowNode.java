package behavior.bpmn;

import behavior.bpmn.auxiliary.ControlFlowNodeVisitor;

public abstract class ControlFlowNode {
    private final String name;

    public ControlFlowNode(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public abstract void accept(ControlFlowNodeVisitor visitor);

    public abstract boolean isParallelGateway();
}
