package behavior.bpmn.activities.tasks;

import behavior.bpmn.auxiliary.FlowNodeVisitor;
import com.google.common.base.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Task that = (Task) o;
        return getName().equals(that.getName()) && getBoundaryEvents().equals(that.getBoundaryEvents());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getName(), getBoundaryEvents());
    }
}
