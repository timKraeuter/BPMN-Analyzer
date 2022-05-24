package no.hvl.tk.ruleGenerator.server.endpoint;

import behavior.bpmn.BPMNCollaboration;
import behavior.bpmn.FlowNode;
import behavior.bpmn.Process;
import no.hvl.tk.ruleGenerator.server.endpoint.dtos.BPMNPropertyCheckingResult;
import no.hvl.tk.ruleGenerator.server.endpoint.dtos.ModelCheckingProperty;
import no.hvl.tk.ruleGenerator.server.endpoint.dtos.ModelCheckingRequest;
import no.hvl.tk.ruleGenerator.server.endpoint.dtos.ModelCheckingResponse;
import util.GrooveRunner;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.StringJoiner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class BPMNModelChecker {
    private final File graphGrammarDir;
    private final BPMNCollaboration bpmnModel;

    public BPMNModelChecker(File graphGrammarDir, BPMNCollaboration bpmnModel) {
        this.graphGrammarDir = graphGrammarDir;
        this.bpmnModel = bpmnModel;
    }

    public ModelCheckingResponse runModelChecking(ModelCheckingRequest modelCheckingRequest) {
        ModelCheckingResponse response = new ModelCheckingResponse();

        modelCheckingRequest.getPropertiesToBeChecked().forEach(modelCheckingProperty -> this.checkPropertyAndRecordResult(
                modelCheckingProperty,
                response));
        return response;
    }

    private void checkPropertyAndRecordResult(ModelCheckingProperty modelCheckingProperty,
                                              ModelCheckingResponse response) {
        switch (modelCheckingProperty) {
            case NO_DEAD_ACTIVITIES:
                this.checkNoDeadActivities(response);
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

    private void checkNoDeadActivities(ModelCheckingResponse response) {
        try {
            // Generate state space for graph grammar.
            final GrooveRunner grooveRunner = new GrooveRunner();
            // TODO: Fix naming
            final String stateSpaceTempFile = RuleGeneratorControllerHelper.stateSpaceTempDir +
                                              graphGrammarDir.getName();
            grooveRunner.generateStateSpace(graphGrammarDir.getPath(), stateSpaceTempFile, true);

            // Read the file and find the executed activities
            final Set<String> executedActivities = findExecutedActivitiesInStateSpace(stateSpaceTempFile);

            // TODO: Not connected activities are not found due to the nature of my BPMN model.
            // Compare to all activities (needs BPMN model)
            final Set<String> allActivityNames = getAllActivityNames();
            allActivityNames.removeAll(executedActivities);

            if (allActivityNames.isEmpty()) {
                response.addPropertyCheckingResult(new BPMNPropertyCheckingResult(ModelCheckingProperty.NO_DEAD_ACTIVITIES,
                                                                                  true,
                                                                                  ""));
            } else {
                response.addPropertyCheckingResult(new BPMNPropertyCheckingResult(ModelCheckingProperty.NO_DEAD_ACTIVITIES,
                                                                                  false,
                                                                                  String.format("Dead activities: %s", String.join(",", allActivityNames))));
            }
        }
        catch (InterruptedException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Set<String> getAllActivityNames() {
        return this.bpmnModel.getParticipants().stream()
                             .flatMap(process -> getAllActivityNames(process).stream())
                             .collect(Collectors.toSet());
    }

    private Set<String> getAllActivityNames(Process process) {
        // Get all activities from subprocesses
        final Set<String> allActivityNames =
                process.getSubProcesses().flatMap(subprocess -> getAllActivityNames(subprocess).stream()).collect(
                Collectors.toSet());
        // TODO: Get all activities from event subprocesses

        process.getControlFlowNodes().filter(FlowNode::isTask)
                                     .map(FlowNode::getName)
                                     .forEach(allActivityNames::add);
        return allActivityNames;
    }

    private Set<String> findExecutedActivitiesInStateSpace(String stateSpaceTempFile) throws IOException {
        final Pattern regEx = Pattern.compile("<string>(.*)_start</string>");

        // Read the file in chunks if needed in the future!
        final String stateSpaceString = Files.readString(Path.of(stateSpaceTempFile));

        final Matcher matcher = regEx.matcher(stateSpaceString);
        final Set<String> executedActivities = new HashSet<>();
        while (matcher.find()) {
            executedActivities.add(matcher.group(1));
        }
        return executedActivities;
    }
}
