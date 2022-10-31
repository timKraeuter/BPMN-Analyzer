package no.hvl.tk.rulegenerator.server.endpoint.dtos;

public class BPMNPropertyCheckingResult {
  private final ModelCheckingProperty name;
  private final boolean holds;
  private final String additionalInfo;

  public BPMNPropertyCheckingResult(
      ModelCheckingProperty name, boolean holds, String additionalInfo) {
    this.name = name;
    this.holds = holds;
    this.additionalInfo = additionalInfo;
  }

  public ModelCheckingProperty getName() {
    return name;
  }

  public boolean isHolds() {
    return holds;
  }

  public String getAdditionalInfo() {
    return additionalInfo;
  }
}
