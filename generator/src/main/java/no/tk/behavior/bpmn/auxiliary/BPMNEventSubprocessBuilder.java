package no.tk.behavior.bpmn.auxiliary;

import no.tk.behavior.bpmn.BPMNEventSubprocess;
import no.tk.behavior.bpmn.FlowNode;
import no.tk.behavior.bpmn.SequenceFlow;
import no.tk.behavior.bpmn.events.StartEvent;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

public class BPMNEventSubprocessBuilder implements BPMNModelBuilder {
  private final Set<SequenceFlow> sequenceFlows;
  private final Set<FlowNode> flowNodes;
  private final Set<BPMNEventSubprocess> eventSubprocesses;
  private String name;

  public BPMNEventSubprocessBuilder() {
    this.sequenceFlows = new LinkedHashSet<>();
    flowNodes = new LinkedHashSet<>();
    eventSubprocesses = new LinkedHashSet<>();
  }

  public BPMNEventSubprocessBuilder name(String name) {
    this.name = name;
    return this;
  }

  @Override
  public BPMNEventSubprocessBuilder sequenceFlow(
      String id, String name, FlowNode from, FlowNode to) {
    final SequenceFlow sequenceFlow = new SequenceFlow(id, name, from, to);
    this.sequenceFlows.add(sequenceFlow);
    from.addOutgoingSequenceFlow(sequenceFlow);
    to.addIncomingSequenceFlow(sequenceFlow);
    return this;
  }

  @Override
  public BPMNEventSubprocessBuilder sequenceFlow(String id, FlowNode from, FlowNode to) {
    return sequenceFlow(id, "", from, to);
  }

  @Override
  public BPMNModelBuilder flowNode(FlowNode flowNode) {
    flowNodes.add(flowNode);
    return this;
  }

  @Override
  public BPMNEventSubprocessBuilder eventSubprocess(BPMNEventSubprocess eventSubprocess) {
    eventSubprocesses.add(eventSubprocess);
    return this;
  }

  @Override
  public Set<StartEvent> getStartEvents() {
    return new HashSet<>();
  }

  @Override
  public BPMNEventSubprocessBuilder startEvent(StartEvent startEvent) {
    // NOOP since it is not needed.
    return this;
  }

  public BPMNEventSubprocess build() {
    return new BPMNEventSubprocess(name, sequenceFlows, flowNodes, eventSubprocesses);
  }
}
