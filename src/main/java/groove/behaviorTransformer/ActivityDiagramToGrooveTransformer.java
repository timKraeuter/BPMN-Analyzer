package groove.behaviorTransformer;

import behavior.activity.ActivityDiagram;
import behavior.activity.nodes.*;
import groove.graph.GrooveGraph;
import groove.graph.GrooveGraphBuilder;
import groove.graph.GrooveNode;
import groove.graph.rule.GrooveGraphRule;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Stream;

public class ActivityDiagramToGrooveTransformer implements GrooveTransformer<ActivityDiagram> {


    // Possible node labels.
    private static final String TYPE_INITIAL_NODE = TYPE + "InitialNode";
    private static final String TYPE_ACTIVITY_DIAGRAM = TYPE + "ActivityDiagram";
    private static final String TYPE_CONTROL_FLOW = TYPE + "ControlFlow";
    private static final String TYPE_OPAQUE_ACTION = TYPE + "OpaqueAction";
    private static final String TYPE_FORK_NODE = TYPE + "ForkNode";
    private static final String TYPE_JOIN_NODE = TYPE + "JoinNode";
    private static final String TYPE_DECISION_NODE = TYPE + "DecisionNode";
    private static final String TYPE_MERGE_NODE = TYPE + "MergeNode";
    private static final String TYPE_FINAL_NODE = TYPE + "FinalNode";
    public static final String SOURCE = "source";
    public static final String TARGET = "target";

    @Override
    public GrooveGraph generateStartGraph(ActivityDiagram activityDiagram, boolean addPrefix) {
        GrooveGraphBuilder builder = new GrooveGraphBuilder().setName(activityDiagram.getName());

        Map<ActivityNode, GrooveNode> createdNodesIndex = new LinkedHashMap<>();

        // Create initial node in groove
        InitialNode initialNode = activityDiagram.getInitialNode();
        GrooveNode initialNodeGroove = new GrooveNode(TYPE_INITIAL_NODE);
        builder.addNode(initialNodeGroove);
        createdNodesIndex.put(initialNode, initialNodeGroove);

        GrooveNode activityDiagramNode = new GrooveNode(TYPE_ACTIVITY_DIAGRAM);
        activityDiagramNode.addAttribute("running", false);
        builder.addNode(activityDiagramNode);
        builder.addEdge("start", activityDiagramNode, initialNodeGroove);

        // Create activity nodes in groove
        activityDiagram.getNodes().forEach(activityNode -> activityNode.accept(new ActivityNodeVisitor() {
            @Override
            public void handle(DecisionNode decisionNode) {
                GrooveNode decisionNodeGroove = new GrooveNode(TYPE_DECISION_NODE);
                builder.addNode(decisionNodeGroove);
                createdNodesIndex.put(decisionNode, decisionNodeGroove);
            }

            @Override
            public void handle(ForkNode forkNode) {
                GrooveNode forkNodeGroove = new GrooveNode(TYPE_FORK_NODE);
                builder.addNode(forkNodeGroove);
                createdNodesIndex.put(forkNode, forkNodeGroove);
            }

            @Override
            public void handle(InitialNode initialNode) {
                // Is ignored because it was already created earlier!
            }

            @Override
            public void handle(JoinNode joinNode) {
                GrooveNode joinNodeGroove = new GrooveNode(TYPE_JOIN_NODE);
                builder.addNode(joinNodeGroove);
                createdNodesIndex.put(joinNode, joinNodeGroove);
            }

            @Override
            public void handle(MergeNode mergeNode) {
                GrooveNode mergeNodeGroove = new GrooveNode(TYPE_MERGE_NODE);
                builder.addNode(mergeNodeGroove);
                createdNodesIndex.put(mergeNode, mergeNodeGroove);
            }

            @Override
            public void handle(OpaqueAction opaqueAction) {
                GrooveNode opaqueActionGroove = new GrooveNode(TYPE_OPAQUE_ACTION);
                opaqueActionGroove.addAttribute("name", opaqueAction.getName());
                builder.addNode(opaqueActionGroove);
                createdNodesIndex.put(opaqueAction, opaqueActionGroove);
                // TODO: Add expressions!
            }

            @Override
            public void handle(ActivityFinalNode activityFinalNode) {
                GrooveNode activityFinalNodeGroove = new GrooveNode(TYPE_FINAL_NODE);
                builder.addNode(activityFinalNodeGroove);
                createdNodesIndex.put(activityFinalNode, activityFinalNodeGroove);
            }
        }));
        // Create control flows in groove
        activityDiagram.getEdges().forEach(
                edge -> {
                    GrooveNode controlFlowNode = new GrooveNode(TYPE_CONTROL_FLOW);
                    builder.addNode(controlFlowNode);
                    builder.addEdge(SOURCE, controlFlowNode, createdNodesIndex.get(edge.getSource()));
                    builder.addEdge(TARGET, controlFlowNode, createdNodesIndex.get(edge.getTarget()));
                });

        return builder.build();
    }

    @Override
    public Stream<GrooveGraphRule> generateRules(ActivityDiagram activityDiagram, boolean addPrefix) {
        // TODO: implement!
        return Stream.of();
    }
}
