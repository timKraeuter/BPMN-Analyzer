package no.tk.behavior.bpmn;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Stream;
import no.tk.behavior.bpmn.auxiliary.visitors.AbstractProcessVisitor;
import no.tk.behavior.bpmn.auxiliary.visitors.CallActivityFlowNodeVisitor;

public abstract class AbstractBPMNProcess {
  private final String name;
  private final Set<SequenceFlow> sequenceFlows;
  private final Set<FlowNode> flowNodes;
  private final Set<BPMNEventSubprocess> eventSubprocesses;

  protected AbstractBPMNProcess(
      String name,
      Set<SequenceFlow> sequenceFlows,
      Set<FlowNode> flowNodes,
      Set<BPMNEventSubprocess> eventSubprocesses) {
    this.name = name;
    this.sequenceFlows = sequenceFlows;
    this.flowNodes = flowNodes;
    this.eventSubprocesses = eventSubprocesses;
  }

  public Stream<BPMNEventSubprocess> eventSubprocesses() {
    return eventSubprocesses.stream();
  }

  public Stream<SequenceFlow> sequenceFlows() {
    return sequenceFlows.stream();
  }

  public Stream<FlowNode> flowNodes() {
    return flowNodes.stream();
  }

  public Stream<BPMNProcess> allSubProcesses() {
    final LinkedHashSet<BPMNProcess> subProcesses = new LinkedHashSet<>();
    flowNodes()
        .forEach(
            flowNode ->
                flowNode.accept(
                    new CallActivityFlowNodeVisitor(
                        callAct -> {
                          subProcesses.add(callAct.getSubProcessModel());
                          subProcesses.addAll(
                              callAct.getSubProcessModel().allSubProcesses().toList());
                        })));
    return subProcesses.stream();
  }

  public String getName() {
    return name;
  }

  public abstract void accept(AbstractProcessVisitor visitor);

  public abstract boolean isEventSubprocess();
}
