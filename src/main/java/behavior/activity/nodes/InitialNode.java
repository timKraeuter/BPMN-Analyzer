package behavior.activity.nodes;

/**
 * Represent the starting point for the execution of an activity diagram.
 * Only one initial node is allowed in the diagram.
 */
public class InitialNode extends ControlNode {
    public InitialNode(String name) {
        super(name);
    }

    @Override
    public void accept(ActivityNodeVisitor visitor) {
        visitor.handle(this);
    }

    @Override
    public boolean isDecisionNode() {
        return false;
    }
}
