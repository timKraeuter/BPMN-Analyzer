package no.hvl.tk.ruleGenerator.server.endpoint.dtos;

import java.util.*;

public class ModelCheckingResponse {
    List<BPMNPropertyCheckingResult> propertyCheckingResults;
    public ModelCheckingResponse() {
        this.propertyCheckingResults = new ArrayList<>();
    }

    public void addPropertyCheckingResult(BPMNPropertyCheckingResult result) {
        this.propertyCheckingResults.add(result);
    }

    public List<BPMNPropertyCheckingResult> getPropertyCheckingResults() {
        return propertyCheckingResults;
    }

    public void setPropertyCheckingResults(List<BPMNPropertyCheckingResult> propertyCheckingResults) {
        this.propertyCheckingResults = propertyCheckingResults;
    }
}
