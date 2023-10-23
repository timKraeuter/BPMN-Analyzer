package no.tk.groove.behaviortransformer;

import io.github.timkraeuter.groove.graph.GrooveEdge;
import io.github.timkraeuter.groove.graph.GrooveGraph;
import io.github.timkraeuter.groove.graph.GrooveNode;
import io.github.timkraeuter.groove.rule.GrooveGraphRule;
import io.github.timkraeuter.groove.rule.GrooveRuleBuilder;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Stream;
import no.tk.behavior.petrinet.PetriNet;
import no.tk.behavior.petrinet.Place;

public class PNToGrooveTransformer implements GrooveTransformer<PetriNet> {

  private static final String TOKEN_NODE_NAME = "Token";
  private static final String TOKEN_EDGE_NAME = "token";
  private final boolean layout;

  public PNToGrooveTransformer(boolean layout) {
    this.layout = layout;
  }

  @Override
  public GrooveGraph generateStartGraph(PetriNet petriNet) {
    Set<GrooveNode> nodes = new LinkedHashSet<>();
    Set<GrooveEdge> edges = new LinkedHashSet<>();

    // Create a node for each place of the petri net.
    petriNet
        .getPlaces()
        .forEach(
            place -> {
              GrooveNode placeNode = new GrooveNode(place.getName());
              nodes.add(placeNode);
              // Create and link start tokens for each place.
              for (int i = 0; i < place.getStartTokenAmount(); i++) {
                GrooveNode tokenNode = new GrooveNode(TOKEN_NODE_NAME);
                nodes.add(tokenNode);

                GrooveEdge tokenEdge = new GrooveEdge(TOKEN_EDGE_NAME, placeNode, tokenNode);
                edges.add(tokenEdge);
              }
            });
    return new GrooveGraph(petriNet.getName(), nodes, edges);
  }

  @Override
  public Stream<GrooveGraphRule> generateRules(PetriNet petriNet) {
    GrooveRuleBuilder ruleBuilder = new GrooveRuleBuilder();
    petriNet
        .getTransitions()
        .forEach(
            transition -> {
              ruleBuilder.startRule(transition.getName());

              transition
                  .getIncomingEdges()
                  .forEach(
                      weigthPlacePair -> {
                        Place place = weigthPlacePair.getRight();
                        Integer weight = weigthPlacePair.getLeft();
                        GrooveNode placeNode = ruleBuilder.contextNode(place.getName());
                        for (int i = 0; i < weight; i++) {
                          GrooveNode toBeDeletedTokenNode = ruleBuilder.deleteNode(TOKEN_NODE_NAME);
                          ruleBuilder.deleteEdge(TOKEN_EDGE_NAME, placeNode, toBeDeletedTokenNode);
                        }
                      });

              transition
                  .getOutgoingEdges()
                  .forEach(
                      weigthPlacePair -> {
                        Place place = weigthPlacePair.getRight();
                        Integer weight = weigthPlacePair.getLeft();
                        GrooveNode placeNode = ruleBuilder.contextNode(place.getName());
                        for (int i = 0; i < weight; i++) {
                          GrooveNode toBeAddedTokenNode = ruleBuilder.addNode(TOKEN_NODE_NAME);
                          ruleBuilder.addEdge(TOKEN_EDGE_NAME, placeNode, toBeAddedTokenNode);
                        }
                      });

              ruleBuilder.buildRule();
            });
    return ruleBuilder.getRules();
  }

  @Override
  public boolean isLayoutActivated() {
    return layout;
  }
}
