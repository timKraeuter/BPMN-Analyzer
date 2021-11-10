package behavior.bpmn;

import behavior.bpmn.auxiliary.ControleFlowNodeVisitor;

public abstract class ControleFlowNode {
    private final String name;

    public ControleFlowNode(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    abstract void accept(ControleFlowNodeVisitor visitor);
}
