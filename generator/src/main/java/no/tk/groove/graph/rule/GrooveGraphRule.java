package no.tk.groove.graph.rule;

import java.util.*;
import no.tk.api.GraphRule;
import no.tk.behavior.bpmn.auxiliary.exceptions.GrooveGenerationRuntimeException;
import no.tk.groove.graph.GrooveEdge;
import no.tk.groove.graph.GrooveNode;

public class GrooveGraphRule implements GraphRule {
  private final String ruleName;

  private final Map<String, GrooveNode> nodesToBeAdded;
  private final Map<String, GrooveNode> contextNodes;
  private final Map<String, GrooveNode> nodesToBeDeleted;
  private final Map<String, GrooveNode> notNodes;
  private final Map<String, GrooveEdge> edgesToBeAdded;
  private final Map<String, GrooveEdge> contextEdges;
  private final Map<String, GrooveEdge> edgesToBeDeleted;

  public GrooveGraphRule(String ruleName) {
    this.ruleName = ruleName;
    this.nodesToBeAdded = new LinkedHashMap<>();
    this.contextNodes = new LinkedHashMap<>();
    this.nodesToBeDeleted = new LinkedHashMap<>();
    this.edgesToBeAdded = new LinkedHashMap<>();
    this.contextEdges = new LinkedHashMap<>();
    this.edgesToBeDeleted = new LinkedHashMap<>();
    this.notNodes = new LinkedHashMap<>();
  }

  public void addNewNode(GrooveNode newNode) {
    this.checkIfNotAlreadyInContext(newNode);
    this.checkIfNotAlreadyDeleted(newNode);
    this.checkIfNotAlreadyInNot(newNode);
    this.nodesToBeAdded.put(newNode.getId(), newNode);
  }

  private void checkIfNotAlreadyDeleted(GrooveNode node) {
    if (this.nodesToBeDeleted.get(node.getId()) != null) {
      throw new GrooveGenerationRuntimeException(
          String.format("Node %s already contained as a to-be-deleted node!", node));
    }
  }

  private void checkIfNotAlreadyInContext(GrooveNode node) {
    if (this.contextNodes.get(node.getId()) != null) {
      throw new GrooveGenerationRuntimeException(
          String.format("Node %s already contained as a context node!", node));
    }
  }

  private void checkIfNotAlreadyInNot(GrooveNode node) {
    if (this.notNodes.get(node.getId()) != null) {
      throw new GrooveGenerationRuntimeException(
          String.format("Node %s already contained as a not node (NAC)!", node));
    }
  }

  private void checkIfNotAlreadyAdded(GrooveNode contextNode) {
    if (this.nodesToBeAdded.get(contextNode.getId()) != null) {
      throw new GrooveGenerationRuntimeException(
          String.format("Node %s already contained as a to-be-added node!", contextNode));
    }
  }

  public void addContextNode(GrooveNode contextNode) {
    this.checkIfNotAlreadyAdded(contextNode);
    this.checkIfNotAlreadyDeleted(contextNode);
    this.checkIfNotAlreadyInNot(contextNode);
    this.contextNodes.put(contextNode.getId(), contextNode);
  }

  public void addDelNode(GrooveNode deleteNode) {
    this.checkIfNotAlreadyAdded(deleteNode);
    this.checkIfNotAlreadyInContext(deleteNode);
    this.checkIfNotAlreadyInNot(deleteNode);
    this.nodesToBeDeleted.put(deleteNode.getId(), deleteNode);
  }

  public void addNotNode(GrooveNode notNode) {
    this.checkIfNotAlreadyAdded(notNode);
    this.checkIfNotAlreadyDeleted(notNode);
    this.checkIfNotAlreadyInContext(notNode);
    this.notNodes.put(notNode.getId(), notNode);
  }

  public void addNewEdge(GrooveEdge edge) {
    // It should be checked elsewhere that the source and target node are in the context of the rule
    // or added by the rule!
    this.edgesToBeAdded.put(edge.getId(), edge);
  }

  public void addDelEdge(GrooveEdge edge) {
    // It should be checked elsewhere that the source and target nodes are contained in the rule!
    this.edgesToBeDeleted.put(edge.getId(), edge);
  }

  public void addContextEdge(GrooveEdge edge) {
    // It should be checked elsewhere that the source and target nodes are contained in the rule!
    this.contextEdges.put(edge.getId(), edge);
  }

  public String getRuleName() {
    return this.ruleName;
  }

  public Set<GrooveNode> getNodesToBeAdded() {
    return new LinkedHashSet<>(this.nodesToBeAdded.values());
  }

  public Set<GrooveNode> getContextNodes() {
    return new LinkedHashSet<>(this.contextNodes.values());
  }

  public Set<GrooveNode> getNotNodes() {
    return new LinkedHashSet<>(this.notNodes.values());
  }

  public Set<GrooveEdge> getEdgesToBeAdded() {
    return new LinkedHashSet<>(this.edgesToBeAdded.values());
  }

  public Set<GrooveEdge> getEdgesToBeDeleted() {
    return new LinkedHashSet<>(this.edgesToBeDeleted.values());
  }

  public Set<GrooveEdge> getContextEdges() {
    return new LinkedHashSet<>(this.contextEdges.values());
  }

  public Map<String, GrooveNode> getContextAndAddedNodes() {
    Map<String, GrooveNode> addedAndContextNodes = new HashMap<>(this.nodesToBeAdded);
    addedAndContextNodes.putAll(this.contextNodes); // the maps are distinct, see add methods.
    return addedAndContextNodes;
  }

  public List<GrooveNode> getNodesToBeDeleted() {
    return new ArrayList<>(this.nodesToBeDeleted.values());
  }

  public Map<String, GrooveNode> getAllNodes() {
    Map<String, GrooveNode> allNodes = new LinkedHashMap<>(this.nodesToBeDeleted);
    allNodes.putAll(this.nodesToBeAdded);
    allNodes.putAll(this.contextNodes);
    allNodes.putAll(this.notNodes);
    return allNodes;
  }
}
