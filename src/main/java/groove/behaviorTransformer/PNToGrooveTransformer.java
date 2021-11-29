package groove.behaviorTransformer;

import behavior.petriNet.PetriNet;
import behavior.petriNet.Place;
import groove.GrooveGxlHelper;
import groove.GxlToXMLConverter;
import groove.graph.GrooveNode;
import groove.graph.GrooveRuleGenerator;
import groove.gxl.Graph;
import groove.gxl.Gxl;
import groove.gxl.Node;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import static groove.behaviorTransformer.BehaviorToGrooveTransformer.START;
import static groove.behaviorTransformer.BehaviorToGrooveTransformer.START_GST;

public class PNToGrooveTransformer {

    private static final String TOKEN_NODE_NAME = "Token";
    private static final String TOKEN_EDGE_NAME = "token";

    void generatePNStartGraphFile(PetriNet petriNet, File graphGrammarSubFolder) {
        Gxl gxl = new Gxl();
        Graph graph = GrooveGxlHelper.createStandardGxlGraph(START, gxl);
        AtomicLong idCounter = new AtomicLong(-1);
        Map<String, String> nodeLabels = new HashMap<>();

        petriNet.getPlaces().forEach(place -> {
            Node placeNode = GrooveGxlHelper.createNodeWithName(
                    this.getNodeId(idCounter),
                    place.getName(),
                    graph);
            nodeLabels.put(placeNode.getId(), place.getName());
            // Create and link start tokens for each place.
            if (place.getStartTokenAmount() > 0) {
                for (int i = 0; i < place.getStartTokenAmount(); i++) {
                    Node tokenNode = GrooveGxlHelper.createNodeWithName(
                            this.getNodeId(idCounter),
                            TOKEN_NODE_NAME,
                            graph);
                    nodeLabels.put(tokenNode.getId(), TOKEN_NODE_NAME);
                    GrooveGxlHelper.createEdgeWithName(graph, placeNode, tokenNode, TOKEN_EDGE_NAME);
                }
            }
        });
        GrooveGxlHelper.layoutGraph(graph, nodeLabels);

        File startGraphFile = new File(graphGrammarSubFolder.getPath() + START_GST);
        GxlToXMLConverter.toXml(gxl, startGraphFile);
    }

    private String getNodeId(AtomicLong idCounter) {
        return "n" + idCounter.incrementAndGet();
    }

    void generatePNRules(PetriNet petriNet, File subFolder) {
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
}
