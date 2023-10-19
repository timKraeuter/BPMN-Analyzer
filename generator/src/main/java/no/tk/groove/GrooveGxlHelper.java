package no.tk.groove;

import java.util.HashMap;
import java.util.Map;
import no.tk.groove.gxl.Attr;
import no.tk.groove.gxl.Edge;
import no.tk.groove.gxl.Graph;
import no.tk.groove.gxl.Gxl;
import no.tk.groove.gxl.Node;
import org.eclipse.elk.alg.layered.options.LayeredMetaDataProvider;
import org.eclipse.elk.core.RecursiveGraphLayoutEngine;
import org.eclipse.elk.core.data.LayoutMetaDataService;
import org.eclipse.elk.core.util.BasicProgressMonitor;
import org.eclipse.elk.graph.ElkNode;
import org.eclipse.elk.graph.util.ElkGraphUtil;

public class GrooveGxlHelper {
  private static final int XY_SHIFT_GROOVE_LAYOUT = 50;
  private static final String LABEL = "label";
  private static final String FLAG = "flag:";

  static {
    // Needed when using ELK 0.8.1
    LayoutMetaDataService.getInstance()
        .registerLayoutMetaDataProviders(new LayeredMetaDataProvider());
  }

  private GrooveGxlHelper() {
    // Helper methods.
  }

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
    Node gxlNode = new Node();
    gxlNode.setId(nodeId);

    Edge nameEdge = new Edge();
    nameEdge.setFrom(gxlNode);
    nameEdge.setTo(gxlNode);
    Attr nameAttr = createLabelAttribute(nodeName);
    nameEdge.getAttr().add(nameAttr);

    graph.getNodeOrEdgeOrRel().add(gxlNode);
    graph.getNodeOrEdgeOrRel().add(nameEdge);

    return gxlNode;
  }

  public static void createEdgeWithName(
      Graph graph, Node sourceNode, Node targetNode, String name) {
    Edge gxledge = new Edge();
    gxledge.setFrom(sourceNode);
    gxledge.setTo(targetNode);

    Attr nameAttr = GrooveGxlHelper.createLabelAttribute(name);
    gxledge.getAttr().add(nameAttr);

    graph.getNodeOrEdgeOrRel().add(gxledge);
  }

  public static void addFlagToNode(Graph graph, Node node, String flagValue) {
    Edge gxledge = new Edge();
    gxledge.setFrom(node);
    gxledge.setTo(node);

    Attr flagAttr = createLabelAttribute(FLAG + flagValue);
    gxledge.getAttr().add(flagAttr);

    graph.getNodeOrEdgeOrRel().add(gxledge);
  }

  public static Attr createLabelAttribute(String value) {
    return createAttribute(LABEL, value);
  }

  private static Attr createAttribute(String attrName, String attrValue) {
    Attr nameAttr = new Attr();
    no.tk.groove.gxl.String name = new no.tk.groove.gxl.String();
    name.setvalue(attrValue);
    nameAttr.getLocatorOrBoolOrIntOrFloatOrStringOrEnumOrSeqOrSetOrBagOrTup().add(name);
    nameAttr.setName(attrName);
    return nameAttr;
  }

  public static void layoutGraph(Graph graph, Map<String, String> nodeLabels) {
    Map<String, ElkNode> layoutNodes = new HashMap<>();
    ElkNode layoutGraph = createElkGraph(graph, nodeLabels, layoutNodes);

    RecursiveGraphLayoutEngine recursiveGraphLayoutEngine = new RecursiveGraphLayoutEngine();
    recursiveGraphLayoutEngine.layout(layoutGraph, new BasicProgressMonitor());

    // Add layout info to gxl
    graph
        .getNodeOrEdgeOrRel()
        .forEach(
            nodeOrEdge -> {
              if (nodeOrEdge instanceof Node gxlNode) {
                final ElkNode layoutNode = layoutNodes.get(gxlNode.getId());
                GrooveGxlHelper.addLayoutToNode(
                    gxlNode,
                    layoutNode.getX() + XY_SHIFT_GROOVE_LAYOUT,
                    layoutNode.getY() + XY_SHIFT_GROOVE_LAYOUT);
              }
            });
  }

  private static void addLayoutToNode(Node gxlNode, double x, double y) {
    Attr layoutAttr = createAttribute("layout", String.format("%.0f %.0f 0 0", x, y));
    gxlNode.getAttr().add(layoutAttr);
  }

  private static ElkNode createElkGraph(
      Graph graph, Map<String, String> nodeLabels, Map<String, ElkNode> layoutNodes) {
    ElkNode layoutGraph = ElkGraphUtil.createGraph();

    graph
        .getNodeOrEdgeOrRel()
        .forEach(
            nodeOrEdge -> {
              if (nodeOrEdge instanceof Node gxlNode) {
                createNodeIfNeeded(layoutNodes, layoutGraph, gxlNode.getId(), nodeLabels);
              }
              if (nodeOrEdge instanceof Edge edge) {
                final String fromId = ((Node) edge.getFrom()).getId();
                final String toId = ((Node) edge.getTo()).getId();

                final ElkNode sourceLayoutNode =
                    createNodeIfNeeded(layoutNodes, layoutGraph, fromId, nodeLabels);
                final ElkNode targetLayoutNode =
                    createNodeIfNeeded(layoutNodes, layoutGraph, toId, nodeLabels);
                ElkGraphUtil.createSimpleEdge(sourceLayoutNode, targetLayoutNode);
              }
            });
    return layoutGraph;
  }

  private static ElkNode createNodeIfNeeded(
      Map<String, ElkNode> layoutNodes,
      ElkNode layoutGraph,
      String id,
      Map<String, String> nodeLabels) {
    return layoutNodes.computeIfAbsent(
        id,
        key -> {
          ElkNode node = ElkGraphUtil.createNode(layoutGraph);
          node.setHeight(50d);
          node.setWidth(nodeLabels.get(key).length() * 15d);
          return node;
        });
  }
}
