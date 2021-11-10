package groove;

import api.GraphRuleGenerator;
import api.Node;
import behavior.Aspect;
import groove.gxl.Edge;
import groove.gxl.Graph;
import groove.gxl.Gxl;
import org.eclipse.elk.core.RecursiveGraphLayoutEngine;
import org.eclipse.elk.core.util.BasicProgressMonitor;
import org.eclipse.elk.graph.ElkNode;
import org.eclipse.elk.graph.util.ElkGraphUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class GrooveRuleGenerator implements GraphRuleGenerator {
    public static final String ASPECT_LABEL_NEW = "new:";
    public static final String ASPECT_LABEL_DEL = "del:";
    public static final int XY_SHIFT_GROOVE_LAYOUT = 50;
    private List<GrooveGraphRule> rules = new ArrayList<>();
    private GrooveGraphRule currentRule = null;

    @Override
    public void startRule(String ruleName) {
        this.currentRule = new GrooveGraphRule(ruleName);
    }

    @Override
    public GrooveNode contextNode(String nodeName) {
        assert this.currentRule != null;
        GrooveNode contextNode = new GrooveNode(nodeName);
        this.currentRule.addContextNode(contextNode);
        return contextNode;
    }

    @Override
    public GrooveNode addNode(String nodeName) {
        assert this.currentRule != null;
        GrooveNode newNode = new GrooveNode(nodeName);
        this.currentRule.addNewNode(newNode);
        return newNode;
    }

    @Override
    public void addEdge(String name, Node source, Node target) {
        assert this.currentRule != null;
        Map<String, GrooveNode> contextAndAddedNodes = this.currentRule.getContextAndAddedNodes();
        GrooveNode sourceNode = contextAndAddedNodes.get(source.getId());
        GrooveNode targetNode = contextAndAddedNodes.get(target.getId());

        this.checkNodeContainment(source, target, sourceNode, targetNode);

        this.currentRule.addNewEdge(new GrooveEdge(name, sourceNode, targetNode));
    }

    @Override
    public GrooveNode deleteNode(String nodeName) {
        assert this.currentRule != null;
        GrooveNode deleteNode = new GrooveNode(nodeName);
        this.currentRule.addDelNode(deleteNode);
        return deleteNode;
    }

    @Override
    public void deleteEdge(String name, Node source, Node target) {
        assert this.currentRule != null;
        Map<String, GrooveNode> nodes = this.currentRule.getAllNodes();

        GrooveNode sourceNode = nodes.get(source.getId());
        GrooveNode targetNode = nodes.get(target.getId());

        this.checkNodeContainment(source, target, sourceNode, targetNode);

        this.currentRule.addDelEdge(new GrooveEdge(name, sourceNode, targetNode));
    }

    private void checkNodeContainment(Node source, Node target, GrooveNode sourceNode, GrooveNode targetNode) {
        if (sourceNode == null) {
            throw new RuntimeException(String.format("Source node %s not contained in the rule!", source));
        }
        if (targetNode == null) {
            throw new RuntimeException(String.format("Target node %s not contained in the rule!", target));
        }
    }

    @Override
    public void generateRule() {
        this.rules.add(this.currentRule);
        this.currentRule = null;
    }

    public void writeRules(File dir) {
        this.rules.forEach(grooveGraphRule -> {
            // Create gxl with a graph for each rule
            Gxl gxl = new Gxl();
            Graph graph = GrooveGxlHelper.createStandardGxlGraph(grooveGraphRule.getRuleName(), gxl);

            Map<String, groove.gxl.Node> allGxlNodes = new HashMap<>();
            // Add nodes which should be added to gxl
            grooveGraphRule.getNodesToBeAdded().forEach(toBeAddedNode -> this.addNodeToGxlGraph(graph, toBeAddedNode, allGxlNodes, Aspect.ADD));

            // Add nodes which should be deleted to gxl
            grooveGraphRule.getNodesToBeDeleted().forEach(toBeDeletedNode -> this.addNodeToGxlGraph(graph, toBeDeletedNode, allGxlNodes, Aspect.DEL));

            // Add nodes which should be in context
            grooveGraphRule.getContextNodes().forEach(contextNode -> this.addNodeToGxlGraph(graph, contextNode, allGxlNodes, Aspect.CONTEXT));

            // Add edges which should be added to gxl
            grooveGraphRule.getEdgesToBeAdded().forEach(toBeAddedEdge -> this.addEdgeToGxlGraph(graph, toBeAddedEdge, allGxlNodes, Aspect.ADD));

            // Add edges which should be deleted to gxl
            grooveGraphRule.getEdgesToBeDeleted().forEach(toBeDeletedEdge -> this.addEdgeToGxlGraph(graph, toBeDeletedEdge, allGxlNodes, Aspect.DEL));

            // TODO: context edges!

            this.layoutGraph(graph);
            // Write each rule to a file
            this.writeRuleToFile(dir, grooveGraphRule, gxl);
        });
    }

    private void layoutGraph(Graph graph) {
        // Need from to of edges for the nodes created earlier.
        // Build a map of nodes with id or something.
        graph.getNodeOrEdgeOrRel().forEach(nodeOrEdge -> {
            if (nodeOrEdge instanceof groove.gxl.Node) {
                System.out.println("Node " + nodeOrEdge);
            }
            if (nodeOrEdge instanceof Edge) {
                Edge edge = (Edge) nodeOrEdge;
                System.out.println("Edge " + edge);
            }
        });
//        GrooveGxlHelper.addLayoutToNode(gxlNode, layoutNode.getX() + XY_SHIFT_GROOVE_LAYOUT, layoutNode.getY() + XY_SHIFT_GROOVE_LAYOUT);
    }

    private Map<String, ElkNode> createLayouting(GrooveGraphRule grooveGraphRule) {
        ElkNode graph = ElkGraphUtil.createGraph();
        Map<String, ElkNode> layoutNodes = grooveGraphRule.getAllNodes().entrySet().stream()
                                                          .collect(Collectors.toMap(Map.Entry::getKey, o -> {
                                                              ElkNode node = ElkGraphUtil.createNode(graph);
                                                              // Layouting of groove is complicated. But these magic numbers work ok for now.
                                                              node.setHeight(50);
                                                              node.setWidth(o.getKey().length() * 60);
                                                              return node;
                                                          }));
        grooveGraphRule.getAllEdges()
                       .forEach((s, grooveEdge) -> ElkGraphUtil.createSimpleEdge(
                               layoutNodes.get(grooveEdge.getSourceNode().getId()),
                               layoutNodes.get(grooveEdge.getTargetNode().getId())));

        RecursiveGraphLayoutEngine recursiveGraphLayoutEngine = new RecursiveGraphLayoutEngine();
        recursiveGraphLayoutEngine.layout(graph, new BasicProgressMonitor());
        return layoutNodes;
    }

    private void addEdgeToGxlGraph(
            Graph graph,
            GrooveEdge grooveEdge,
            Map<String, groove.gxl.Node> createdGxlNodes,
            Aspect addDelOrContext) {
        groove.gxl.Node sourceNode = createdGxlNodes.get(grooveEdge.getSourceNode().getId());
        groove.gxl.Node targetNode = createdGxlNodes.get(grooveEdge.getTargetNode().getId());
        assert sourceNode != null;
        assert targetNode != null;

        GrooveGxlHelper.createEdgeWithName(graph, sourceNode, targetNode, this.getAspectLabel(addDelOrContext) + grooveEdge.getName());
    }

    private void addNodeToGxlGraph(
            Graph graph,
            GrooveNode grooveNode,
            Map<String, groove.gxl.Node> nodeRepository,
            Aspect addDelOrContext) {
        groove.gxl.Node gxlNode = GrooveGxlHelper.createNodeWithName(grooveNode.getId(), grooveNode.getName(), graph);
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

    private String getAspectLabel(Aspect addDelOrContext) {
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

    private void writeRuleToFile(File dir, GrooveGraphRule grooveGraphRule, Gxl gxl) {
        File file = new File(dir.getPath() + "/" + grooveGraphRule.getRuleName() + ".gpr");
        GxlToXMLConverter.toXml(gxl, file);
    }
}
