package behavior.bpmn.activities;

import behavior.bpmn.FlowNode;

public abstract class Activity extends FlowNode {

    public Activity(String name) {
        super(name);
    }

    @Override
    public boolean isInclusiveGateway() {
        return false;
    }

    @Override
    public boolean isGateway() {
        return false;
    }

    @Override
    public boolean isExclusiveEventBasedGateway() {
        return false;
    }
}
