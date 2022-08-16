package behavior.bpmn.auxiliary.visitors;

import behavior.bpmn.EventSubprocess;
import behavior.bpmn.Process;

public interface AbstractProcessVisitor {
    void handle(EventSubprocess eventSubprocess);

    void handle(Process process);
}
