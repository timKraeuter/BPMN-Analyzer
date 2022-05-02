package no.hvl.tk.ruleGenerator.server.endpoint.dtos;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ModelCheckingResponse {
    Map<ModelCheckingProperty, Boolean> propertyCheckingResults;
    Set<String> deadActivities;

    public ModelCheckingResponse() {
        this.propertyCheckingResults = new HashMap<>();
        this.deadActivities = new HashSet<>();
    }

    // Getters needed for request mapping.
    public Map<ModelCheckingProperty, Boolean> getPropertyCheckingResults() {
        return propertyCheckingResults;
    }

    public Set<String> getDeadActivities() {
        return deadActivities;
    }

    public void setPropertyCheckingResult(ModelCheckingProperty property, Boolean propertyFulfilled) {
        propertyCheckingResults.put(property, propertyFulfilled);
    }

    public void addDeadActivity(String activityNameOrId) {
        deadActivities.add(activityNameOrId);
    }
}
