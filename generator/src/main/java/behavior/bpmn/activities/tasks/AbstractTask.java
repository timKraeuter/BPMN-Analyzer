package behavior.bpmn.activities.tasks;

import behavior.bpmn.activities.Activity;

// Possible task markers: loop, multi-instance and compensation (fig. 10.9)
public abstract class AbstractTask extends Activity {

    protected AbstractTask(String name) {
        super(name);
    }

    @Override
    public boolean isTask() {
        return true;
    }
}
