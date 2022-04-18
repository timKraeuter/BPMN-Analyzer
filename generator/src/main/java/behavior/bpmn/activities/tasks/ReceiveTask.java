package behavior.bpmn.activities.tasks;

import behavior.bpmn.auxiliary.FlowNodeVisitor;
import com.google.common.base.Objects;

/**
 * Represents a ReceiveTask.
 * Inheritance hierarchy similar to the BPMN spec (Fig.10.10).
 */
public class ReceiveTask extends AbstractTask {
    private final boolean instantiate;

    public ReceiveTask(String name) {
        this(name, false);
    }

    public ReceiveTask(String name, boolean instantiate) {
        super(name);
        this.instantiate = instantiate;
    }

    public boolean isInstantiate() {
        return instantiate;
    }

    @Override
    public void accept(FlowNodeVisitor visitor) {
        visitor.handle(this);
    }

    @Override
    public boolean isInstantiateFlowNode() {
        return isInstantiate();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ReceiveTask that = (ReceiveTask) o;
        return getName().equals(that.getName()) && instantiate == that.instantiate;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getName(), instantiate);
    }
}
