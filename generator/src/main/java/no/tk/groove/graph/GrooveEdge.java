package no.tk.groove.graph;

import java.util.concurrent.atomic.AtomicLong;
import no.tk.api.Edge;

public class GrooveEdge implements Edge {
  private static final AtomicLong idCounter = new AtomicLong(-1);
  private final String id = Long.toString(idCounter.incrementAndGet());

  private final String name;
  private final GrooveNode sourceNode;
  private final GrooveNode targetNode;

  public GrooveEdge(String name, GrooveNode sourceNode, GrooveNode targetNode) {
    this.name = name;
    this.sourceNode = sourceNode;
    this.targetNode = targetNode;
  }

  public String getId() {
    return this.id;
  }

  public String getName() {
    return this.name;
  }

  public GrooveNode getSourceNode() {
    return this.sourceNode;
  }

  public GrooveNode getTargetNode() {
    return this.targetNode;
  }
}
