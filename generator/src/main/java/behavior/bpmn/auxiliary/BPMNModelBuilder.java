package behavior.bpmn.auxiliary;

import behavior.bpmn.EventSubprocess;
import behavior.bpmn.FlowNode;
import behavior.bpmn.events.StartEvent;

import java.util.Set;

public interface BPMNModelBuilder {
    BPMNModelBuilder sequenceFlow(String id, String name, FlowNode from, FlowNode to);

    BPMNModelBuilder sequenceFlow(String id, FlowNode from, FlowNode to);

    BPMNModelBuilder flowNode(FlowNode flowNode);

    Set<StartEvent> getStartEvents();

    BPMNModelBuilder startEvent(StartEvent startEvent);

    BPMNModelBuilder eventSubprocess(EventSubprocess eventSubprocess);
}
