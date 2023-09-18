package no.tk.behavior.bpmn.auxiliary.visitors;

import no.tk.behavior.bpmn.BPMNEventSubprocess;
import no.tk.behavior.bpmn.BPMNProcess;

public interface AbstractProcessVisitor {
  void handle(BPMNEventSubprocess eventSubprocess);

  void handle(BPMNProcess process);
}
