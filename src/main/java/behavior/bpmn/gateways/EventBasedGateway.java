package behavior.bpmn.gateways;

import behavior.bpmn.auxiliary.FlowNodeVisitor;

/**
 * Represents and exclusive event based gateway (parallel ones are currently not supported).
 */
public class EventBasedGateway extends Gateway {

    // TODO: Implement instantiate exlusive gateway.
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
    public boolean isExclusiveEventBasedGateway() {
        return true;
    }

    public boolean isInstantiate() {
        return instantiate;
    }
}
