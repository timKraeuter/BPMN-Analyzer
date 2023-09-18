package no.tk.behavior.picalculus;

import java.util.*;

public class MultiarySum implements Sum {
  private final Set<Sum> sums;

  public MultiarySum(List<Sum> sums) {
    this.sums = new LinkedHashSet<>(sums);
  }

  @Override
  public <T> T accept(PiProcessVisitor<T> visitor) {
    return visitor.handle(this);
  }

  @Override
  public boolean isEmptySum() {
    return false;
  }

  public Sum getFirst() {
    assert this.sums.size() == 2;
    return this.sums.iterator().next();
  }

  public Sum getSecond() {
    assert this.sums.size() == 2;
    Iterator<Sum> iterator = this.sums.iterator();
    iterator.next(); // Ignore first.
    return iterator.next();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof MultiarySum)) return false;
    MultiarySum that = (MultiarySum) o;
    return Objects.equals(sums, that.sums);
  }

  @Override
  public int hashCode() {
    return Objects.hash(sums);
  }
}
