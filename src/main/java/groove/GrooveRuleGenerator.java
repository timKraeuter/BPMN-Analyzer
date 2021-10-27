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
    public void deleteNode(String nodeName) {

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
    public void newRule(String ruleName) {
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
            Graph graph = new Graph();
            gxl.getGraph().add(graph);
            graph.setRole("rule");
            graph.setEdgeids("false");
            graph.setEdgemode("directed");
            graph.setId(grooveGraphRule.getRuleName());

            Map<String, groove.gxl.Node> createdGxlNodes = new HashMap<>();
            // Add nodes which should be added to gxl
            grooveGraphRule.getNodesToBeAdded().forEach(grooveNode -> this.addNodeToGxlGraph(graph, grooveNode, createdGxlNodes));
            grooveGraphRule.getEdgesToBeAdded().forEach(grooveEdge -> {
                groove.gxl.Node sourceNode = createdGxlNodes.get(grooveEdge.getSourceNode().getId());
                groove.gxl.Node targetNode = createdGxlNodes.get(grooveEdge.getTargetNode().getId());
                assert sourceNode != null;
                assert targetNode != null;

                groove.gxl.Edge gxledge = new groove.gxl.Edge();
                gxledge.setFrom(sourceNode);
                gxledge.setTo(targetNode);

                Attr nameAttr = new Attr();
                groove.gxl.String name = new groove.gxl.String();
                name.setvalue(grooveEdge.getName());
                nameAttr.getLocatorOrBoolOrIntOrFloatOrStringOrEnumOrSeqOrSetOrBagOrTup().add(name);
                nameAttr.setName("label");
                gxledge.getAttr().add(nameAttr);

                graph.getNodeOrEdgeOrRel().add(gxledge);
            });

            // Write each rule to a file
            this.writeRuleToFile(dir, grooveGraphRule, gxl);
        });
    }

    private void addNodeToGxlGraph(
            Graph graph,
            GrooveNode grooveNode,
            Map<String, groove.gxl.Node> createdGxlNodes) {
        groove.gxl.Node gxlNode = new groove.gxl.Node();
        gxlNode.setId(grooveNode.getId());
        createdGxlNodes.put(gxlNode.getId(), gxlNode);

        // Node name becomes a label edge
        groove.gxl.Edge nameEdge = new groove.gxl.Edge();
        nameEdge.setFrom(gxlNode);
        nameEdge.setTo(gxlNode);
        Attr nameAttr = new Attr();
        groove.gxl.String name = new groove.gxl.String();
        name.setvalue(grooveNode.getName());
        nameAttr.getLocatorOrBoolOrIntOrFloatOrStringOrEnumOrSeqOrSetOrBagOrTup().add(name);
        nameAttr.setName("label");
        nameEdge.getAttr().add(nameAttr);

        // New nodes need the new label
        groove.gxl.Edge newIdentifierEdge = new groove.gxl.Edge();
        newIdentifierEdge.setFrom(gxlNode);
        newIdentifierEdge.setTo(gxlNode);
        Attr newAttr = new Attr();
        groove.gxl.String newTag = new groove.gxl.String();
        newTag.setvalue("new:");
        newAttr.getLocatorOrBoolOrIntOrFloatOrStringOrEnumOrSeqOrSetOrBagOrTup().add(newTag);
        newAttr.setName("label");
        newIdentifierEdge.getAttr().add(newAttr);

        graph.getNodeOrEdgeOrRel().add(gxlNode);
        graph.getNodeOrEdgeOrRel().add(newIdentifierEdge);
        graph.getNodeOrEdgeOrRel().add(nameEdge);
    }

    private void writeRuleToFile(File dir, GrooveGraphRule grooveGraphRule, Gxl gxl) {
        File file = new File(dir.getPath() + "/" + grooveGraphRule.getRuleName() + ".gpr");
        GxlToXMLConverter.toXml(gxl, file);
    }
}
