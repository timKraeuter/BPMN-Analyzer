package behavior.petrinet;

import org.apache.commons.lang3.tuple.Pair;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class Transition {
    private final String name;
    private final Set<Pair<Integer, Place>> outgoingEdges;
    private final Set<Pair<Integer, Place>> incomingEdges;

    public Transition(String name) {
        this.name = name;
        this.incomingEdges = new LinkedHashSet<>();
        this.outgoingEdges = new LinkedHashSet<>();
    }

    public String getName() {
        return this.name;
    }

    public Transition addOutgoingEdge(Place to, Integer weight) {
        this.outgoingEdges.add(Pair.of(weight, to));
        return this;
    }

    public Transition addOutgoingEdge(Place to) {
        return this.addOutgoingEdge(to, 1);
    }

    public Transition addIncomingEdge(Place to, Integer weight) {
        this.incomingEdges.add(Pair.of(weight, to));
        return this;
    }

    public Transition addIncomingEdge(Place to) {
        return this.addIncomingEdge(to, 1);
    }

    public Set<Pair<Integer, Place>> getOutgoingEdges() {
        return new LinkedHashSet<>(this.outgoingEdges);
    }

    public Set<Pair<Integer, Place>> getIncomingEdges() {
        return new LinkedHashSet<>(this.incomingEdges);
    }

    public Set<Place> getNextPlaces() {
        return this.outgoingEdges.stream()
                                 .map(Pair::getValue)
                                 .collect(Collectors.toSet());
    }

    public Set<Place> getPreviousPlaces() {
        return this.incomingEdges.stream()
                                 .map(Pair::getValue)
                                 .collect(Collectors.toSet());
    }
}
