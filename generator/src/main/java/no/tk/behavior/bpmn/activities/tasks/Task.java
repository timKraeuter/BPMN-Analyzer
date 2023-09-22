package no.tk.behavior.bpmn.activities.tasks;

import no.tk.behavior.bpmn.auxiliary.visitors.ActivityVisitor;
import no.tk.behavior.bpmn.auxiliary.visitors.FlowElementVisitor;
import no.tk.behavior.bpmn.auxiliary.visitors.FlowNodeVisitor;

/** Represents a Task in BPMN where the task type is not specified. */
public class Task extends AbstractTask {
  public Task(String id, String name) {
    super(id, name);
  }

  @Override
  public void accept(FlowElementVisitor visitor) {
    visitor.handle(this);
  }

  @Override
  public void accept(ActivityVisitor visitor) {
    visitor.handle(this);
  }

  @Override
  public void accept(FlowNodeVisitor visitor) {
    visitor.handle(this);
  }

  @Override
  public boolean isInstantiateFlowNode() {
    return false;
  }
}
