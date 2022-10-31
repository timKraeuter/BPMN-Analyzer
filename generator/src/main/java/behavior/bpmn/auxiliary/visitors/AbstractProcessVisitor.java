package behavior.bpmn.auxiliary.visitors;

import behavior.bpmn.BPMNEventSubprocess;
import behavior.bpmn.BPMNProcess;

public interface AbstractProcessVisitor {
  void handle(BPMNEventSubprocess eventSubprocess);

  void handle(BPMNProcess process);
}
