package no.tk.api;

import no.tk.groove.graph.GrooveNode;

public interface GraphRuleGenerator {

  void startRule(String ruleName);

  /** Define that the current rule needs a node in the context with the given name. */
  Node contextNode(String name);

  /** Define that the current rule adds a node with the given name. */
  Node addNode(String nodeName);

  /** Define that the current rule deletes a node with the given name. */
  Node deleteNode(String nodeName);

  /** Define that the current rule is not applicable if a node with the given name exists (NAC). */
  GrooveNode nacNode(String nodeName);

  /** Define that the current rule adds an edge between the two given nodes. */
  void addEdge(String name, Node source, Node target);

  /**
   * Define that the current rule deletes an edge between two nodes (The nodes must be in context,
   * added or deleted).
   */
  void deleteEdge(String name, Node source, Node target);

  /**
   * Define that the current rule needs an edge between two nodes (The nodes must be in context,
   * added or deleted).
   */
  void contextEdge(String name, GrooveNode source, GrooveNode target);

  GraphRule buildRule();
}
