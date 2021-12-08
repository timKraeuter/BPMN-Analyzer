package groove.behaviorTransformer;

import behavior.Behavior;
import behavior.BehaviorVisitor;
import behavior.bpmn.BPMNProcessModel;
import behavior.fsm.FiniteStateMachine;
import behavior.petriNet.PetriNet;
import behavior.piCalculus.NamedPiProcess;
import com.google.common.collect.Maps;
import groove.GrooveGxlHelper;
import groove.GxlToXMLConverter;
import groove.graph.GrooveGraph;
import groove.gxl.Graph;
import groove.gxl.Gxl;
import groove.gxl.Node;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class BehaviorToGrooveTransformer {
    static final String START_GST = "/start.gst";
    static final String START = "start";

    void generateGrooveGrammar(File grooveFolder, String name, Behavior... behaviors) {
        File graphGrammarSubFolder = this.makeSubFolder(name, grooveFolder);
        final boolean[] piProcessIncluded = {false};
        Set<GrooveGraph> startGraphs = new LinkedHashSet<>();
        Arrays.stream(behaviors).forEach(behavior -> behavior.accept(new BehaviorVisitor() {
            @Override
            public void handle(FiniteStateMachine finiteStateMachine) {
                FSMToGrooveTransformer transformer = new FSMToGrooveTransformer();

                startGraphs.add(transformer.generateStartGraph(finiteStateMachine, true));

                transformer.generateFSMRules(finiteStateMachine, graphGrammarSubFolder, true);
            }

            @Override
            public void handle(PetriNet petriNet) {
                PNToGrooveTransformer transformer = new PNToGrooveTransformer();

                // TODO: Save start graph and add a prefix to it
                transformer.generatePNStartGraphFile(petriNet, graphGrammarSubFolder);

                transformer.generatePNRules(petriNet, graphGrammarSubFolder, true);
            }

            @Override
            public void handle(BPMNProcessModel bpmnProcessModel) {
                BPMNToGrooveTransformer transformer = new BPMNToGrooveTransformer();

                startGraphs.add(transformer.generateStartGraph(bpmnProcessModel, true));

                // TODO: prefix rules when start graph is prefixed.
                transformer.generateBPMNRules(bpmnProcessModel, graphGrammarSubFolder, true);
            }

            @Override
            public void handle(NamedPiProcess piProcess) {
                piProcessIncluded[0] = true;

                PiCalcToGrooveTransformer transformer = new PiCalcToGrooveTransformer();

                // TODO: Save start graph
                transformer.generatePiStartGraph(piProcess, graphGrammarSubFolder);

                transformer.copyPiRulesAndTypeGraph(graphGrammarSubFolder);

            }
        }));

        final Map<String, String> additionalProperties = Maps.newHashMap();
        if (piProcessIncluded[0]) {
            additionalProperties.put("typeGraph", "Type");
            additionalProperties.put("checkDangling", "true");
        }
        this.generatePropertiesFile(graphGrammarSubFolder, "start", additionalProperties);

        // Merge start graphs and write the final one.
        Optional<GrooveGraph> startGraph = startGraphs.stream()
                                                      .reduce((graph, graph2) -> graph.union(graph2, (name1, name2) -> name1));
        startGraph.ifPresent(graph -> this.generateStartGraphFile(graph, graphGrammarSubFolder));
    }

    void generateGrooveGrammar(Behavior behavior, File targetFolder, boolean addPrefix) {
        behavior.accept(new BehaviorVisitor() {
            @Override
            public void handle(FiniteStateMachine finiteStateMachine) {
                BehaviorToGrooveTransformer.this.generateGrooveGrammarForFSM(finiteStateMachine, targetFolder, addPrefix);
            }

            @Override
            public void handle(PetriNet petriNet) {
                BehaviorToGrooveTransformer.this.generateGrooveGrammarForPN(petriNet, targetFolder, addPrefix);
            }

            @Override
            public void handle(BPMNProcessModel bpmnProcessModel) {
                BehaviorToGrooveTransformer.this.generateGrooveGrammarForBPMNProcessModel(bpmnProcessModel, targetFolder, addPrefix);
            }

            @Override
            public void handle(NamedPiProcess piProcess) {
                BehaviorToGrooveTransformer.this.generateGrooveGrammarForPiProcess(piProcess, targetFolder, addPrefix);
            }
        });
    }

    private void generateGrooveGrammarForPiProcess(NamedPiProcess piProcess, File grooveDir, boolean addPrefix) {
        File graphGrammarSubFolder = this.makeSubFolder(piProcess, grooveDir);
        PiCalcToGrooveTransformer transformer = new PiCalcToGrooveTransformer();

        transformer.generatePiStartGraph(piProcess, graphGrammarSubFolder);

        transformer.copyPiRulesAndTypeGraph(graphGrammarSubFolder);

        final Map<String, String> additionalProperties = Maps.newHashMap();
        additionalProperties.put("typeGraph", "Type");
        additionalProperties.put("checkDangling", "true");
        this.generatePropertiesFile(graphGrammarSubFolder, piProcess.getName(), additionalProperties);
    }

    private void generateGrooveGrammarForBPMNProcessModel(BPMNProcessModel bpmnProcessModel, File grooveDir, boolean addPrefix) {
        File graphGrammarSubFolder = this.makeSubFolder(bpmnProcessModel, grooveDir);
        BPMNToGrooveTransformer transformer = new BPMNToGrooveTransformer();

        // Generate start graph
        // TODO: Make an abstraction of transformer, such that every transformer has to generate a start graph. Than refactor these two lines into one method and use many times.
        GrooveGraph startGraph = transformer.generateStartGraph(bpmnProcessModel, addPrefix);
        this.generateStartGraphFile(startGraph, graphGrammarSubFolder);

        // Generate rules
        transformer.generateBPMNRules(bpmnProcessModel, graphGrammarSubFolder, addPrefix);

        this.generatePropertiesFile(graphGrammarSubFolder, START, Maps.newHashMap());
    }

    private void generateGrooveGrammarForPN(PetriNet petriNet, File grooveDir, boolean addPrefix) {
        File graphGrammarSubFolder = this.makeSubFolder(petriNet, grooveDir);
        PNToGrooveTransformer transformer = new PNToGrooveTransformer();

        // Generate start graph
        // TODO: Should accept a potential prefix!
        transformer.generatePNStartGraphFile(petriNet, graphGrammarSubFolder);

        // Generate rules
        transformer.generatePNRules(petriNet, graphGrammarSubFolder, addPrefix);

        this.generatePropertiesFile(graphGrammarSubFolder, START, Maps.newHashMap());
    }

    private void generateGrooveGrammarForFSM(FiniteStateMachine finiteStateMachine, File grooveDir, boolean addPrefix) {
        File graphGrammarSubFolder = this.makeSubFolder(finiteStateMachine, grooveDir);
        FSMToGrooveTransformer transformer = new FSMToGrooveTransformer();

        GrooveGraph startGraph = transformer.generateStartGraph(finiteStateMachine, addPrefix);
        this.generateStartGraphFile(startGraph, graphGrammarSubFolder);

        transformer.generateFSMRules(finiteStateMachine, graphGrammarSubFolder, addPrefix);

        this.generatePropertiesFile(graphGrammarSubFolder, START, Maps.newHashMap());
    }

    void generateStartGraphFile(GrooveGraph grooveGraph, File targetFolder) {
        Gxl gxl = createGxlFromGrooveGraph(grooveGraph);
        File startGraphFile = new File(targetFolder.getPath() + START_GST);

        GxlToXMLConverter.toXml(gxl, startGraphFile);
    }

    private File makeSubFolder(Behavior behavior, File grooveDir) {
        return this.makeSubFolder(behavior.getName(), grooveDir);
    }

    private File makeSubFolder(String folderName, File grooveDir) {
        File graphGrammarSubFolder = new File(grooveDir + "/" + folderName + ".gps");
        //noinspection ResultOfMethodCallIgnored
        graphGrammarSubFolder.mkdir();
        return graphGrammarSubFolder;
    }

    private void generatePropertiesFile(File subFolder, String startGraph, Map<String, String> additionalProperties) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();

        String propertiesContent = String.format("# %s (Groove rule generator)\n" +
                        "location=%s\n" +
                        "startGraph=%s\n" +
                        this.getAdditionalProperties(additionalProperties) +
                        "grooveVersion=5.8.1\n" +
                        "grammarVersion=3.7",
                dtf.format(now),
                subFolder.getPath(),
                startGraph);
        File properties_file = new File(subFolder + "/" + "system.properties");
        try {
            FileUtils.writeStringToFile(properties_file, propertiesContent, StandardCharsets.UTF_8, false);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String getAdditionalProperties(Map<String, String> additionalProperties) {
        return additionalProperties.entrySet().stream()
                                   .reduce("",
                                           (prop1, prop2) -> prop1 + prop2 + "\n",
                                           (key, value) -> key + "=" + value);
    }

    static Gxl createGxlFromGrooveGraph(GrooveGraph graph) {
        String gxlGraphName = String.format("%s_%s", graph.getName(), START);
        Gxl gxl = new Gxl();
        Graph gxlGraph = GrooveGxlHelper.createStandardGxlGraph(gxlGraphName, gxl);

        Map<String, String> idToNodeLabel = new HashMap<>();
        Map<String, Node> grooveNodeIdToGxlNode = new HashMap<>();

        graph.nodes().forEach(node -> {
            idToNodeLabel.put(node.getId(), node.getName());
            Node gxlNode = GrooveGxlHelper.createNodeWithName(node.getId(), node.getName(), gxlGraph);
            grooveNodeIdToGxlNode.put(node.getId(), gxlNode);
        });
        graph.edges().forEach(edge -> GrooveGxlHelper.createEdgeWithName(
                gxlGraph,
                grooveNodeIdToGxlNode.get(edge.getSourceNode().getId()),
                grooveNodeIdToGxlNode.get(edge.getTargetNode().getId()),
                edge.getName()));

        GrooveGxlHelper.layoutGraph(gxlGraph, idToNodeLabel);
        return gxl;
    }
}
