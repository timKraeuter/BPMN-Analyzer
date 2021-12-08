package groove.behaviorTransformer;

import behavior.piCalculus.*;
import groove.GrooveGxlHelper;
import groove.GxlToXMLConverter;
import groove.gxl.Graph;
import groove.gxl.Gxl;
import groove.gxl.Node;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.io.File;
import java.io.IOException;
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
    private static final String TYPE_RESTRICTION = TYPE + "Restriction";

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
    private static final String NAME = "name";

    private Graph graph;
    private AtomicLong idCounter;
    private Map<String, String> nodeIdToLabel;
    private Map<String, Node> nameToNode;

    void copyPiRulesAndTypeGraph(File targetFolder) {
        File sourceDirectory = new File(this.getClass().getResource("/GaducciPi").getFile());
        try {
            FileUtils.copyDirectory(sourceDirectory, targetFolder);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    void generatePiStartGraph(NamedPiProcess piProcess, File targetFolder) {
        Gxl gxl = new Gxl();
        this.graph = GrooveGxlHelper.createStandardGxlGraph(piProcess.getName(), gxl);

        this.idCounter = new AtomicLong(-1);
        this.nodeIdToLabel = new HashMap<>();
        this.nameToNode = new HashMap<>();

        // Create root process with flag go.
        Node rootNode = GrooveGxlHelper.createNodeWithNameAndRememberLabel(
                this.getNodeId(this.idCounter),
                TYPE_PROCESS,
                this.graph,
                this.nodeIdToLabel);
        GrooveGxlHelper.addFlagToNode(this.graph, rootNode, "go");

        Optional<Pair<Node, String>> parOrSum = this.convertProcess(
                piProcess.getProcess(),
                true);
        parOrSum.ifPresent(nodeEdgeLabelPair ->
                GrooveGxlHelper.createEdgeWithName(
                        this.graph,
                        rootNode,
                        nodeEdgeLabelPair.getLeft(),
                        nodeEdgeLabelPair.getRight()));

        GrooveGxlHelper.layoutGraph(this.graph, this.nodeIdToLabel);

        File startGraphFile = new File(targetFolder.getPath() + String.format("/%s.gst", piProcess.getName()));
        GxlToXMLConverter.toXml(gxl, startGraphFile);
    }

    private Optional<Pair<Node, String>> convertProcess(
            PiProcess piProcess,
            boolean addCoercion) {
        return piProcess.accept(new PiProcessVisitor<>() {

            @Override
            public Optional<Pair<Node, String>> handle(Parallelism parallelism) {
                Node parNode = this.createNodeWithName(TYPE_PAR);

                // arg 1
                Node processNodeArg1 = this.createNodeWithName(TYPE_PROCESS);
                GrooveGxlHelper.createEdgeWithName(PiCalcToGrooveTransformer.this.graph, parNode, processNodeArg1, ARG_1);

                final PiProcess arg1 = parallelism.getFirst();
                Optional<Pair<Node, String>> arg1Continuation = PiCalcToGrooveTransformer.this.convertProcess(arg1, true);
                arg1Continuation.ifPresent(nodeEdgeLabelPair ->
                        GrooveGxlHelper.createEdgeWithName(
                                PiCalcToGrooveTransformer.this.graph,
                                processNodeArg1,
                                nodeEdgeLabelPair.getLeft(),
                                nodeEdgeLabelPair.getRight()));

                // arg2
                Node processNodeArg2 = this.createNodeWithName(TYPE_PROCESS);
                GrooveGxlHelper.createEdgeWithName(PiCalcToGrooveTransformer.this.graph, parNode, processNodeArg2, ARG_2);

                final PiProcess arg2 = parallelism.getSecond();
                Optional<Pair<Node, String>> arg2Continuation = PiCalcToGrooveTransformer.this.convertProcess(arg2, true);
                arg2Continuation.ifPresent(nodeEdgeLabelPair ->
                        GrooveGxlHelper.createEdgeWithName(
                                PiCalcToGrooveTransformer.this.graph,
                                processNodeArg2,
                                nodeEdgeLabelPair.getLeft(),
                                nodeEdgeLabelPair.getRight()));

                return Optional.of(Pair.of(parNode, PAR));
            }

            private Node createNodeWithName(String name) {
                return GrooveGxlHelper.createNodeWithNameAndRememberLabel(
                        PiCalcToGrooveTransformer.this.getNodeId(PiCalcToGrooveTransformer.this.idCounter),
                        name,
                        PiCalcToGrooveTransformer.this.graph,
                        PiCalcToGrooveTransformer.this.nodeIdToLabel);
            }

            @Override
            public Optional<Pair<Node, String>> handle(NameRestriction restriction) {
                // TODO: Add picking a free name and renaming!
                final Node resNode = this.createNodeWithName(TYPE_RESTRICTION);

                // Create restricted name
                Node nameNode = this.createNodeForNameIfNeeded(restriction.getRestrictedName());
                GrooveGxlHelper.createEdgeWithName(PiCalcToGrooveTransformer.this.graph, resNode, nameNode, NAME);

                // Create process for subprocess
                Node processNode = this.createNodeWithName(TYPE_PROCESS);
                GrooveGxlHelper.createEdgeWithName(PiCalcToGrooveTransformer.this.graph, resNode, processNode, PROCESS);

                // Connect subprocess
                Optional<Pair<Node, String>> subProcessNode = PiCalcToGrooveTransformer.this.convertProcess(
                        restriction.getRestrictedProcess(),
                        true);
                subProcessNode.ifPresent(nodeEdgeLabelPair ->
                        GrooveGxlHelper.createEdgeWithName(
                                PiCalcToGrooveTransformer.this.graph,
                                processNode,
                                nodeEdgeLabelPair.getLeft(),
                                nodeEdgeLabelPair.getRight()));

                return Optional.of(Pair.of(resNode, RES));
            }

            @Override
            public Optional<Pair<Node, String>> handle(PrefixedProcess prefixedProcess) {
                // TODO: Add picking a free name and renaming!
                Node topLevelNode;

                Node summationNode = this.createNodeWithName(TYPE_SUMMATION);

                topLevelNode = summationNode;
                if (addCoercion) {
                    Node coercionNode = this.createNodeWithName(TYPE_COERCION);
                    GrooveGxlHelper.createEdgeWithName(PiCalcToGrooveTransformer.this.graph, coercionNode, summationNode, C);
                    topLevelNode = coercionNode;
                }

                Node opNode = this.createNodeWithName(this.getPrefixTypeString(prefixedProcess.getPrefixType()));

                GrooveGxlHelper.createEdgeWithName(PiCalcToGrooveTransformer.this.graph, summationNode, opNode, OP);

                Node channelNode = this.createNodeForNameIfNeeded(prefixedProcess.getChannel());
                GrooveGxlHelper.createEdgeWithName(PiCalcToGrooveTransformer.this.graph, opNode, channelNode, CHANNEL);

                Node payloadNode = this.createNodeForNameIfNeeded(prefixedProcess.getPayload());
                GrooveGxlHelper.createEdgeWithName(PiCalcToGrooveTransformer.this.graph, opNode, payloadNode, PAYLOAD);

                Node processNode = this.createNodeWithName(TYPE_PROCESS);
                GrooveGxlHelper.createEdgeWithName(PiCalcToGrooveTransformer.this.graph, opNode, processNode, PROCESS);

                Optional<Pair<Node, String>> subProcessNode = PiCalcToGrooveTransformer.this.convertProcess(
                        prefixedProcess.getProcess(),
                        true);
                subProcessNode.ifPresent(nodeEdgeLabelPair ->
                        GrooveGxlHelper.createEdgeWithName(
                                PiCalcToGrooveTransformer.this.graph,
                                processNode,
                                nodeEdgeLabelPair.getLeft(),
                                nodeEdgeLabelPair.getRight()));

                return Optional.of(Pair.of(topLevelNode, addCoercion ? C : ""));
            }

            private Node createNodeForNameIfNeeded(String name) {
                Node nameNode = PiCalcToGrooveTransformer.this.nameToNode.get(name);
                if (nameNode == null) {
                    nameNode = GrooveGxlHelper.createNodeWithNameAndRememberLabel(
                            PiCalcToGrooveTransformer.this.getNodeId(PiCalcToGrooveTransformer.this.idCounter),
                            TYPE_NAME,
                            PiCalcToGrooveTransformer.this.graph,
                            PiCalcToGrooveTransformer.this.nodeIdToLabel);
                    PiCalcToGrooveTransformer.this.nameToNode.put(name, nameNode);
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
                Node summationNode = this.createNodeWithName(TYPE_SUMMATION);

                topLevelNode = summationNode;
                if (addCoercion) {
                    Node coercionNode = this.createNodeWithName(TYPE_COERCION);
                    GrooveGxlHelper.createEdgeWithName(PiCalcToGrooveTransformer.this.graph, coercionNode, summationNode, C);
                    topLevelNode = coercionNode;
                }
                Node sumNode = this.createNodeWithName(TYPE_SUM);

                GrooveGxlHelper.createEdgeWithName(PiCalcToGrooveTransformer.this.graph, summationNode, sumNode, SUM);

                // Arg 1
                Sum first = multiarySum.getFirst();
                Optional<Pair<Node, String>> arg1 = PiCalcToGrooveTransformer.this.convertProcess(first, false);
                arg1.ifPresent(nodeStringPair -> GrooveGxlHelper.createEdgeWithName(PiCalcToGrooveTransformer.this.graph, sumNode, nodeStringPair.getLeft(), ARG_1));

                // Arg 2
                Sum second = multiarySum.getSecond();
                Optional<Pair<Node, String>> arg2 = PiCalcToGrooveTransformer.this.convertProcess(second, false);
                arg2.ifPresent(nodeStringPair -> GrooveGxlHelper.createEdgeWithName(PiCalcToGrooveTransformer.this.graph, sumNode, nodeStringPair.getLeft(), ARG_2));

                return Optional.of(Pair.of(topLevelNode, addCoercion ? C : ""));
            }
        });
    }

    private String getNodeId(AtomicLong idCounter) {
        return "n" + idCounter.incrementAndGet();
    }
}