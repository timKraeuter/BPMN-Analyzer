package no.tk.behavior.bpmn.reader;

import org.camunda.bpm.model.bpmn.instance.*;

public interface EventDefinitionVisitor<T> {

  T handle(MessageEventDefinition evDefinition);

  T handle(LinkEventDefinition evDefinition);

  T handle(SignalEventDefinition evDefinition);

  T handle(TerminateEventDefinition evDefinition);

  T handle(TimerEventDefinition evDefinition);

  T handle(ErrorEventDefinition evDefinition);

  T handle(EscalationEventDefinition evDefinition);
}
