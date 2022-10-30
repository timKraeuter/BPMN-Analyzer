package behavior.bpmn;

public class SequenceFlow extends FlowElement {
  private final FlowNode source;
  private final FlowNode target;

  public SequenceFlow(String id, String name, FlowNode source, FlowNode target) {
    super(id, name);
    this.source = source;
    this.target = target;
  }

  /** Descriptive names might bot be unique! Only the id is guaranteed to be unique. */
  public String getDescriptiveName() {
    if (getName().isEmpty()) {
      return String.format("%s_%s", source.getName(), target.getName());
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
