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
    public static final String PAR = "par";
    public static final String RES = "res";

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
        Node rootNode = GrooveGxlHelper.createNodeWithNameAndRememberLabel(
                this.getNodeId(idCounter),
                TYPE_PROCESS,
                graph,
                nodeIdToLabel);
        GrooveGxlHelper.addFlagToNode(graph, rootNode, "go");

        if (!piProcess.getProcess().isEmptySum()) {
            Node parOrSum = this.convertProcess(
                    graph,
                    piProcess.getProcess(),
                    nodeIdToLabel,
                    idCounter,
                    nameToNode,
                    true);
            this.addEdgeFromRoot(piProcess, graph, rootNode, parOrSum);
        }

        GrooveGxlHelper.layoutGraph(graph, nodeIdToLabel);

        File startGraphFile = new File(targetFolder.getPath() + String.format("/%s.gst", piProcess.getName()));
        GxlToXMLConverter.toXml(gxl, startGraphFile);
    }

    private void addEdgeFromRoot(NamedPiProcess piProcess, Graph graph, Node rootNode, Node parOrSum) {
        piProcess.getProcess().accept(new PiProcessVisitor<Void>() {
            @Override
            public Void handle(Parallelism parallelism) {
                // Add par edge
                GrooveGxlHelper.createEdgeWithName(graph, rootNode, parOrSum, PAR);
                return null;
            }

            @Override
            public Void handle(NameRestriction restriction) {
                // Add res edge
                GrooveGxlHelper.createEdgeWithName(graph, rootNode, parOrSum, RES);
                return null;
            }

            @Override
            public Void handle(PrefixedProcess prefixedProcess) {
                // Add coercion edge
                GrooveGxlHelper.createEdgeWithName(graph, rootNode, parOrSum, C);
                return null;
            }

            @Override
            public Void handle(EmptySum emptySum) {
                return null;
            }

            @Override
            public Void handle(MultiarySum multiarySum) {
                // Add coercion edge
                GrooveGxlHelper.createEdgeWithName(graph, rootNode, parOrSum, C);
                return null;
            }
        });
    }

    private Node convertProcess(
            Graph graph,
            PiProcess piProcess,
            Map<String, String> nodeIdToLabel,
            AtomicLong idCounter,
            Map<String, Node> nameToNode,
            boolean addCoercion) {
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
                // TODO: respect coercion boolean
                // TODO: Add picking a free name and renaming!
                Node topLevelNode;

                Node summationNode = GrooveGxlHelper.createNodeWithNameAndRememberLabel(
                        PiCalcToGrooveTransformer.this.getNodeId(idCounter),
                        TYPE_SUMMATION,
                        graph,
                        nodeIdToLabel);

                topLevelNode = summationNode;
                if (addCoercion) {
                    Node coercionNode = GrooveGxlHelper.createNodeWithNameAndRememberLabel(
                            PiCalcToGrooveTransformer.this.getNodeId(idCounter),
                            TYPE_COERCION,
                            graph,
                            nodeIdToLabel);
                    GrooveGxlHelper.createEdgeWithName(graph, coercionNode, summationNode, C);
                    topLevelNode = coercionNode;
                }

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
                        nameToNode,
                        true);
                GrooveGxlHelper.createEdgeWithName(graph, opNode, subProcessNode, PROCESS);

                return topLevelNode;
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
                    case OUT:
                        return TYPE_OUT;
                    case IN:
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
