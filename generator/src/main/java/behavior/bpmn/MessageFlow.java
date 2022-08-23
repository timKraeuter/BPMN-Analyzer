package behavior.bpmn;

public class MessageFlow {
    // TODO: Message flows need ids, similar to sequence flows!
    private final String name;
    private final FlowNode source;
    private final FlowNode target;

    public MessageFlow(String name, FlowNode source, FlowNode target) {
        this.name = name;
        this.source = source;
        this.target = target;
    }

    public String getName() {
        if (name.isEmpty()) {
            return source.getName() + "_" + target.getName();
        }
        return name;
    }

    public FlowNode getSource() {
        return source;
    }

    public FlowNode getTarget() {
        return target;
    }
}
