package no.tk.behavior.activity.edges;

import no.tk.behavior.activity.nodes.ActivityNode;
import no.tk.behavior.activity.variables.BooleanVariable;

public class ControlFlow extends ActivityEdge {
  private final BooleanVariable guard;

  public ControlFlow(String name, ActivityNode source, ActivityNode target, BooleanVariable guard) {
    super(name, source, target);
    this.guard = guard;
  }

  public BooleanVariable getGuardIfExists() {
    return this.guard;
  }
}
