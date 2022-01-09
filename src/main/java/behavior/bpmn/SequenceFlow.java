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

    public String getID() {
        // We assume names are unique if not empty
        // We assume there only exists one sequence flow between two nodes for now.
        if (name.isEmpty()) {
            return String.format("%s_%s", source.getName(), target.getName());
        }
        return name;
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
