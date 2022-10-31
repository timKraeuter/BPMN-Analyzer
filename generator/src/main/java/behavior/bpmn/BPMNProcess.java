package behavior.bpmn;

import behavior.bpmn.auxiliary.visitors.AbstractProcessVisitor;
import behavior.bpmn.auxiliary.visitors.CallActivityFlowNodeVisitor;
import behavior.bpmn.events.StartEvent;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/** Represents a process modeled in BPMN. */
public class BPMNProcess extends AbstractBPMNProcess {
  private final Set<StartEvent> startEvents;

  public BPMNProcess(
      String name,
      Set<StartEvent> startEvents,
      Set<SequenceFlow> sequenceFlows,
      Set<FlowNode> flowNodes,
      Set<BPMNEventSubprocess> eventSubprocesses) {
    super(name, sequenceFlows, flowNodes, eventSubprocesses);
    this.startEvents = startEvents;
  }

  public Set<StartEvent> getStartEvents() {
    return this.startEvents;
  }

  public Stream<BPMNProcess> getSubProcesses() {
    final LinkedHashSet<BPMNProcess> subProcesses = new LinkedHashSet<>();
    getFlowNodes()
        .forEach(
            flowNode ->
                flowNode.accept(
                    new CallActivityFlowNodeVisitor(
                        callActivity -> {
                          subProcesses.add(callActivity.getSubProcessModel());
                          subProcesses.addAll(
                              callActivity
                                  .getSubProcessModel()
                                  .getSubProcesses()
                                  .collect(Collectors.toList()));
                        })));
    return subProcesses.stream();
  }

  @Override
  public void accept(AbstractProcessVisitor visitor) {
    visitor.handle(this);
  }

  @Override
  public boolean isEventSubprocess() {
    return false;
  }

  /*
  According to the BPMN spec the following consistency rules exist:
  - Gateways or Activities without incoming sequence flows are forbidden (p426)
   */
}
