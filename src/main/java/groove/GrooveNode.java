package groove;

import api.Node;

import java.util.concurrent.atomic.AtomicLong;

public class GrooveNode implements Node {
    private final static AtomicLong idCounter = new AtomicLong(-1);

    private final String id = "n" + idCounter.incrementAndGet();
    private final String name;

    public GrooveNode(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }
}
