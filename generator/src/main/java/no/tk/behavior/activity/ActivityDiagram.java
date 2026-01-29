package no.tk.behavior.activity;

import java.util.Set;
import java.util.stream.Stream;
import no.tk.behavior.Behavior;
import no.tk.behavior.BehaviorVisitor;
import no.tk.behavior.activity.edges.ControlFlow;
import no.tk.behavior.activity.nodes.ActivityNode;
import no.tk.behavior.activity.nodes.InitialNode;
import no.tk.behavior.activity.variables.Variable;

/** Represents an activity diagram as described in the TTC 2015 Model Execution Case. */
public class ActivityDiagram implements Behavior {
  private final String name;

  private final InitialNode initialNode;
  private final Set<ActivityNode> nodes;
  private final Set<ControlFlow> edges;

  private final Set<Variable<?>> inputVariables;
  private final Set<Variable<?>> localVariables;

  public ActivityDiagram(
      String name,
      InitialNode initialNode,
      Set<Variable<?>> inputVariables,
      Set<Variable<?>> localVariables,
      Set<ActivityNode> nodes,
      Set<ControlFlow> edges) {
    this.name = name;
    this.initialNode = initialNode;
    this.inputVariables = inputVariables;
    this.localVariables = localVariables;
    this.nodes = nodes;
    this.edges = edges;
  }

  public InitialNode getInitialNode() {
    return this.initialNode;
  }

  public Stream<ActivityNode> getNodes() {
    return this.nodes.stream();
  }

  @Override
  public String getName() {
    return this.name;
  }

  @Override
  public void accept(BehaviorVisitor visitor) {
    visitor.handle(this);
  }

  @SuppressWarnings("java:S1452") // Wildcard needed for Variable with different Value types
  public Stream<Variable<?>> inputVariables() {
    return this.inputVariables.stream();
  }

  @SuppressWarnings("java:S1452") // Wildcard needed for Variable with different Value types
  public Stream<Variable<?>> localVariables() {
    return this.localVariables.stream();
  }
}
