package no.tk.behavior.bpmn.reader;

import org.camunda.bpm.model.bpmn.instance.ErrorEventDefinition;
import org.camunda.bpm.model.bpmn.instance.EscalationEventDefinition;
import org.camunda.bpm.model.bpmn.instance.LinkEventDefinition;
import org.camunda.bpm.model.bpmn.instance.MessageEventDefinition;
import org.camunda.bpm.model.bpmn.instance.SignalEventDefinition;
import org.camunda.bpm.model.bpmn.instance.TerminateEventDefinition;
import org.camunda.bpm.model.bpmn.instance.TimerEventDefinition;

public interface EventDefinitionVisitor<T> {

  T handle(MessageEventDefinition evDefinition);

  T handle(LinkEventDefinition evDefinition);

  T handle(SignalEventDefinition evDefinition);

  T handle(TerminateEventDefinition evDefinition);

  T handle(TimerEventDefinition evDefinition);

  T handle(ErrorEventDefinition evDefinition);

  T handle(EscalationEventDefinition evDefinition);
}
