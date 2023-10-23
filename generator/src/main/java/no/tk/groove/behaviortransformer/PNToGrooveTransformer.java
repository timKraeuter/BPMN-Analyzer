package no.tk.groove.behaviortransformer;

import io.github.timkraeuter.groove.graph.GrooveGraphBuilder;
import io.github.timkraeuter.groove.graph.GrooveNode;
import io.github.timkraeuter.groove.rule.GrooveRuleBuilder;
import no.tk.behavior.petrinet.PetriNet;
import no.tk.behavior.petrinet.Place;

public class PNToGrooveTransformer extends GrooveTransformer<PetriNet> {

  private static final String TOKEN_NODE_NAME = "Token";
  private static final String TOKEN_EDGE_NAME = "token";

  public PNToGrooveTransformer(boolean layout) {
    super(layout);
  }

  @Override
  public void generateStartGraph(PetriNet petriNet, GrooveGraphBuilder builder) {
    builder.name(petriNet.getName());

    // Create a node for each place of the petri net.
    petriNet
        .getPlaces()
        .forEach(
            place -> {
              GrooveNode placeNode = new GrooveNode(place.getName());
              builder.addNode(placeNode);
              // Create and link start tokens for each place.
              for (int i = 0; i < place.getStartTokenAmount(); i++) {
                GrooveNode tokenNode = new GrooveNode(TOKEN_NODE_NAME);
                builder.addNode(tokenNode);

                builder.addEdge(TOKEN_EDGE_NAME, placeNode, tokenNode);
              }
            });
  }

  @Override
  public void generateRules(PetriNet petriNet, GrooveRuleBuilder ruleBuilder) {
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
  }
}
