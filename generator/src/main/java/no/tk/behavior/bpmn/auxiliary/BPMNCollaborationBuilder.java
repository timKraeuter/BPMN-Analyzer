package no.tk.behavior.bpmn.auxiliary;

import java.util.LinkedHashSet;
import java.util.Set;
import no.tk.behavior.bpmn.*;
import no.tk.behavior.bpmn.auxiliary.visitors.CallActivityFlowNodeVisitor;
import no.tk.behavior.bpmn.events.StartEvent;

public class BPMNCollaborationBuilder implements BPMNModelBuilder {
  private final Set<MessageFlow> messageFlows;
  private final Set<BPMNProcess> participants;
  private final Set<BPMNProcess> subprocesses;
  private String name;

  private BPMNProcessBuilder currentProcessBuilder;

  public BPMNCollaborationBuilder() {
    messageFlows = new LinkedHashSet<>();
    participants = new LinkedHashSet<>();
    subprocesses = new LinkedHashSet<>();

    currentProcessBuilder = new BPMNProcessBuilder();
  }

  @Override
  public Set<StartEvent> getStartEvents() {
    return currentProcessBuilder.getStartEvents();
  }

  public BPMNCollaborationBuilder name(String name) {
    this.name = name;
    return this;
  }

  public BPMNCollaborationBuilder processName(String processName) {
    currentProcessBuilder.name(processName);
    return this;
  }

  @Override
  public BPMNCollaborationBuilder startEvent(StartEvent event) {
    currentProcessBuilder.startEvent(event);
    return this;
  }

  @Override
  public BPMNCollaborationBuilder eventSubprocess(BPMNEventSubprocess eventSubprocess) {
    currentProcessBuilder.eventSubprocess(eventSubprocess);
    return this;
  }

  @Override
  public BPMNCollaborationBuilder sequenceFlow(String id, String name, FlowNode from, FlowNode to) {
    currentProcessBuilder.sequenceFlow(id, name, from, to);

    this.findAndAddSubProcessIfPresent(from);
    this.findAndAddSubProcessIfPresent(to);
    return this;
  }

  private void findAndAddSubProcessIfPresent(FlowNode flowNode) {
    flowNode.accept(
        new CallActivityFlowNodeVisitor(
            callActivity -> {
              BPMNProcess subProcessModel = callActivity.getSubProcessModel();
              if (!subprocesses.contains(subProcessModel)
                  || !participants.contains(subProcessModel)) {
                subprocesses.add(subProcessModel);
                subProcessModel
                    .getFlowNodes()
                    .forEach(BPMNCollaborationBuilder.this::findAndAddSubProcessIfPresent);
              }
            }));
  }

  @Override
  public BPMNCollaborationBuilder sequenceFlow(String id, FlowNode from, FlowNode to) {
    return sequenceFlow(id, "", from, to);
  }

  @Override
  public BPMNModelBuilder flowNode(FlowNode flowNode) {
    currentProcessBuilder.flowNode(flowNode);
    this.findAndAddSubProcessIfPresent(flowNode);
    return this;
  }

  public BPMNCollaborationBuilder buildProcess() {
    this.participants.add(currentProcessBuilder.build());
    currentProcessBuilder = new BPMNProcessBuilder();
    return this;
  }

  public BPMNCollaborationBuilder messageFlow(String id, FlowNode source, FlowNode target) {
    return messageFlow(id, "", source, target);
  }

  public BPMNCollaborationBuilder messageFlow(
      String id, String name, FlowNode source, FlowNode target) {
    this.messageFlows.add(new MessageFlow(id, name, source, target));
    return this;
  }

  public BPMNCollaboration build() {
    if (!currentProcessBuilder.getStartEvents().isEmpty()
        || !currentProcessBuilder.getFlowNodes().isEmpty()
        || !currentProcessBuilder.getSequenceFlows().isEmpty()) {
      this.buildProcess();
    }
    return new BPMNCollaboration(name, participants, subprocesses, messageFlows);
  }
}
