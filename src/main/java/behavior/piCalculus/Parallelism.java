package behavior.piCalculus;

import java.util.LinkedHashSet;
import java.util.Set;

public class Parallelism implements PiProcess {
    private final Set<PiProcess> parallelProcesses;

    public Parallelism(Set<PiProcess> parallelProcesses) {
        this.parallelProcesses = new LinkedHashSet<>(parallelProcesses);
    }
}
