package no.tk.rulegenerator.server.endpoint.dtos;

public record BPMNPropertyCheckingResult(
    BPMNSpecificProperty name, boolean valid, String additionalInfo)
    implements Comparable<BPMNPropertyCheckingResult> {

  @Override
  public int compareTo(BPMNPropertyCheckingResult o) {
    return Integer.compare(this.name().getOrdering(), o.name().getOrdering());
  }
}
