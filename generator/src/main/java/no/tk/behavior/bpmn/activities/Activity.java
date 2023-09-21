package no.tk.behavior.bpmn.activities;

import java.util.LinkedHashSet;
import java.util.Set;
import no.tk.behavior.bpmn.FlowNode;
import no.tk.behavior.bpmn.auxiliary.visitors.ActivityVisitor;
import no.tk.behavior.bpmn.events.BoundaryEvent;

public abstract class Activity extends FlowNode {
  private final Set<BoundaryEvent> boundaryEvents;

  protected Activity(String id, String name) {
    super(id, name);
    boundaryEvents = new LinkedHashSet<>();
  }

  public abstract void accept(ActivityVisitor visitor);

  @Override
  public boolean isInclusiveGateway() {
    return false;
  }

  @Override
  public boolean isGateway() {
    return false;
  }

  @Override
  public boolean isExclusiveEventBasedGateway() {
    return false;
  }

  public void attachBoundaryEvent(BoundaryEvent boundaryEvent) {
    if (boundaryEvent.getAttachedTo() != null) {
      throw new IllegalArgumentException(
          String.format(
              "The boundary event %s cannot be attached to two activities! It is already attached to %s.",
              boundaryEvent.getName(), boundaryEvent.getAttachedTo().getName()));
    }
    boundaryEvents.add(boundaryEvent);
    boundaryEvent.setAttachedTo(this);
  }

  public Set<BoundaryEvent> getBoundaryEvents() {
    return boundaryEvents;
  }
}
