package groove.graph;

import api.GraphRuleGenerator;
import api.Node;
import behavior.Behavior;
import groove.GrooveGxlHelper;
import groove.GxlToXMLConverter;
import groove.gxl.Graph;
import groove.gxl.Gxl;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class GrooveRuleGenerator implements GraphRuleGenerator {
    public static final String ASPECT_LABEL_NEW = "new:";
    public static final String ASPECT_LABEL_DEL = "del:";
    private final List<GrooveGraphRule> rules = new ArrayList<>();
    private GrooveGraphRule currentRule = null;
    private String prefix;

    public GrooveRuleGenerator() {
        this.prefix = "";
    }

    public GrooveRuleGenerator(Behavior behavior, boolean addPrefix) {
        if (addPrefix) {
            this.prefix = behavior.getName() + "_";
        } else {
            this.prefix = "";
        }
    }

    @Override
    public void startRule(String ruleName) {
        this.currentRule = new GrooveGraphRule(this.addPrefix(ruleName));
    }

    @Override
    public GrooveNode contextNode(String nodeName) {
        String prefixedNodeName = this.addPrefix(nodeName);

        assert this.currentRule != null;
        GrooveNode contextNode = new GrooveNode(prefixedNodeName);
        this.currentRule.addContextNode(contextNode);
        return contextNode;
    }

    private String addPrefix(String name) {
        return this.prefix + name;
    }

    @Override
    public GrooveNode addNode(String nodeName) {
        assert this.currentRule != null;

        String prefixedNodeName = this.addPrefix(nodeName);

        GrooveNode newNode = new GrooveNode(prefixedNodeName);
        this.currentRule.addNewNode(newNode);
        return newNode;
    }

    @Override
    public void addEdge(String edgeName, Node source, Node target) {

        assert this.currentRule != null;
        Map<String, GrooveNode> contextAndAddedNodes = this.currentRule.getContextAndAddedNodes();
        GrooveNode sourceNode = contextAndAddedNodes.get(source.getId());
        GrooveNode targetNode = contextAndAddedNodes.get(target.getId());

        this.checkNodeContainment(source, target, sourceNode, targetNode);

        String prefixedEdgeName = this.addPrefix(edgeName);
        this.currentRule.addNewEdge(new GrooveEdge(prefixedEdgeName, sourceNode, targetNode));
    }

    @Override
    public GrooveNode deleteNode(String nodeName) {
        assert this.currentRule != null;

        String prefixedNodeName = this.addPrefix(nodeName);
        GrooveNode deleteNode = new GrooveNode(prefixedNodeName);
        this.currentRule.addDelNode(deleteNode);
        return deleteNode;
    }

    @Override
    public void deleteEdge(String edgeName, Node source, Node target) {
        assert this.currentRule != null;
        Map<String, GrooveNode> nodes = this.currentRule.getAllNodes();

        GrooveNode sourceNode = nodes.get(source.getId());
        GrooveNode targetNode = nodes.get(target.getId());

        this.checkNodeContainment(source, target, sourceNode, targetNode);

        String prefixedEdgeName = this.addPrefix(edgeName);
        this.currentRule.addDelEdge(new GrooveEdge(prefixedEdgeName, sourceNode, targetNode));
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
            grooveGraphRule.getNodesToBeAdded().forEach(toBeAddedNode -> this.addNodeToGxlGraph(graph, toBeAddedNode, allGxlNodes, NodeRuleAspect.ADD));

            // Add nodes which should be deleted to gxl
            grooveGraphRule.getNodesToBeDeleted().forEach(toBeDeletedNode -> this.addNodeToGxlGraph(graph, toBeDeletedNode, allGxlNodes, NodeRuleAspect.DEL));

            // Add nodes which should be in context
            grooveGraphRule.getContextNodes().forEach(contextNode -> this.addNodeToGxlGraph(graph, contextNode, allGxlNodes, NodeRuleAspect.CONTEXT));

            // Add edges which should be added to gxl
            grooveGraphRule.getEdgesToBeAdded().forEach(toBeAddedEdge -> this.addEdgeToGxlGraph(graph, toBeAddedEdge, allGxlNodes, NodeRuleAspect.ADD));

            // Add edges which should be deleted to gxl
            grooveGraphRule.getEdgesToBeDeleted().forEach(toBeDeletedEdge -> this.addEdgeToGxlGraph(graph, toBeDeletedEdge, allGxlNodes, NodeRuleAspect.DEL));

            // TODO: context edges!

            GrooveGxlHelper.layoutGraph(graph, grooveGraphRule.getAllNodes().entrySet().stream()
                                                              .collect(Collectors.toMap(Map.Entry::getKey,
                                                                      idNodePair -> idNodePair.getValue().getName())));
            // Write each rule to a file
            this.writeRuleToFile(dir, grooveGraphRule, gxl);
        });
    }

    private void addEdgeToGxlGraph(
            Graph graph,
            GrooveEdge grooveEdge,
            Map<String, groove.gxl.Node> createdGxlNodes,
            NodeRuleAspect addDelOrContext) {
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
            NodeRuleAspect addDelOrContext) {
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

    private String getAspectLabel(NodeRuleAspect addDelOrContext) {
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
