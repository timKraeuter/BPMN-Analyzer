package no.tk.behavior.bpmn.events;

import no.tk.behavior.bpmn.FlowNode;
import no.tk.behavior.bpmn.auxiliary.visitors.EventVisitor;
import no.tk.behavior.bpmn.events.definitions.EventDefinition;

public abstract class Event extends FlowNode {

  private final EventDefinition eventDefinition;

  protected Event(String id, String name, EventDefinition eventDefinition) {
    super(id, name);
    this.eventDefinition = eventDefinition;
  }

  public abstract void accept(EventVisitor eventVisitor);

  @Override
  public boolean isInclusiveGateway() {
    return false;
  }

  @Override
  public boolean isTask() {
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

  public EventDefinition getEventDefinition() {
    return eventDefinition;
  }
}
