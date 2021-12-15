package behavior.activity;

import behavior.Behavior;
import behavior.BehaviorVisitor;
import behavior.activity.edges.ActivityEdge;
import behavior.activity.nodes.ActivityNode;
import behavior.activity.values.Value;
import behavior.activity.variables.Variable;

import java.util.Set;

/**
 * Represents an activity diagram as described in the TTC 2015 Model Execution Case.
 */
public class ActivityDiagram implements Behavior {
    private final String name;
    private final Set<Variable<? extends Value>> inputs;
    private final Set<Variable<? extends Value>> locals;
    private final Set<ActivityNode> nodes;
    private final Set<ActivityEdge> edges;

    public ActivityDiagram(String name,
                           Set<Variable<? extends Value>> inputs,
                           Set<Variable<? extends Value>> locals,
                           Set<ActivityNode> nodes,
                           Set<ActivityEdge> edges) {
        this.name = name;
        this.inputs = inputs;
        this.locals = locals;
        this.nodes = nodes;
        this.edges = edges;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public void accept(BehaviorVisitor visitor) {
        visitor.handle(this);
    }
}
