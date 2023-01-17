package behavior.bpmn;

import behavior.bpmn.activities.CallActivity;
import behavior.bpmn.auxiliary.visitors.AbstractProcessVisitor;
import behavior.bpmn.auxiliary.visitors.CallActivityFlowNodeVisitor;
import behavior.bpmn.events.StartEvent;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Represents a process model in BPMN. Gateways or Activities without incoming sequence flows are
 * forbidden (p426).
 */
public class BPMNProcess extends AbstractBPMNProcess {
  private final Set<StartEvent> startEvents;
  private CallActivity callActivity;

  public BPMNProcess(
      String name,
      Set<SequenceFlow> sequenceFlows,
      Set<FlowNode> flowNodes,
      Set<BPMNEventSubprocess> eventSubprocesses,
      Set<StartEvent> startEvents) {
    super(name, sequenceFlows, flowNodes, eventSubprocesses);
    this.startEvents = startEvents;
    this.callActivity = null;
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
                        callAct -> {
                          subProcesses.add(callAct.getSubProcessModel());
                          subProcesses.addAll(
                              callAct
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

  public CallActivity getCallActivityIfExists() {
    return callActivity;
  }

  public void setCallActivity(CallActivity callActivity) {
    this.callActivity = callActivity;
  }
}
