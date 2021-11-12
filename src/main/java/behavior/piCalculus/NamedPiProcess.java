package behavior.piCalculus;

import behavior.Behavior;
import behavior.BehaviorVisitor;

/**
 * Acts as a wrapper with a name for PiProcess.
 */
public class NamedPiProcess implements Behavior {
    private final String name;
    private final PiProcess process;

    public NamedPiProcess(String name, PiProcess process) {
        this.name = name;
        this.process = process;
    }

    @Override
    public String getName() {
        return this.name;
    }

    public PiProcess getProcess() {
        return this.process;
    }

    @Override
    public void accept(BehaviorVisitor visitor) {
        visitor.handle(this);
    }
}
