package behavior.petriNet;

import behavior.Behavior;
import behavior.BehaviorVisitor;

import java.util.LinkedHashSet;
import java.util.Set;

public class PetriNet implements Behavior {
    private final String name;
    private final Set<Place> places;
    private final Set<Transition> transitions;

    public PetriNet(String name) {
        this.name = name;
        this.places = new LinkedHashSet<>();
        this.transitions = new LinkedHashSet<>();
    }

    private void addPlace(Place place) {
        this.places.add(place);
    }

    public void addTransition(Transition transition) {
        transition.getPreviousPlaces().forEach(this::addPlace);
        this.transitions.add(transition);
        transition.getNextPlaces().forEach(this::addPlace);
    }

    public Set<Place> getPlaces() {
        return new LinkedHashSet<>(this.places);
    }

    public Set<Transition> getTransitions() {
        return new LinkedHashSet<>(this.transitions);
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public void handle(BehaviorVisitor visitor) {
        visitor.accept(this);
    }
}
