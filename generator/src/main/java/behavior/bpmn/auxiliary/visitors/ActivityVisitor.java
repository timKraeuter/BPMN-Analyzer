package behavior.bpmn.auxiliary.visitors;

import behavior.bpmn.activities.CallActivity;
import behavior.bpmn.activities.tasks.ReceiveTask;
import behavior.bpmn.activities.tasks.SendTask;
import behavior.bpmn.activities.tasks.Task;

public interface ActivityVisitor {
    void handle(Task task);

    void handle(SendTask task);

    void handle(ReceiveTask task);

    void handle(CallActivity callActivity);
}
