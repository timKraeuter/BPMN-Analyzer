package behavior.bpmn.reader;

import org.camunda.bpm.model.bpmn.instance.LinkEventDefinition;
import org.camunda.bpm.model.bpmn.instance.MessageEventDefinition;
import org.camunda.bpm.model.bpmn.instance.SignalEventDefinition;
import org.camunda.bpm.model.bpmn.instance.TerminateEventDefinition;

public interface EventDefinitionVisitor<T> {

    T handle(MessageEventDefinition evDefinition);

    T handle(LinkEventDefinition evDefinition);

    T handle(SignalEventDefinition evDefinition);

    T handle(TerminateEventDefinition evDefinition);
}
