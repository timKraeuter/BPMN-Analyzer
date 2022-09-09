package no.hvl.tk.rulegenerator.server.endpoint.dtos;

import java.util.ArrayList;
import java.util.List;

public class ModelCheckingResponse {
    private final List<BPMNPropertyCheckingResult> propertyCheckingResults;
    public ModelCheckingResponse() {
        this.propertyCheckingResults = new ArrayList<>();
    }

    public void addPropertyCheckingResult(BPMNPropertyCheckingResult result) {
        this.propertyCheckingResults.add(result);
    }

    public List<BPMNPropertyCheckingResult> getPropertyCheckingResults() {
        return propertyCheckingResults;
    }
}
