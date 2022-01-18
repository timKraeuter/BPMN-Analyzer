package behavior.bpmn.activities;

import behavior.bpmn.auxiliary.FlowNodeVisitor;

// TODO: Split up into: ReceiveTask, SendTask and ServiceTask.
// Currently not interesting but part of the standard: UserTask, ManualTask, ScriptTask and BusinessRuleTask.
// Possible task markers: loop, multi-instance and compensation (fig. 10.9)
public class Task extends Activity {
    public Task(String name) {
        super(name);
    }

    @Override
    public void accept(FlowNodeVisitor visitor) {
        visitor.handle(this);
    }

    @Override
    public boolean isTask() {
        return true;
    }
}
