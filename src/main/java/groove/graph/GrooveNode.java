package groove.graph;

import api.Node;

import java.util.concurrent.atomic.AtomicLong;

public class GrooveNode implements Node {
    public static final AtomicLong idCounter = new AtomicLong(-1);

    private final String id = "n" + idCounter.incrementAndGet();
    private final String name;

    public GrooveNode(String name) {
        this.name = name;
    }

    @Override
    public String getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    @Override
    public String toString() {
        return "GrooveNode{" +
                "id='" + this.id + '\'' +
                ", name='" + this.name + '\'' +
                '}';
    }
}
