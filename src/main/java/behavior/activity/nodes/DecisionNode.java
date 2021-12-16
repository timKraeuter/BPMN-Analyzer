package behavior.activity.nodes;

/**
 * Node to define alternative branches in an activity diagram.
 * The corresponding node to merge the created alternative branches is the {@link MergeNode}.
 */
public class DecisionNode extends ControlNode {

    public DecisionNode(String name) {
        super(name);
    }
}
