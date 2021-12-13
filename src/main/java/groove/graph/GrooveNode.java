package groove.graph;

import api.Node;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

public class GrooveNode implements Node {
    public static final AtomicLong idCounter = new AtomicLong(-1);

    private final String id = "n" + idCounter.incrementAndGet();
    private final String name;
    private final Set<String> flags;

    public GrooveNode(String name) {
        this.name = name;
        this.flags = new LinkedHashSet<>();
    }

    @Override
    public String getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public Set<String> getFlags() {
        return this.flags;
    }

    public void addFlag(String flag) {
        this.flags.add(flag);
    }
}
