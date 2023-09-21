package no.tk.behavior.bpmn.auxiliary;

import java.util.LinkedHashSet;
import java.util.Set;
import no.tk.behavior.bpmn.BPMNEventSubprocess;
import no.tk.behavior.bpmn.BPMNProcess;
import no.tk.behavior.bpmn.FlowNode;
import no.tk.behavior.bpmn.SequenceFlow;
import no.tk.behavior.bpmn.events.StartEvent;

public class BPMNProcessBuilder implements BPMNModelBuilder {
  private final Set<SequenceFlow> sequenceFlows;
  private final Set<FlowNode> flowNodes;
  private final Set<BPMNEventSubprocess> eventSubprocesses;
  private String name;
  private final Set<StartEvent> startEvents;

  public BPMNProcessBuilder() {
    this.sequenceFlows = new LinkedHashSet<>();
    eventSubprocesses = new LinkedHashSet<>();
    flowNodes = new LinkedHashSet<>();
    startEvents = new LinkedHashSet<>();
  }

  @Override
  public Set<StartEvent> getStartEvents() {
    return startEvents;
  }

  public Set<SequenceFlow> getSequenceFlows() {
    return sequenceFlows;
  }

  public Set<FlowNode> getFlowNodes() {
    return flowNodes;
  }

  public BPMNProcessBuilder name(String name) {
    this.name = name;
    return this;
  }

  @Override
  public BPMNProcessBuilder startEvent(StartEvent event) {
    this.startEvents.add(event);
    return this;
  }

  @Override
  public BPMNProcessBuilder eventSubprocess(BPMNEventSubprocess eventSubprocess) {
    eventSubprocesses.add(eventSubprocess);
    return this;
  }

  @Override
  public BPMNProcessBuilder sequenceFlow(String id, String name, FlowNode from, FlowNode to) {
    final SequenceFlow sequenceFlow = new SequenceFlow(id, name, from, to);
    this.sequenceFlows.add(sequenceFlow);
    from.addOutgoingSequenceFlow(sequenceFlow);
    to.addIncomingSequenceFlow(sequenceFlow);

    flowNodes.add(from);
    flowNodes.add(to);
    return this;
  }

  @Override
  public BPMNProcessBuilder sequenceFlow(String id, FlowNode from, FlowNode to) {
    return sequenceFlow(id, "", from, to);
  }

  @Override
  public BPMNModelBuilder flowNode(FlowNode flowNode) {
    flowNodes.add(flowNode);
    return this;
  }

  public BPMNProcess build() {
    return new BPMNProcess(name, sequenceFlows, flowNodes, eventSubprocesses, startEvents);
  }
}
