package behavior.activity;

import behavior.Behavior;
import behavior.BehaviorVisitor;
import behavior.activity.edges.ActivityEdge;
import behavior.activity.nodes.ActivityNode;
import behavior.activity.nodes.InitialNode;
import behavior.activity.values.Value;
import behavior.activity.variables.Variable;

import java.util.Set;

/**
 * Represents an activity diagram as described in the TTC 2015 Model Execution Case.
 */
public class ActivityDiagram implements Behavior {
    private final String name;

    private final InitialNode initialNode;
    private final Set<ActivityNode> nodes;
    private final Set<ActivityEdge> edges;

    private final Set<Variable<? extends Value>> inputVariables;
    private final Set<Variable<? extends Value>> localVariables;

    public ActivityDiagram(String name,
                           InitialNode initialNode,
                           Set<Variable<? extends Value>> inputVariables,
                           Set<Variable<? extends Value>> localVariables,
                           Set<ActivityNode> nodes,
                           Set<ActivityEdge> edges) {
        this.name = name;
        this.initialNode = initialNode;
        this.inputVariables = inputVariables;
        this.localVariables = localVariables;
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
