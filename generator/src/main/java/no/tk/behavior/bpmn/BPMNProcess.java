package no.tk.behavior.bpmn;

import java.util.Set;
import no.tk.behavior.bpmn.activities.CallActivity;
import no.tk.behavior.bpmn.auxiliary.visitors.AbstractProcessVisitor;
import no.tk.behavior.bpmn.events.StartEvent;

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
