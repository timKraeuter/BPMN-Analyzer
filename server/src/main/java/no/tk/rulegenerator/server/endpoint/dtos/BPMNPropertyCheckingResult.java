package no.tk.rulegenerator.server.endpoint.dtos;

import java.util.Objects;

public record BPMNPropertyCheckingResult(BPMNSpecificProperty name, boolean valid, String additionalInfo) implements Comparable<BPMNPropertyCheckingResult> {

  @Override
  public int compareTo(BPMNPropertyCheckingResult o) {
    return Integer.compare(this.name().getOrdering(), o.name().getOrdering());
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
