package groove.graph;

import api.Graph;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.BinaryOperator;
import java.util.stream.Stream;

/**
 * Represents a graph which is used to generate a graph grammar for the tool groove.
 */
public class GrooveGraph implements Graph<GrooveNode, GrooveEdge> {

    private final String name;
    private final Set<GrooveNode> nodes;
    private final Set<GrooveEdge> edges;

    public GrooveGraph(String name, Set<GrooveNode> nodes, Set<GrooveEdge> edges) {
        this.name = name;
        this.nodes = new LinkedHashSet<>(nodes);
        this.edges = new LinkedHashSet<>(edges);
    }

    @Override
    public Stream<GrooveNode> nodes() {
        return this.nodes.stream();
    }

    @Override
    public Stream<GrooveEdge> edges() {
        return this.edges.stream();
    }

    public String getName() {
        return this.name;
    }

    /**
     * Creates a new graph which is the union of the other two graphs.
     * Union of nodes.
     * Union of edges.
     * Does not check if nodes have distinct names (we assume the graphs are prefixed using the name)!
     */
    public GrooveGraph union(GrooveGraph graph, BinaryOperator<String> nameResolver) {
        assert !this.name.equals(graph.name);

        Set<GrooveNode> unionNodes = new LinkedHashSet<>();
        Set<GrooveEdge> unionEdges = new LinkedHashSet<>();

        unionNodes.addAll(this.nodes);
        unionNodes.addAll(graph.nodes);
        unionEdges.addAll(this.edges);
        unionEdges.addAll(graph.edges);

        return new GrooveGraph(nameResolver.apply(this.getName(), graph.getName()), unionNodes, unionEdges);
    }
}
