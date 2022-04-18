package behavior.bpmn.auxiliary;

import behavior.bpmn.EventSubprocess;
import behavior.bpmn.FlowNode;
import behavior.bpmn.events.StartEvent;

public interface BPMNModelBuilder {
    BPMNModelBuilder sequenceFlow(String name, FlowNode from, FlowNode to);

    BPMNModelBuilder sequenceFlow(FlowNode from, FlowNode to);

    StartEvent getStartEvent();

    BPMNModelBuilder startEvent(StartEvent startEvent);

    BPMNModelBuilder eventSubprocess(EventSubprocess eventSubprocess);
}
