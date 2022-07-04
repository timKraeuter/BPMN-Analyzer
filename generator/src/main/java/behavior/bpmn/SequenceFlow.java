package behavior.bpmn;

public class SequenceFlow extends FlowElement {
    private final FlowNode source;
    private final FlowNode target;

    public SequenceFlow(String id, String name, FlowNode source, FlowNode target) {
        super(id, name);
        this.source = source;
        this.target = target;
    }
    public String getDescriptiveID() {
        // We assume names are unique if not empty
        // We assume there only exists one sequence flow between two nodes for now.
        if (getName().isEmpty()) {
            return String.format("%s_%s", source.getName(), target.getName());
        }
        return getName();
    }

    public FlowNode getSource() {
        return source;
    }

    public FlowNode getTarget() {
        return target;
    }
}
