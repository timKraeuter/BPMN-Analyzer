package no.tk.behavior.bpmn.auxiliary.visitors;

import no.tk.behavior.bpmn.activities.CallActivity;
import no.tk.behavior.bpmn.activities.tasks.ReceiveTask;
import no.tk.behavior.bpmn.activities.tasks.SendTask;
import no.tk.behavior.bpmn.activities.tasks.Task;

public interface ActivityVisitor {
  void handle(Task task);

  void handle(SendTask task);

  void handle(ReceiveTask task);

  void handle(CallActivity callActivity);
}
