package no.tk.behavior.bpmn.auxiliary;

import no.tk.behavior.bpmn.BPMNEventSubprocess;
import no.tk.behavior.bpmn.FlowNode;
import no.tk.behavior.bpmn.events.StartEvent;
import java.util.Set;

public interface BPMNModelBuilder {
  BPMNModelBuilder sequenceFlow(String id, String name, FlowNode from, FlowNode to);

  BPMNModelBuilder sequenceFlow(String id, FlowNode from, FlowNode to);

  BPMNModelBuilder flowNode(FlowNode flowNode);

  Set<StartEvent> getStartEvents();

  BPMNModelBuilder startEvent(StartEvent startEvent);

  BPMNModelBuilder eventSubprocess(BPMNEventSubprocess eventSubprocess);
}
