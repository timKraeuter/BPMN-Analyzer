package groove.graph.rule;

import groove.GrooveGxlHelper;
import groove.GxlToXMLConverter;
import groove.graph.GrooveEdge;
import groove.graph.GrooveNode;
import groove.gxl.Graph;
import groove.gxl.Gxl;
import groove.gxl.Node;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GrooveRuleWriter {

    private GrooveRuleWriter() {
        // Helper class
    }

    public static final String ASPECT_LABEL_NEW = "new:";
    public static final String ASPECT_LABEL_DEL = "del:";

    public static void writeRules(Stream<GrooveGraphRule> rules, File dir) {
        rules.forEach(grooveGraphRule -> {
            // Create gxl with a graph for each rule
            Gxl gxl = new Gxl();
            Graph graph = GrooveGxlHelper.createStandardGxlGraph(grooveGraphRule.getRuleName(), gxl);

            Map<String, Node> allGxlNodes = new HashMap<>();
            // Add nodes which should be added to gxl
            grooveGraphRule.getNodesToBeAdded().forEach(toBeAddedNode -> addNodeToGxlGraph(graph, toBeAddedNode, allGxlNodes, NodeRuleAspect.ADD));

            // Add nodes which should be deleted to gxl
            grooveGraphRule.getNodesToBeDeleted().forEach(toBeDeletedNode -> addNodeToGxlGraph(graph, toBeDeletedNode, allGxlNodes, NodeRuleAspect.DEL));

            // Add nodes which should be in context
            grooveGraphRule.getContextNodes().forEach(contextNode -> addNodeToGxlGraph(graph, contextNode, allGxlNodes, NodeRuleAspect.CONTEXT));

            // Add edges which should be added to gxl
            grooveGraphRule.getEdgesToBeAdded().forEach(toBeAddedEdge -> addEdgeToGxlGraph(graph, toBeAddedEdge, allGxlNodes, NodeRuleAspect.ADD));

            // Add edges which should be deleted to gxl
            grooveGraphRule.getEdgesToBeDeleted().forEach(toBeDeletedEdge -> addEdgeToGxlGraph(graph, toBeDeletedEdge, allGxlNodes, NodeRuleAspect.DEL));

            // Add edges which should be deleted to gxl
            grooveGraphRule.getContextEdges().forEach(toBeDeletedEdge -> addEdgeToGxlGraph(graph, toBeDeletedEdge, allGxlNodes, NodeRuleAspect.CONTEXT));

            GrooveGxlHelper.layoutGraph(graph, grooveGraphRule.getAllNodes().entrySet().stream()
                    .collect(Collectors.toMap(Map.Entry::getKey,
                            idNodePair -> idNodePair.getValue().getName())));
            // Write each rule to a file
            writeRuleToFile(dir, grooveGraphRule, gxl);
        });
    }

    private static void addEdgeToGxlGraph(
            Graph graph,
            GrooveEdge grooveEdge,
            Map<String, groove.gxl.Node> createdGxlNodes,
            NodeRuleAspect addDelOrContext) {
        groove.gxl.Node sourceNode = createdGxlNodes.get(grooveEdge.getSourceNode().getId());
        groove.gxl.Node targetNode = createdGxlNodes.get(grooveEdge.getTargetNode().getId());
        assert sourceNode != null;
        assert targetNode != null;

        GrooveGxlHelper.createEdgeWithName(graph, sourceNode, targetNode, getAspectLabel(addDelOrContext) + grooveEdge.getName());
    }

    private static void addNodeToGxlGraph(
            Graph graph,
            GrooveNode grooveNode,
            Map<String, groove.gxl.Node> nodeRepository,
            NodeRuleAspect addDelOrContext) {
        groove.gxl.Node gxlNode = GrooveGxlHelper.createNodeWithName(grooveNode.getId(), grooveNode.getName(), graph);
        // Each flag itself could be deleted, added or just context!
        grooveNode.getFlags().forEach(flag -> GrooveGxlHelper.addFlagToNode(graph, gxlNode, flag));
        nodeRepository.put(gxlNode.getId(), gxlNode);

        // Nodes need get a "new:", "del:" or no label depending on their aspect.
        switch (addDelOrContext) {
            case CONTEXT:
                // No label
                break;
            case ADD:
                GrooveGxlHelper.createEdgeWithName(graph, gxlNode, gxlNode, ASPECT_LABEL_NEW);
                break;
            case DEL:
                GrooveGxlHelper.createEdgeWithName(graph, gxlNode, gxlNode, ASPECT_LABEL_DEL);
                break;
        }
    }

    private static String getAspectLabel(NodeRuleAspect addDelOrContext) {
        switch (addDelOrContext) {
            case ADD:
                return ASPECT_LABEL_NEW;
            case DEL:
                return ASPECT_LABEL_DEL;
            case CONTEXT:
            default:
                return "";
        }
    }

    private static void writeRuleToFile(File dir, GrooveGraphRule grooveGraphRule, Gxl gxl) {
        File file = new File(dir.getPath() + "/" + grooveGraphRule.getRuleName() + ".gpr");
        GxlToXMLConverter.toXml(gxl, file);
    }
}
