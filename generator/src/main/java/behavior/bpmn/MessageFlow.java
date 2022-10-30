package behavior.bpmn;

public class MessageFlow {
  private final String id;
  private final String name;
  private final FlowNode source;
  private final FlowNode target;

  public MessageFlow(String id, String name, FlowNode source, FlowNode target) {
    this.id = id;
    this.name = name;
    this.source = source;
    this.target = target;
  }

  public String getId() {
    return id;
  }

  public String getNameOrDescriptiveName() {
    if (name.isEmpty()) {
      return source.getName() + "_" + target.getName();
    }
    return name;
  }

  public FlowNode getSource() {
    return source;
  }

  public FlowNode getTarget() {
    return target;
  }
}
