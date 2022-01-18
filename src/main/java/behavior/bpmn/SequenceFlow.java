package behavior.bpmn;

public class SequenceFlow {
    private final String name;
    private final FlowNode source;
    private final FlowNode target;

    public SequenceFlow(String name, FlowNode source, FlowNode target) {
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

    public FlowNode getSource() {
        return source;
    }

    public FlowNode getTarget() {
        return target;
    }

    public String getName() {
        return name;
    }
}
