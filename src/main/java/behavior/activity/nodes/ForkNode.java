package behavior.activity.nodes;

/**
 * Node to define concurrent branches in an activity diagram.
 * The corresponding node to synchronise concurrent branches is the {@link JoinNode}.
 */
public class ForkNode extends ControlNode {

    public ForkNode(String name) {
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
