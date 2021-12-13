package groove.behaviorTransformer;

import behavior.petriNet.PetriNet;
import behavior.petriNet.Place;
import groove.graph.GrooveEdge;
import groove.graph.GrooveGraph;
import groove.graph.GrooveNode;
import groove.graph.GrooveRuleGenerator;

import java.util.LinkedHashSet;
import java.util.Set;

public class PNToGrooveTransformer implements GrooveTransformer<PetriNet> {

    private static final String TOKEN_NODE_NAME = "Token";
    private static final String TOKEN_EDGE_NAME = "token";

    @Override
    public GrooveGraph generateStartGraph(PetriNet petriNet, boolean addPrefix) {
        String potentialPrefix = GrooveRuleGenerator.getPotentialPrefix(petriNet, addPrefix);

        Set<GrooveNode> nodes = new LinkedHashSet<>();
        Set<GrooveEdge> edges = new LinkedHashSet<>();

        // Create a node for each place of the petri net.
        petriNet.getPlaces().forEach(place -> {
            GrooveNode placeNode = new GrooveNode(potentialPrefix + place.getName());
            nodes.add(placeNode);
            // Create and link start tokens for each place.
            for (int i = 0; i < place.getStartTokenAmount(); i++) {
                GrooveNode tokenNode = new GrooveNode(potentialPrefix + TOKEN_NODE_NAME);
                nodes.add(tokenNode);

                GrooveEdge tokenEdge = new GrooveEdge(potentialPrefix + TOKEN_EDGE_NAME, placeNode, tokenNode);
                edges.add(tokenEdge);
            }
        });
        return new GrooveGraph(petriNet.getName(), nodes, edges);
    }

    @Override
    public GrooveRuleGenerator generateRules(PetriNet petriNet, boolean addPrefix) {
        GrooveRuleGenerator ruleGenerator = new GrooveRuleGenerator(petriNet, addPrefix);
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
        return ruleGenerator;
    }
}
