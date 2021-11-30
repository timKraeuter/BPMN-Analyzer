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
import groove.gxl.Graph;
import groove.gxl.Gxl;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class BehaviorToGrooveTransformer {
    static final String START_GST = "/start.gst";
    static final String START = "start";
    static final String START_NODE_ID = "n0";

    void generateGrooveGrammar(Behavior behavior, File targetFolder) {
        behavior.accept(new BehaviorVisitor() {
            @Override
            public void handle(FiniteStateMachine finiteStateMachine) {
                BehaviorToGrooveTransformer.this.generateGrooveGrammarForFSM(finiteStateMachine, targetFolder);
            }

            @Override
            public void handle(PetriNet petriNet) {
                BehaviorToGrooveTransformer.this.generateGrooveGrammarForPN(petriNet, targetFolder);
            }

            @Override
            public void handle(BPMNProcessModel bpmnProcessModel) {
                BehaviorToGrooveTransformer.this.generateGrooveGrammarForBPMNProcessModel(bpmnProcessModel, targetFolder);
            }

            @Override
            public void handle(NamedPiProcess piProcess) {
                BehaviorToGrooveTransformer.this.generateGrooveGrammarForPiProcess(piProcess, targetFolder);
            }
        });
    }

    private void generateGrooveGrammarForPiProcess(NamedPiProcess piProcess, File grooveDir) {
        File graphGrammarSubFolder = this.makeSubFolder(piProcess, grooveDir);
        PiCalcToGrooveTransformer transformer = new PiCalcToGrooveTransformer();

        transformer.generatePiStartGraph(piProcess, graphGrammarSubFolder);

        transformer.copyPiRules(graphGrammarSubFolder);

        final Map<String, String> additionalProperties = Maps.newHashMap();
        additionalProperties.put("typeGraph", "Type");
        additionalProperties.put("checkDangling", "true");
        this.generatePropertiesFile(graphGrammarSubFolder, additionalProperties);
    }

    private void generateGrooveGrammarForBPMNProcessModel(BPMNProcessModel bpmnProcessModel, File grooveDir) {
        File graphGrammarSubFolder = this.makeSubFolder(bpmnProcessModel, grooveDir);
        BPMNToGrooveTransformer transformer = new BPMNToGrooveTransformer();

        // Generate start graph
        transformer.generateBPMNStartGraph(bpmnProcessModel, graphGrammarSubFolder);

        // Generate rules
        transformer.generateBPMNRules(bpmnProcessModel, graphGrammarSubFolder);

        this.generatePropertiesFile(graphGrammarSubFolder, Maps.newHashMap());
    }

    private void generateGrooveGrammarForPN(PetriNet petriNet, File grooveDir) {
        File graphGrammarSubFolder = this.makeSubFolder(petriNet, grooveDir);
        PNToGrooveTransformer transformer = new PNToGrooveTransformer();

        // Generate start graph
        transformer.generatePNStartGraphFile(petriNet, graphGrammarSubFolder);

        // Generate rules
        transformer.generatePNRules(petriNet, graphGrammarSubFolder);

        this.generatePropertiesFile(graphGrammarSubFolder, Maps.newHashMap());
    }

    private void generateGrooveGrammarForFSM(FiniteStateMachine finiteStateMachine, File grooveDir) {
        File graphGrammarSubFolder = this.makeSubFolder(finiteStateMachine, grooveDir);
        FSMToGrooveTransformer transformer = new FSMToGrooveTransformer();

        transformer.generateFSMStartGraphFile(finiteStateMachine, graphGrammarSubFolder);

        transformer.generateFSMRules(finiteStateMachine, graphGrammarSubFolder);

        this.generatePropertiesFile(graphGrammarSubFolder, Maps.newHashMap());
    }

    private File makeSubFolder(Behavior behavior, File grooveDir) {
        File graphGrammarSubFolder = new File(grooveDir + "/" + behavior.getName() + ".gps");
        //noinspection ResultOfMethodCallIgnored
        graphGrammarSubFolder.mkdir();
        return graphGrammarSubFolder;
    }

    private void generatePropertiesFile(File subFolder, Map<String, String> additionalProperties) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();

        String propertiesContent = String.format("# %s (Groove rule generator)\n" +
                        "location=%s\n" +
                        "startGraph=start\n" +
                        additionalProperties.entrySet().stream()
                                            .reduce("",
                                                    (prop1, prop2) -> prop1 + prop2 + "\n",
                                                    (key, value) -> key + "=" + value) +
                        "grooveVersion=5.8.1\n" +
                        "grammarVersion=3.7",
                dtf.format(now),
                subFolder.getPath());
        File properties_file = new File(subFolder + "/" + "system.properties");
        try {
            FileUtils.writeStringToFile(properties_file, propertiesContent, StandardCharsets.UTF_8, false);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    static void createStartGraphWithOneNode(File targetFolder, String startNodeName) {
        Gxl gxl = new Gxl();
        Graph graph = GrooveGxlHelper.createStandardGxlGraph(START, gxl);
        GrooveGxlHelper.createNodeWithName(START_NODE_ID, startNodeName, graph);

        Map<String, String> nodeLabels = new HashMap<>();
        nodeLabels.put(START_NODE_ID, startNodeName);
        GrooveGxlHelper.layoutGraph(graph, nodeLabels);

        File startGraphFile = new File(targetFolder.getPath() + START_GST);
        GxlToXMLConverter.toXml(gxl, startGraphFile);
    }
}
