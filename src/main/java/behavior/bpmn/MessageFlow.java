package behavior.bpmn;

public class MessageFlow {
    private final String name;
    private final FlowNode source;
    private final FlowNode target;

    public MessageFlow(String name, FlowNode source, FlowNode target) {
        this.name = name;
        this.source = source;
        this.target = target;
    }

    public String getName() {
        return name;
    }

    public FlowNode getSource() {
        return source;
    }

    public FlowNode getTarget() {
        return target;
    }
}
