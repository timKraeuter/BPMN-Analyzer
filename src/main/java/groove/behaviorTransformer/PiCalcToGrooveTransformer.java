package groove.behaviorTransformer;

import behavior.piCalculus.*;
import groove.GrooveGxlHelper;
import groove.GxlToXMLConverter;
import groove.gxl.Graph;
import groove.gxl.Gxl;
import groove.gxl.Node;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public class PiCalcToGrooveTransformer {

    private static final String TYPE = "type:";
    private static final String TYPE_PROCESS = TYPE + "Process";
    private static final String TYPE_OUT = TYPE + "Out";
    private static final String TYPE_IN = TYPE + "In";
    private static final String PROCESS = "process";
    private static final String TYPE_SUMMATION = TYPE + "Summation";
    private static final String OP = "op";
    private static final String TYPE_COERCION = TYPE + "Coercion";
    private static final String C = "c";
    private static final String TYPE_NAME = TYPE + "Name";
    public static final String CHANNEL = "channel";
    public static final String PAYLOAD = "payload";

    void copyPiRules(File targetFolder) {
        // TODO: Copy the fixed set of rules from somewhere.
    }

    void generatePiStartGraph(NamedPiProcess piProcess, File targetFolder) {
        Gxl gxl = new Gxl();
        Graph graph = GrooveGxlHelper.createStandardGxlGraph(piProcess.getName(), gxl);

        AtomicLong idCounter = new AtomicLong(-1);
        Map<String, String> nodeIdToLabel = new HashMap<>();
        Map<String, Node> nameToNode = new HashMap<>();

        // Create root process with flag go.
        Node node = GrooveGxlHelper.createNodeWithName(this.getNodeId(idCounter), TYPE_PROCESS, graph);
        GrooveGxlHelper.addFlagToNode(graph, node, "go");
        nodeIdToLabel.put(node.getId(), TYPE_PROCESS);
        // TODO: connect to the node either with c or par.
        Node node1 = this.convertProcess(graph, piProcess.getProcess(), nodeIdToLabel, idCounter, nameToNode);

        GrooveGxlHelper.layoutGraph(graph, nodeIdToLabel);

        File startGraphFile = new File(targetFolder.getPath() + String.format("/%s.gst", piProcess.getName()));
        GxlToXMLConverter.toXml(gxl, startGraphFile);
    }

    private Node convertProcess(
            Graph graph,
            PiProcess piProcess,
            Map<String, String> nodeIdToLabel,
            AtomicLong idCounter,
            Map<String, Node> nameToNode) {
        return piProcess.accept(new PiProcessVisitor<>() {

            @Override
            public Node handle(Parallelism parallelism) {
                return null;
            }

            @Override
            public Node handle(NameRestriction restriction) {
                return null;
            }

            @Override
            public Node handle(PrefixedProcess prefixedProcess) {
                // TODO: Add picking a free name and renaming!
                Node coercionNode = GrooveGxlHelper.createNodeWithNameAndRememberLabel(
                        PiCalcToGrooveTransformer.this.getNodeId(idCounter),
                        TYPE_COERCION,
                        graph,
                        nodeIdToLabel);

                Node summationNode = GrooveGxlHelper.createNodeWithNameAndRememberLabel(
                        PiCalcToGrooveTransformer.this.getNodeId(idCounter),
                        TYPE_SUMMATION,
                        graph,
                        nodeIdToLabel);

                GrooveGxlHelper.createEdgeWithName(graph, coercionNode, summationNode, C);

                Node opNode = GrooveGxlHelper.createNodeWithNameAndRememberLabel(
                        PiCalcToGrooveTransformer.this.getNodeId(idCounter),
                        this.getPrefixTypeString(prefixedProcess.getPrefixType()),
                        graph,
                        nodeIdToLabel);

                GrooveGxlHelper.createEdgeWithName(graph, summationNode, opNode, OP);

                Node channelNode = this.createNodeForNameIfNeeded(prefixedProcess.getChannel(), nodeIdToLabel);
                GrooveGxlHelper.createEdgeWithName(graph, opNode, channelNode, CHANNEL);

                Node payloadNode = this.createNodeForNameIfNeeded(prefixedProcess.getPayload(), nodeIdToLabel);
                GrooveGxlHelper.createEdgeWithName(graph, opNode, payloadNode, PAYLOAD);

                Node subProcessNode = PiCalcToGrooveTransformer.this.convertProcess(
                        graph,
                        prefixedProcess.getProcess(),
                        nodeIdToLabel,
                        idCounter,
                        nameToNode);
                GrooveGxlHelper.createEdgeWithName(graph, opNode, subProcessNode, PROCESS);

                return coercionNode;
            }

            private Node createNodeForNameIfNeeded(String name, Map<String, String> nodeIdToLabel) {
                Node nameNode = nameToNode.get(name);
                if (nameNode == null) {
                    nameNode = GrooveGxlHelper.createNodeWithNameAndRememberLabel(
                            PiCalcToGrooveTransformer.this.getNodeId(idCounter),
                            TYPE_NAME,
                            graph,
                            nodeIdToLabel);
                }
                return nameNode;
            }

            private String getPrefixTypeString(PrefixType prefixType) {
                switch (prefixType) {
                    case SEND:
                        return TYPE_OUT;
                    case RECEIVE:
                        return TYPE_IN;
                }
                throw new RuntimeException("Unknown PrefixType: " + prefixType);
            }

            @Override
            public Node handle(EmptySum emptySum) {
                return GrooveGxlHelper.createNodeWithNameAndRememberLabel(
                        PiCalcToGrooveTransformer.this.getNodeId(idCounter),
                        TYPE_PROCESS,
                        graph,
                        nodeIdToLabel);
            }

            @Override
            public Node handle(MultiarySum multiarySum) {
                return null;
            }
        });
    }

    private String getNodeId(AtomicLong idCounter) {
        return "n" + idCounter.incrementAndGet();
    }
}
