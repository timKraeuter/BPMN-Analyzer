package behavior.activity.nodes;

/**
 * Node to synchronise concurrent branches in an activity diagram.
 * The corresponding node to define concurrent branches is the {@link ForkNode}.
 */
public class JoinNode extends ControlNode {

    public JoinNode(String name) {
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
