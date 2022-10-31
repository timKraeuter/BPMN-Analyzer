package groove.graph;

import behavior.bpmn.auxiliary.exceptions.GrooveGenerationRuntimeException;
import java.util.LinkedHashSet;
import java.util.Set;

public class GrooveGraphBuilder {

  private String name;
  private final Set<GrooveNode> nodes;
  private final Set<GrooveEdge> edges;

  public GrooveGraphBuilder() {
    this.nodes = new LinkedHashSet<>();
    this.edges = new LinkedHashSet<>();
  }

  public GrooveGraphBuilder setName(String name) {
    this.name = name;
    return this;
  }

  public GrooveGraphBuilder addNode(GrooveNode node) {
    if (node == null) {
      throw new GrooveGenerationRuntimeException("Node must not be null!");
    }
    this.nodes.add(node);
    return this;
  }

  public GrooveGraphBuilder addEdge(String name, GrooveNode source, GrooveNode target) {
    this.addNode(source).addNode(target);
    this.edges.add(new GrooveEdge(name, source, target));
    return this;
  }

  public GrooveGraph build() {
    return new GrooveGraph(this.name, this.nodes, this.edges);
  }
}
