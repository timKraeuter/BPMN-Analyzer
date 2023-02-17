package no.hvl.tk.rulegenerator.server.endpoint.dtos;

import java.util.ArrayList;
import java.util.List;

public class BPMNSpecificPropertyCheckingResponse {
  private final List<BPMNPropertyCheckingResult> propertyCheckingResults;

  public BPMNSpecificPropertyCheckingResponse() {
    this.propertyCheckingResults = new ArrayList<>();
  }

  public void addPropertyCheckingResult(BPMNPropertyCheckingResult result) {
    this.propertyCheckingResults.add(result);
  }

  public List<BPMNPropertyCheckingResult> getPropertyCheckingResults() {
    return propertyCheckingResults;
  }

  public void sortResults() {
    this.propertyCheckingResults.sort(BPMNPropertyCheckingResult::compareTo);
  }
}
