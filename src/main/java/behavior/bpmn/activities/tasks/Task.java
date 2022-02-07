package behavior.bpmn.activities.tasks;

import behavior.bpmn.auxiliary.FlowNodeVisitor;

/**
 * Represents a Task in BPMN where the task type is not specified.
 */
public class Task extends AbstractTask {
    public Task(String name) {
        super(name);
    }

    @Override
    public void accept(FlowNodeVisitor visitor) {
        visitor.handle(this);
    }

    @Override
    public boolean isInstantiateFlowNode() {
        return false;
    }
}
