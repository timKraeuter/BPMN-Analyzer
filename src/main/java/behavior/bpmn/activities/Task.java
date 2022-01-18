package behavior.bpmn.activities;

import behavior.bpmn.auxiliary.FlowNodeVisitor;

// TODO: Split up into: ReceiveTask, SendTask and ServiceTask.
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
