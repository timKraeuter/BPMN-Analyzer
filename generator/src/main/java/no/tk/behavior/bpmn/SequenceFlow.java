package no.tk.behavior.bpmn;

public class SequenceFlow extends FlowElement {
  private final FlowNode source;
  private final FlowNode target;

  public SequenceFlow(String id, String name, FlowNode source, FlowNode target) {
    super(id, name);
    this.source = source;
    this.target = target;
  }

  /** Descriptive names might not be unique! Only the id is guaranteed to be unique. */
  public String getDescriptiveName() {
    if (getName().isEmpty()) {
      return String.format("%s -> %s", source.getName(), target.getName());
    }
    return getName();
  }

  public String getNameOrIDIfEmpty() {
    if (getName().isEmpty()) {
      return getId();
    }
    return getName();
  }

  public FlowNode getSource() {
    return source;
  }

  public FlowNode getTarget() {
    return target;
  }
}
