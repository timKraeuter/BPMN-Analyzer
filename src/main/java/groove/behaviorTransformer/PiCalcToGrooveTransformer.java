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

    // Possible node labels.
    private static final String TYPE_PROCESS = TYPE + "Process";
    private static final String TYPE_OUT = TYPE + "Out";
    private static final String TYPE_IN = TYPE + "In";
    private static final String TYPE_SUMMATION = TYPE + "Summation";
    private static final String TYPE_COERCION = TYPE + "Coercion";
    private static final String TYPE_NAME = TYPE + "Name";
    private static final String TYPE_SUM = TYPE + "Sum";
    private static final String TYPE_PAR = TYPE + "Par";

    // Possible edge labels
    private static final String PROCESS = "process";
    private static final String OP = "op";
    private static final String C = "c";
    private static final String CHANNEL = "channel";
    private static final String PAYLOAD = "payload";
    private static final String PAR = "par";
    private static final String RES = "res";
    private static final String SUM = "sum";
    private static final String ARG_1 = "arg1";
    private static final String ARG_2 = "arg2";
    private Graph graph;
    private AtomicLong idCounter;
    private HashMap<String, String> nodeIdToLabel;
    private HashMap<String, Node> nameToNode;

    void copyPiRules(File targetFolder) {
        // TODO: Copy the fixed set of rules from somewhere.
    }

    void generatePiStartGraph(NamedPiProcess piProcess, File targetFolder) {
        Gxl gxl = new Gxl();
        graph = GrooveGxlHelper.createStandardGxlGraph(piProcess.getName(), gxl);

        this.idCounter = new AtomicLong(-1);
        this.nodeIdToLabel = new HashMap<>();
        this.nameToNode = new HashMap<>();

        // Create root process with flag go.
        Node rootNode = GrooveGxlHelper.createNodeWithNameAndRememberLabel(
                this.getNodeId(idCounter),
                TYPE_PROCESS,
                graph,
                nodeIdToLabel);
        GrooveGxlHelper.addFlagToNode(graph, rootNode, "go");

        if (!piProcess.getProcess().isEmptySum()) {
            Node parOrSum = this.convertProcess(
                    piProcess.getProcess(),
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
            PiProcess piProcess,
            boolean addCoercion) {
        return piProcess.accept(new PiProcessVisitor<>() {

            @Override
            public Node handle(Parallelism parallelism) {
                Node parNode = createNodeWithName(TYPE_PAR);

                // arg 1
                Node processNodeArg1 = createNodeWithName(TYPE_PROCESS);
                GrooveGxlHelper.createEdgeWithName(graph, parNode, processNodeArg1, ARG_1);

                final PiProcess arg1 = parallelism.getFirst();
                Node arg1Continuation = PiCalcToGrooveTransformer.this.convertProcess(arg1, true);
                GrooveGxlHelper.createEdgeWithName(graph, processNodeArg1, arg1Continuation, C);

                // arg2
                Node processNodeArg2 = createNodeWithName(TYPE_PROCESS);
                GrooveGxlHelper.createEdgeWithName(graph, parNode, processNodeArg2, ARG_2);

                final PiProcess arg2 = parallelism.getSecond();
                Node arg2Continuation = PiCalcToGrooveTransformer.this.convertProcess(arg2, true);
                GrooveGxlHelper.createEdgeWithName(graph, processNodeArg2, arg2Continuation, C);

                return parNode;
            }

            private Node createNodeWithName(String name) {
                return GrooveGxlHelper.createNodeWithNameAndRememberLabel(
                        PiCalcToGrooveTransformer.this.getNodeId(idCounter),
                        name,
                        graph,
                        nodeIdToLabel);
            }

            @Override
            public Node handle(NameRestriction restriction) {
                return null;
            }

            @Override
            public Node handle(PrefixedProcess prefixedProcess) {
                // TODO: Add picking a free name and renaming!
                Node topLevelNode;

                Node summationNode = createNodeWithName(TYPE_SUMMATION);

                topLevelNode = summationNode;
                if (addCoercion) {
                    Node coercionNode = createNodeWithName(TYPE_COERCION);
                    GrooveGxlHelper.createEdgeWithName(graph, coercionNode, summationNode, C);
                    topLevelNode = coercionNode;
                }

                Node opNode = createNodeWithName(this.getPrefixTypeString(prefixedProcess.getPrefixType()));

                GrooveGxlHelper.createEdgeWithName(graph, summationNode, opNode, OP);

                Node channelNode = this.createNodeForNameIfNeeded(prefixedProcess.getChannel(), nodeIdToLabel);
                GrooveGxlHelper.createEdgeWithName(graph, opNode, channelNode, CHANNEL);

                Node payloadNode = this.createNodeForNameIfNeeded(prefixedProcess.getPayload(), nodeIdToLabel);
                GrooveGxlHelper.createEdgeWithName(graph, opNode, payloadNode, PAYLOAD);

                Node subProcessNode = PiCalcToGrooveTransformer.this.convertProcess(prefixedProcess.getProcess(), true);
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
                    nameToNode.put(name, nameNode);
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
                Node topLevelNode;
                Node summationNode = createNodeWithName(TYPE_SUMMATION);

                topLevelNode = summationNode;
                if (addCoercion) {
                    Node coercionNode = createNodeWithName(TYPE_COERCION);
                    GrooveGxlHelper.createEdgeWithName(graph, coercionNode, summationNode, C);
                    topLevelNode = coercionNode;
                }
                Node sumNode = createNodeWithName(TYPE_SUM);

                GrooveGxlHelper.createEdgeWithName(graph, summationNode, sumNode, SUM);

                // Arg 1
                Sum first = multiarySum.getFirst();
                Node arg1 = PiCalcToGrooveTransformer.this.convertProcess(first, false);
                GrooveGxlHelper.createEdgeWithName(graph, sumNode, arg1, ARG_1);

                // Arg 2
                Sum second = multiarySum.getSecond();
                Node arg2 = PiCalcToGrooveTransformer.this.convertProcess(second, false);
                GrooveGxlHelper.createEdgeWithName(graph, sumNode, arg2, ARG_2);

                return topLevelNode;
            }
        });
    }

    private String getNodeId(AtomicLong idCounter) {
        return "n" + idCounter.incrementAndGet();
    }
}
