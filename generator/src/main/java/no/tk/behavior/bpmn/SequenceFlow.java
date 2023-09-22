package no.tk.behavior.bpmn;

import no.tk.behavior.bpmn.auxiliary.visitors.FlowElementVisitor;

public class SequenceFlow extends FlowElement {
  private final FlowNode source;
  private final FlowNode target;

  public SequenceFlow(String id, String name, FlowNode source, FlowNode target) {
    super(id, name);
    this.source = source;
    this.target = target;
  }

  @Override
  public void accept(FlowElementVisitor visitor) {
    visitor.handle(this);
  }

  /** Descriptive names might not be unique! Only the id is guaranteed to be unique. */
  public String getDescriptiveName() {
    if (getName().isEmpty()) {
      return String.format("%s -> %s", source.getName(), target.getName());
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
