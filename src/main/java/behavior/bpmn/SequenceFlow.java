package behavior.bpmn;

public class SequenceFlow {
    private final String name;
    private final ControlFlowNode source;
    private final ControlFlowNode target;

    public SequenceFlow(String name, ControlFlowNode source, ControlFlowNode target) {
        this.name = name;
        this.source = source;
        this.target = target;
    }

    public ControlFlowNode getSource() {
        return source;
    }

    public ControlFlowNode getTarget() {
        return target;
    }

    public String getName() {
        return name;
    }
}
