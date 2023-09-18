package no.tk.behavior.bpmn.activities.tasks;

import no.tk.behavior.bpmn.activities.Activity;

// Possible task markers: loop, multi-instance and compensation (fig. 10.9)
public abstract class AbstractTask extends Activity {

  protected AbstractTask(String id, String name) {
    super(id, name);
  }

  @Override
  public boolean isTask() {
    return true;
  }
}
