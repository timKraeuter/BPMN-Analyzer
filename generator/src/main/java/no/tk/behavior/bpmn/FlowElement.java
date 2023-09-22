package no.tk.behavior.bpmn;

import com.google.common.base.Objects;

public abstract class FlowElement {
  private final String id;
  private final String name;

  protected FlowElement(String id, String name) {
    this.id = id;
    this.name = name;
  }

  public String getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public String getNameOrIDIfEmpty() {
    if (getName().isEmpty()) {
      return getId();
    }
    return getName();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof FlowElement that)) {
      return false;
    }
    return getId().equals(that.getId());
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(getId());
  }

  @Override
  public String toString() {
    return String.format("%s (%s)", name, id);
  }
}
