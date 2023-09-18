package no.tk.behavior.activity.nodes;

import no.tk.behavior.activity.edges.ControlFlow;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Stream;

public abstract class ActivityNode {
  private final String name;
  private final Set<ControlFlow> outgoingFlows = new LinkedHashSet<>();
  private final Set<ControlFlow> incomingFlows = new LinkedHashSet<>();

  protected ActivityNode(String name) {
    this.name = name;
  }

  public String getName() {
    return this.name;
  }

  public void addOutgoingControlFlow(ControlFlow outgoingFlow) {
    this.outgoingFlows.add(outgoingFlow);
  }

  public void addIncomingControlFlow(ControlFlow incomingFlow) {
    this.incomingFlows.add(incomingFlow);
  }

  public Stream<ControlFlow> getOutgoingFlows() {
    return this.outgoingFlows.stream();
  }

  public Stream<ControlFlow> getIncomingFlows() {
    return this.incomingFlows.stream();
  }

  public abstract void accept(ActivityNodeVisitor visitor);
}
