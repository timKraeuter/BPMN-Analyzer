package groove;

import api.Edge;
import api.GraphRuleGenerator;
import api.Node;
import groove.gxl.Attr;
import groove.gxl.Graph;
import groove.gxl.Gxl;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GrooveRuleGenerator implements GraphRuleGenerator {
    private List<GrooveGraphRule> rules = new ArrayList<>();
    private GrooveGraphRule currentRule = null;

    @Override
    public void deleteNode(Node node) {

    }

    @Override
    public GrooveNode deleteNode(String nodeName) {
        assert this.currentRule != null;
        GrooveNode deleteNode = new GrooveNode(nodeName);
        this.currentRule.addDelNode(deleteNode);
        return deleteNode;
    }

    @Override
    public void addNode(Node node) {

    }

    @Override
    public GrooveNode addNode(String nodeName) {
        assert this.currentRule != null;
        GrooveNode newNode = new GrooveNode(nodeName);
        this.currentRule.addNewNode(newNode);
        return newNode;
    }

    @Override
    public void addEdge(Edge edge) {

    }

    @Override
    public void addEdge(String name, Node source, Node target) {
        assert this.currentRule != null;
        Map<String, GrooveNode> contextAndAddedNodes = this.currentRule.getContextAndAddedNodes();
        GrooveNode sourceNode = contextAndAddedNodes.get(source.getId());
        if (sourceNode == null) {
            throw new RuntimeException(String.format("Source node %s not contained in the rule!", source));
        }
        GrooveNode targetNode = contextAndAddedNodes.get(target.getId());
        if (targetNode == null) {
            throw new RuntimeException(String.format("Target node %s not contained in the rule!", target));
        }
        this.currentRule.addNewEdge(new GrooveEdge(name, sourceNode, targetNode));
    }

    @Override
    public void deleteEdge(Edge edge) {

    }

    @Override
    public void startRule(String ruleName) {
        this.currentRule = new GrooveGraphRule(ruleName);
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
            Graph graph = GxlHelper.createStandardGxlGraph(grooveGraphRule.getRuleName(), gxl);

            Map<String, groove.gxl.Node> createdGxlNodes = new HashMap<>();
            // Add nodes which should be added to gxl
            grooveGraphRule.getNodesToBeAdded().forEach(toBeAddedNode -> this.addNodeToGxlGraph(graph, toBeAddedNode, createdGxlNodes, "new:"));
            // Add edges which should be added to gxl
            grooveGraphRule.getEdgesToBeAdded().forEach(toBeAddedEdge -> this.addEdgeToGxlGraph(graph, toBeAddedEdge, createdGxlNodes));
            // Add nodes which should be deleted to gxl
            grooveGraphRule.getNodesToBeDeleted().forEach(toBeDeletedNode -> this.addNodeToGxlGraph(graph, toBeDeletedNode, new HashMap<>(), "del:"));

            // Write each rule to a file
            this.writeRuleToFile(dir, grooveGraphRule, gxl);
        });
    }

    private void addEdgeToGxlGraph(Graph graph, GrooveEdge grooveEdge, Map<String, groove.gxl.Node> createdGxlNodes) {
        groove.gxl.Node sourceNode = createdGxlNodes.get(grooveEdge.getSourceNode().getId());
        groove.gxl.Node targetNode = createdGxlNodes.get(grooveEdge.getTargetNode().getId());
        assert sourceNode != null;
        assert targetNode != null;

        groove.gxl.Edge gxledge = new groove.gxl.Edge();
        gxledge.setFrom(sourceNode);
        gxledge.setTo(targetNode);

        Attr nameAttr = GxlHelper.createLabelAttribute(grooveEdge.getName());
        gxledge.getAttr().add(nameAttr);

        graph.getNodeOrEdgeOrRel().add(gxledge);
    }


    private void addNodeToGxlGraph(
            Graph graph,
            GrooveNode grooveNode,
            Map<String, groove.gxl.Node> nodeRepository,
            String labelValue) {
        groove.gxl.Node gxlNode = GxlHelper.createNodeWithName(grooveNode.getId(), grooveNode.getName(), graph);
        nodeRepository.put(gxlNode.getId(), gxlNode);

        // Nodes need get a "new:" or "del:" label.
        groove.gxl.Edge newIdentifierEdge = new groove.gxl.Edge();
        newIdentifierEdge.setFrom(gxlNode);
        newIdentifierEdge.setTo(gxlNode);
        Attr newAttr = GxlHelper.createLabelAttribute(labelValue);
        newIdentifierEdge.getAttr().add(newAttr);

        graph.getNodeOrEdgeOrRel().add(newIdentifierEdge);
    }

    private void writeRuleToFile(File dir, GrooveGraphRule grooveGraphRule, Gxl gxl) {
        File file = new File(dir.getPath() + "/" + grooveGraphRule.getRuleName() + ".gpr");
        GxlToXMLConverter.toXml(gxl, file);
    }
}
