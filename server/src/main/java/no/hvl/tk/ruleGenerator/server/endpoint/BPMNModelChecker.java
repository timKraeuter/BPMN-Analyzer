package no.hvl.tk.ruleGenerator.server.endpoint;

import no.hvl.tk.ruleGenerator.server.endpoint.dtos.ModelCheckingProperty;
import no.hvl.tk.ruleGenerator.server.endpoint.dtos.ModelCheckingRequest;
import no.hvl.tk.ruleGenerator.server.endpoint.dtos.ModelCheckingResponse;

import java.io.File;

public class BPMNModelChecker {
    private final File graphGrammarDir;

    public BPMNModelChecker(File graphGrammarDir) {
        this.graphGrammarDir = graphGrammarDir;
    }

    public ModelCheckingResponse runModelChecking(ModelCheckingRequest modelCheckingRequest) {
        System.out.println(modelCheckingRequest.getPropertiesToBeChecked());
        ModelCheckingResponse response = new ModelCheckingResponse();
        response.setPropertyCheckingResult(ModelCheckingProperty.NO_DEAD_ACTIVITIES, true);
        modelCheckingRequest.getPropertiesToBeChecked().forEach(modelCheckingProperty -> this.checkPropertyAndRecordResult(
                modelCheckingProperty,
                response));
        return response;
    }

    private void checkPropertyAndRecordResult(ModelCheckingProperty modelCheckingProperty,
                                              ModelCheckingResponse response) {
        switch (modelCheckingProperty) {
            case NO_DEAD_ACTIVITIES:
                // TODO: Implement checking.
                response.setPropertyCheckingResult(modelCheckingProperty, true);
                break;
            // Not supported atm.
            case SAFENESS:
                break;
            case OPTION_TO_COMPLETE:
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + modelCheckingProperty);
        }
    }
}
