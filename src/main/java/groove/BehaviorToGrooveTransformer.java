package groove;

import behavior.Behavior;
import behavior.BehaviorVisitor;
import behavior.fsm.FiniteStateMachine;
import behavior.petriNet.PetriNet;
import behavior.petriNet.Place;
import groove.gxl.Graph;
import groove.gxl.Gxl;
import groove.gxl.Node;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicLong;

public class BehaviorToGrooveTransformer {

    public static final String TOKEN_NODE_NAME = "Token";
    public static final String TOKEN_EDGE_NAME = "token";

    void generateGrooveGrammar(Behavior behavior, File targetFolder) {
        behavior.handle(new BehaviorVisitor() {
            @Override
            public void accept(FiniteStateMachine finiteStateMachine) {
                BehaviorToGrooveTransformer.this.generateGrooveGrammarForFSM(finiteStateMachine, targetFolder);
            }

            @Override
            public void accept(PetriNet petriNet) {
                BehaviorToGrooveTransformer.this.generateGrooveGrammarForPN(petriNet, targetFolder);
            }
        });
    }

    private void generateGrooveGrammarForPN(PetriNet petriNet, File grooveDir) {
        File graphGrammarSubFolder = this.makeSubFolder(petriNet, grooveDir);

        // Generate start graph
        this.generatePNStartGraphFile(petriNet, graphGrammarSubFolder);

        // Generate rules
        this.generatePNRules(petriNet, graphGrammarSubFolder);

        this.generatePropertiesFile(graphGrammarSubFolder);
    }

    private void generatePNStartGraphFile(PetriNet petriNet, File graphGrammarSubFolder) {
        Gxl gxl = new Gxl();
        Graph graph = GrooveGxlHelper.createStandardGxlGraph("start", gxl);
        AtomicLong idCounter = new AtomicLong(-1);
        petriNet.getPlaces().forEach(place -> {
            Node placeNode = GrooveGxlHelper.createNodeWithName(
                    this.getNodeId(idCounter),
                    place.getName(),
                    graph);
            // Create and link start tokens for each place.
            if (place.getStartTokenAmount() > 0) {
                for (int i = 0; i < place.getStartTokenAmount(); i++) {
                    Node tokenNode = GrooveGxlHelper.createNodeWithName(
                            this.getNodeId(idCounter),
                            TOKEN_NODE_NAME,
                            graph);
                    GrooveGxlHelper.createEdgeWithName(graph, placeNode, tokenNode, TOKEN_EDGE_NAME);
                }
            }
        });

        File startGraphFile = new File(graphGrammarSubFolder.getPath() + "/start.gst");
        GxlToXMLConverter.toXml(gxl, startGraphFile);
    }

    private void generatePNRules(PetriNet petriNet, File subFolder) {
        GrooveRuleGenerator ruleGenerator = new GrooveRuleGenerator();
        petriNet.getTransitions().forEach(transition -> {
            ruleGenerator.startRule(transition.getName());

            transition.getIncomingEdges().forEach(weigthPlacePair -> {
                Place place = weigthPlacePair.getRight();
                Integer weight = weigthPlacePair.getLeft();
                GrooveNode placeNode = ruleGenerator.contextNode(place.getName());
                for (int i = 0; i < weight; i++) {
                    GrooveNode toBeDeletedTokenNode = ruleGenerator.deleteNode(TOKEN_NODE_NAME);
                    ruleGenerator.deleteEdge(TOKEN_EDGE_NAME, placeNode, toBeDeletedTokenNode);
                }
            });

            transition.getOutgoingEdges().forEach(weigthPlacePair -> {
                Place place = weigthPlacePair.getRight();
                Integer weight = weigthPlacePair.getLeft();
                GrooveNode placeNode = ruleGenerator.contextNode(place.getName());
                for (int i = 0; i < weight; i++) {
                    GrooveNode toBeAddedTokenNode = ruleGenerator.addNode(TOKEN_NODE_NAME);
                    ruleGenerator.addEdge(TOKEN_EDGE_NAME, placeNode, toBeAddedTokenNode);
                }
            });

            ruleGenerator.generateRule();
        });
        ruleGenerator.writeRules(subFolder);
    }

    private String getNodeId(AtomicLong idCounter) {
        return "n" + idCounter.incrementAndGet();
    }

    private void generateGrooveGrammarForFSM(FiniteStateMachine finiteStateMachine, File grooveDir) {
        File graphGrammarSubFolder = this.makeSubFolder(finiteStateMachine, grooveDir);

        this.generateFSMStartGraphFile(finiteStateMachine, graphGrammarSubFolder);

        this.generateFSMRules(finiteStateMachine, graphGrammarSubFolder);

        this.generatePropertiesFile(graphGrammarSubFolder);
    }

    private File makeSubFolder(Behavior finiteStateMachine, File grooveDir) {
        File graphGrammarSubFolder = new File(grooveDir + "/" + finiteStateMachine.getName() + ".gps");
        graphGrammarSubFolder.mkdir();
        return graphGrammarSubFolder;
    }

    private void generateFSMRules(FiniteStateMachine finiteStateMachine, File subFolder) {
        GrooveRuleGenerator ruleGenerator = new GrooveRuleGenerator();
        finiteStateMachine.getTransitions().forEach(transition -> {
            ruleGenerator.startRule(transition.getName());

            ruleGenerator.deleteNode(transition.getSource().getName());
            ruleGenerator.addNode(transition.getTarget().getName());

            ruleGenerator.generateRule();
        });
        ruleGenerator.writeRules(subFolder);
    }

    private void generatePropertiesFile(File subFolder) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();

        String propertiesContent = String.format("# %s (Groove rule generator)\n" +
                        "location=%s\n" +
                        "startGraph=start\n" +
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

    private void generateFSMStartGraphFile(FiniteStateMachine finiteStateMachine, File targetFolder) {
        Gxl gxl = new Gxl();
        Graph graph = GrooveGxlHelper.createStandardGxlGraph("start", gxl);
        GrooveGxlHelper.createNodeWithName("n0", finiteStateMachine.getStartState().getName(), graph);

        File startGraphFile = new File(targetFolder.getPath() + "/start.gst");
        GxlToXMLConverter.toXml(gxl, startGraphFile);
    }
}
