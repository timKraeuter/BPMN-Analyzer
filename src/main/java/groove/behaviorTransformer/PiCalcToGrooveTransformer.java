package groove.behaviorTransformer;

import behavior.piCalculus.*;
import groove.GrooveGxlHelper;
import groove.GxlToXMLConverter;
import groove.gxl.Graph;
import groove.gxl.Gxl;
import groove.gxl.Node;
import org.apache.commons.lang3.tuple.Pair;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
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

        Optional<Pair<Node, String>> parOrSum = this.convertProcess(
                piProcess.getProcess(),
                true);
        parOrSum.ifPresent(nodeEdgeLabelPair ->
                GrooveGxlHelper.createEdgeWithName(
                        graph,
                        rootNode,
                        nodeEdgeLabelPair.getLeft(),
                        nodeEdgeLabelPair.getRight()));

        GrooveGxlHelper.layoutGraph(graph, nodeIdToLabel);

        File startGraphFile = new File(targetFolder.getPath() + String.format("/%s.gst", piProcess.getName()));
        GxlToXMLConverter.toXml(gxl, startGraphFile);
    }

    private Optional<Pair<Node, String>> convertProcess(
            PiProcess piProcess,
            boolean addCoercion) {
        return piProcess.accept(new PiProcessVisitor<>() {

            @Override
            public Optional<Pair<Node, String>> handle(Parallelism parallelism) {
                Node parNode = createNodeWithName(TYPE_PAR);

                // arg 1
                Node processNodeArg1 = createNodeWithName(TYPE_PROCESS);
                GrooveGxlHelper.createEdgeWithName(graph, parNode, processNodeArg1, ARG_1);

                final PiProcess arg1 = parallelism.getFirst();
                Optional<Pair<Node, String>> arg1Continuation = PiCalcToGrooveTransformer.this.convertProcess(arg1, true);
                arg1Continuation.ifPresent(nodeEdgeLabelPair ->
                        GrooveGxlHelper.createEdgeWithName(
                                graph,
                                processNodeArg1,
                                nodeEdgeLabelPair.getLeft(),
                                nodeEdgeLabelPair.getRight()));

                // arg2
                Node processNodeArg2 = createNodeWithName(TYPE_PROCESS);
                GrooveGxlHelper.createEdgeWithName(graph, parNode, processNodeArg2, ARG_2);

                final PiProcess arg2 = parallelism.getSecond();
                Optional<Pair<Node, String>> arg2Continuation = PiCalcToGrooveTransformer.this.convertProcess(arg2, true);
                arg2Continuation.ifPresent(nodeEdgeLabelPair ->
                        GrooveGxlHelper.createEdgeWithName(
                                graph,
                                processNodeArg2,
                                nodeEdgeLabelPair.getLeft(),
                                nodeEdgeLabelPair.getRight()));

                return Optional.of(Pair.of(parNode, PAR));
            }

            private Node createNodeWithName(String name) {
                return GrooveGxlHelper.createNodeWithNameAndRememberLabel(
                        PiCalcToGrooveTransformer.this.getNodeId(idCounter),
                        name,
                        graph,
                        nodeIdToLabel);
            }

            @Override
            public Optional<Pair<Node, String>> handle(NameRestriction restriction) {
                return Optional.of(Pair.of(null, "res"));
            }

            @Override
            public Optional<Pair<Node, String>> handle(PrefixedProcess prefixedProcess) {
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

                Node processNode = createNodeWithName(TYPE_PROCESS);
                GrooveGxlHelper.createEdgeWithName(graph, opNode, processNode, PROCESS);

                Optional<Pair<Node, String>> subProcessNode = PiCalcToGrooveTransformer.this.convertProcess(
                        prefixedProcess.getProcess(),
                        true);
                subProcessNode.ifPresent(nodeEdgeLabelPair ->
                        GrooveGxlHelper.createEdgeWithName(
                                graph,
                                processNode,
                                nodeEdgeLabelPair.getLeft(),
                                nodeEdgeLabelPair.getRight()));

                return Optional.of(Pair.of(topLevelNode, addCoercion ? C : ""));
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
            public Optional<Pair<Node, String>> handle(EmptySum emptySum) {
                return Optional.empty();
            }

            @Override
            public Optional<Pair<Node, String>> handle(MultiarySum multiarySum) {
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
                Optional<Pair<Node, String>> arg1 = PiCalcToGrooveTransformer.this.convertProcess(first, false);
                arg1.ifPresent(nodeStringPair -> GrooveGxlHelper.createEdgeWithName(graph, sumNode, nodeStringPair.getLeft(), ARG_1));

                // Arg 2
                Sum second = multiarySum.getSecond();
                Optional<Pair<Node, String>> arg2 = PiCalcToGrooveTransformer.this.convertProcess(second, false);
                arg2.ifPresent(nodeStringPair -> GrooveGxlHelper.createEdgeWithName(graph, sumNode, nodeStringPair.getLeft(), ARG_2));

                return Optional.of(Pair.of(topLevelNode, addCoercion ? C : ""));
            }
        });
    }

    private String getNodeId(AtomicLong idCounter) {
        return "n" + idCounter.incrementAndGet();
    }
}
