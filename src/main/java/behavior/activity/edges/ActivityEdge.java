package behavior.activity.edges;

import behavior.activity.nodes.ActivityNode;

public abstract class ActivityEdge {
    private final String name;

    private final ActivityNode source;
    private final ActivityNode target;

    protected ActivityEdge(String name, ActivityNode source, ActivityNode target) {
        this.name = name;
        this.source = source;
        this.target = target;
    }
}
