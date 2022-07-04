package groove.graph;

import api.Node;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

public class GrooveNode implements Node {
    public static final AtomicLong idCounter = new AtomicLong(-1);

    private final String id;
    private final String name;
    private final Set<String> flags;
    private final Map<String, GrooveValue<?>> attributes;

    public GrooveNode(String name) {
        this.id = getNextNodeId();
        this.name = name;
        this.flags = new LinkedHashSet<>();
        this.attributes = new LinkedHashMap<>();
    }

    public static String getNextNodeId() {
        return "n" + idCounter.incrementAndGet();
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

    public Map<String, GrooveValue<?>> getAttributes() {
        return this.attributes;
    }

    public void addAttribute(String name, String value) {
        this.attributes.put(name, new StringGrooveValue(value));
    }

    public void addAttribute(String name, int value) {
        this.attributes.put(name, new IntGrooveValue(value));
    }

    public void addAttribute(String name, boolean value) {
        this.attributes.put(name, new BooleanGrooveValue(value));
    }

    @Override
    public String toString() {
        return this.name;
    }
}
