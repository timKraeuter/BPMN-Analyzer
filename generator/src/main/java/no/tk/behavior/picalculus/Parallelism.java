package no.tk.behavior.picalculus;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class Parallelism implements PiProcess {
  private final Set<PiProcess> parallelProcesses;

  public Parallelism(List<PiProcess> parallelProcesses) {
    this.parallelProcesses = new LinkedHashSet<>(parallelProcesses);
  }

  @Override
  public <T> T accept(PiProcessVisitor<T> visitor) {
    return visitor.handle(this);
  }

  @Override
  public boolean isEmptySum() {
    return false;
  }

  public PiProcess getFirst() {
    assert parallelProcesses.size() == 2;
    return parallelProcesses.iterator().next();
  }

  public PiProcess getSecond() {
    assert parallelProcesses.size() == 2;
    final Iterator<PiProcess> iterator = parallelProcesses.iterator();
    iterator.next();
    return iterator.next();
  }
}
