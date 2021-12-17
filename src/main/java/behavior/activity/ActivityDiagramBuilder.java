package behavior.activity;

import behavior.activity.edges.ActivityEdge;
import behavior.activity.edges.ControlFlow;
import behavior.activity.nodes.ActivityNode;
import behavior.activity.nodes.InitialNode;
import behavior.activity.values.Value;
import behavior.activity.variables.Variable;

import java.util.LinkedHashSet;
import java.util.Set;

public class ActivityDiagramBuilder {
    private String name;
    private final Set<Variable<? extends Value>> inputVariables = new LinkedHashSet<>();
    private final Set<Variable<? extends Value>> localVariables = new LinkedHashSet<>();
    private final Set<ActivityNode> nodes = new LinkedHashSet<>();
    private final Set<ActivityEdge> edges = new LinkedHashSet<>();
    private InitialNode initialNode;

    public ActivityDiagram build() {
        return new ActivityDiagram(this.name, this.initialNode, this.inputVariables, this.localVariables, this.nodes, this.edges);
    }

    public ActivityDiagramBuilder setName(String name) {
        this.name = name;
        return this;
    }

    public ActivityDiagramBuilder addInputVariable(Variable<? extends Value> inputVar) {
        this.inputVariables.add(inputVar);
        return this;
    }

    public ActivityDiagramBuilder addLocalVariable(Variable<? extends Value> localVar) {
        this.localVariables.add(localVar);
        return this;
    }

    public ActivityDiagramBuilder setInitialNode(InitialNode initialNode) {
        this.initialNode = initialNode;
        return this;
    }

    public ActivityDiagramBuilder addNode(ActivityNode node) {
        this.nodes.add(node);
        return this;
    }

    public ActivityDiagramBuilder createControlFlow(String name, ActivityNode source, ActivityNode target) {
        this.addNode(source);
        this.addNode(target);
        ControlFlow controlFlow = new ControlFlow(name, source, target, null);
        this.edges.add(controlFlow);
        source.addOutgoingControlFlow(controlFlow);
        target.addIncomingControlFlow(controlFlow);
        return this;
    }
}
