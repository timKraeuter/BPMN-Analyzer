package behavior.bpmn.gateways;

import behavior.bpmn.auxiliary.FlowNodeVisitor;
import com.google.common.base.Objects;

/**
 * Represents and exclusive event based gateway (parallel ones are currently not supported).
 */
public class EventBasedGateway extends Gateway {

    // TODO: Implement instantiate exclusive gateway.
    private final boolean instantiate;

    public EventBasedGateway(String name) {
        this(name, false);
    }

    public EventBasedGateway(String name, boolean instantiate) {
        super(name);
        this.instantiate = instantiate;
    }

    @Override
    public void accept(FlowNodeVisitor visitor) {
        visitor.handle(this);
    }

    @Override
    public boolean isInclusiveGateway() {
        return false;
    }

    @Override
    public boolean isInstantiateFlowNode() {
        return instantiate;
    }

    @Override
    public boolean isExclusiveEventBasedGateway() {
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        EventBasedGateway that = (EventBasedGateway) o;
        return getName().equals(that.getName()) && instantiate == that.instantiate;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getName(), instantiate);
    }
}
