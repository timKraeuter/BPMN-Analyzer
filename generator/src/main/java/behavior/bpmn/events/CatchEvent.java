package behavior.bpmn.events;

import behavior.bpmn.events.definitions.EventDefinition;

public abstract class CatchEvent extends Event {
  protected CatchEvent(String id, String name, EventDefinition eventDefinition) {
    super(id, name, eventDefinition);
  }
}
