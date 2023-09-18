package no.tk.groove.behaviortransformer.bpmn.generators;

import no.tk.behavior.bpmn.AbstractBPMNProcess;
import no.tk.behavior.bpmn.activities.tasks.ReceiveTask;
import no.tk.behavior.bpmn.activities.tasks.SendTask;
import no.tk.behavior.bpmn.activities.tasks.Task;

public interface BPMNTaskRuleGenerator {
  void createTaskRulesForProcess(AbstractBPMNProcess process, Task task);

  void createSendTaskRulesForProcess(AbstractBPMNProcess process, SendTask sendTask);

  void createReceiveTaskRulesForProcess(AbstractBPMNProcess process, ReceiveTask receiveTask);
}
