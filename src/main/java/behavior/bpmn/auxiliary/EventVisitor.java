package behavior.bpmn.auxiliary;

import behavior.bpmn.events.EndEvent;
import behavior.bpmn.events.IntermediateCatchEvent;
import behavior.bpmn.events.IntermediateThrowEvent;
import behavior.bpmn.events.StartEvent;

public interface EventVisitor {

    void handle(StartEvent startEvent);

    void handle(IntermediateThrowEvent intermediateThrowEvent);

    void handle(IntermediateCatchEvent intermediateCatchEvent);

    void handle(EndEvent endEvent);
}
