package groove.behaviortransformer.bpmn.generators;

import behavior.bpmn.AbstractProcess;
import behavior.bpmn.activities.tasks.ReceiveTask;
import behavior.bpmn.activities.tasks.SendTask;
import behavior.bpmn.activities.tasks.Task;

public interface BPMNTaskRuleGenerator {
    void createTaskRulesForProcess(AbstractProcess process, Task task);

    void createSendTaskRulesForProcess(AbstractProcess process, SendTask sendTask);

    void createReceiveTaskRulesForProcess(AbstractProcess process, ReceiveTask receiveTask);
}
