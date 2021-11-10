package groove;

import groove.gxl.Attr;
import groove.gxl.Graph;
import groove.gxl.Gxl;
import groove.gxl.Node;

public class GrooveGxlHelper {
    public static Graph createStandardGxlGraph(String id, Gxl gxl) {
        Graph graph = new Graph();
        gxl.getGraph().add(graph);
        graph.setRole("rule");
        graph.setEdgeids("false");
        graph.setEdgemode("directed");
        graph.setId(id);
        return graph;
    }

    public static Node createNodeWithName(String nodeId, String nodeName, Graph graph) {
        Node gxlNode = new groove.gxl.Node();
        gxlNode.setId(nodeId);

        groove.gxl.Edge nameEdge = new groove.gxl.Edge();
        nameEdge.setFrom(gxlNode);
        nameEdge.setTo(gxlNode);
        Attr nameAttr = createLabelAttribute(nodeName);
        nameEdge.getAttr().add(nameAttr);

        graph.getNodeOrEdgeOrRel().add(gxlNode);
        graph.getNodeOrEdgeOrRel().add(nameEdge);

        return gxlNode;
    }

    public static void createEdgeWithName(
            Graph graph,
            groove.gxl.Node sourceNode,
            groove.gxl.Node targetNode,
            String name) {
        groove.gxl.Edge gxledge = new groove.gxl.Edge();
        gxledge.setFrom(sourceNode);
        gxledge.setTo(targetNode);

        Attr nameAttr = GrooveGxlHelper.createLabelAttribute(name);
        gxledge.getAttr().add(nameAttr);

        graph.getNodeOrEdgeOrRel().add(gxledge);
    }

    public static Attr createLabelAttribute(String value) {
        String attrName = "label";
        Attr nameAttr = createAttribute(attrName, value);
        return nameAttr;
    }

    private static Attr createAttribute(String attrName, String attrValue) {
        Attr nameAttr = new Attr();
        groove.gxl.String name = new groove.gxl.String();
        name.setvalue(attrValue);
        nameAttr.getLocatorOrBoolOrIntOrFloatOrStringOrEnumOrSeqOrSetOrBagOrTup().add(name);
        nameAttr.setName(attrName);
        return nameAttr;
    }

    public static void addLayoutToNode(Node gxlNode, double x, double y) {
        Attr layoutAttr = createAttribute("layout", String.format("%.0f %.0f 0 0", x, y));
        gxlNode.getAttr().add(layoutAttr);
    }
}
