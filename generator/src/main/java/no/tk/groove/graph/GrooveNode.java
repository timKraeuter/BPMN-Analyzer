package no.tk.groove.graph;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import no.tk.api.Node;

public class GrooveNode implements Node {
  public static final AtomicLong idCounter = new AtomicLong(-1);
  private final String id;
  private final String name;
  private final Set<String> flags;
  private final Map<String, GrooveValue> attributes;

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

  public Map<String, GrooveValue> getAttributes() {
    return this.attributes;
  }

  public void addAttribute(String name, String value) {
    this.attributes.put(name, new GrooveValue("string", String.format("\"%s\"", value)));
  }

  public void addAttribute(String name, int value) {
    this.attributes.put(name, new GrooveValue("int", String.valueOf(value)));
  }

  public void addAttribute(String name, boolean value) {
    this.attributes.put(name, new GrooveValue("bool", String.valueOf(value)));
  }

  @Override
  public String toString() {
    return this.name;
  }
}
