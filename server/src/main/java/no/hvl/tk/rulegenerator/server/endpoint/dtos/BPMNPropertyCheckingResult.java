package no.hvl.tk.rulegenerator.server.endpoint.dtos;

import java.util.Objects;

public class BPMNPropertyCheckingResult implements Comparable<BPMNPropertyCheckingResult> {
  private final BPMNSpecificProperty name;
  private final boolean holds;
  private final String additionalInfo;

  public BPMNPropertyCheckingResult(
      BPMNSpecificProperty name, boolean holds, String additionalInfo) {
    this.name = name;
    this.holds = holds;
    this.additionalInfo = additionalInfo;
  }

  public BPMNSpecificProperty getName() {
    return name;
  }

  public boolean isHolds() {
    return holds;
  }

  public String getAdditionalInfo() {
    return additionalInfo;
  }

  @Override
  public int compareTo(BPMNPropertyCheckingResult o) {
    return Integer.compare(this.getName().getOrdering(), o.getName().getOrdering());
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof BPMNPropertyCheckingResult)) {
      return false;
    }
    BPMNPropertyCheckingResult that = (BPMNPropertyCheckingResult) o;
    return holds == that.holds && name == that.name && Objects.equals(additionalInfo,
        that.additionalInfo);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, holds, additionalInfo);
  }
}
