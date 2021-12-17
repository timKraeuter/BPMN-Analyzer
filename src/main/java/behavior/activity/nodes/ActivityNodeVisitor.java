package behavior.activity.nodes;

public interface ActivityNodeVisitor {
    void handle(DecisionNode decisionNode);

    void handle(ForkNode forkNode);

    void handle(InitialNode initialNode);

    void handle(JoinNode joinNode);

    void handle(MergeNode mergeNode);

    void handle(OpaqueAction opaqueAction);

    void handle(ActivityFinalNode activityFinalNode);
}
