package behavior.activity.nodes;

/** Node to merge alternative branches in an activity diagram created by a {@link DecisionNode}. */
public class MergeNode extends ControlNode {

  public MergeNode(String name) {
    super(name);
  }

  @Override
  public void accept(ActivityNodeVisitor visitor) {
    visitor.handle(this);
  }
}
