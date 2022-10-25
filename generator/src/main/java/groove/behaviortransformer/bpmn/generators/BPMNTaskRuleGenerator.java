package groove.behaviortransformer.bpmn.generators;

import behavior.bpmn.AbstractBPMNProcess;
import behavior.bpmn.activities.tasks.ReceiveTask;
import behavior.bpmn.activities.tasks.SendTask;
import behavior.bpmn.activities.tasks.Task;

public interface BPMNTaskRuleGenerator {
    void createTaskRulesForProcess(AbstractBPMNProcess process, Task task);

    void createSendTaskRulesForProcess(AbstractBPMNProcess process, SendTask sendTask);

    void createReceiveTaskRulesForProcess(AbstractBPMNProcess process, ReceiveTask receiveTask);
}
