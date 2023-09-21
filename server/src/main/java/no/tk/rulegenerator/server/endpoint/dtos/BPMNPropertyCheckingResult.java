package no.tk.rulegenerator.server.endpoint.dtos;

import java.util.Objects;

public class BPMNPropertyCheckingResult implements Comparable<BPMNPropertyCheckingResult> {
  private final BPMNSpecificProperty name;
  private final boolean valid;
  private final String additionalInfo;

  public BPMNPropertyCheckingResult(
      BPMNSpecificProperty name, boolean valid, String additionalInfo) {
    this.name = name;
    this.valid = valid;
    this.additionalInfo = additionalInfo;
  }

  public BPMNSpecificProperty getName() {
    return name;
  }

  public boolean isValid() {
    return valid;
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
    return valid == that.valid
        && name == that.name
        && Objects.equals(additionalInfo, that.additionalInfo);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, valid, additionalInfo);
  }
}
