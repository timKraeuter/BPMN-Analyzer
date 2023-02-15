package no.hvl.tk.rulegenerator.server.endpoint.dtos;

public class BPMNPropertyCheckingResult {
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
}
