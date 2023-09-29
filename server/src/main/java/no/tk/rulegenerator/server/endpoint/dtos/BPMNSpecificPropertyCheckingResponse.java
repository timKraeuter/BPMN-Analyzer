package no.tk.rulegenerator.server.endpoint.dtos;

import java.util.List;

public record BPMNSpecificPropertyCheckingResponse(
    List<BPMNPropertyCheckingResult> propertyCheckingResults) {

  public void addPropertyCheckingResult(BPMNPropertyCheckingResult result) {
    this.propertyCheckingResults.add(result);
  }

  public void sortResults() {
    propertyCheckingResults.sort(BPMNPropertyCheckingResult::compareTo);
  }
}
