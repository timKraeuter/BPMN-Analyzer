package no.hvl.tk.rulegenerator.server.endpoint.verification;

import behavior.bpmn.*;
import groove.runner.GrooveJarRunner;
import no.hvl.tk.rulegenerator.server.endpoint.RuleGeneratorControllerHelper;
import no.hvl.tk.rulegenerator.server.endpoint.dtos.BPMNPropertyCheckingResult;
import no.hvl.tk.rulegenerator.server.endpoint.dtos.ModelCheckingProperty;
import no.hvl.tk.rulegenerator.server.endpoint.dtos.ModelCheckingRequest;
import no.hvl.tk.rulegenerator.server.endpoint.dtos.ModelCheckingResponse;
import no.hvl.tk.rulegenerator.server.endpoint.verification.exception.ModelCheckingException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
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

    public ModelCheckingResponse runModelChecking(ModelCheckingRequest modelCheckingRequest) throws InterruptedException, IOException {
        ModelCheckingResponse response = new ModelCheckingResponse();

        for (ModelCheckingProperty modelCheckingProperty : modelCheckingRequest.getPropertiesToBeChecked()) {
            this.checkPropertyAndRecordResult(modelCheckingProperty, response);
        }
        return response;
    }

    private void checkPropertyAndRecordResult(ModelCheckingProperty modelCheckingProperty,
                                              ModelCheckingResponse response) throws InterruptedException, IOException {
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

    private void checkNoDeadActivities(ModelCheckingResponse response) throws InterruptedException, IOException {
        // Generate state space for graph grammar.
        final GrooveJarRunner grooveJarRunner = new GrooveJarRunner();
        final String stateSpaceTempFile = String.format("%s%s.txt",
                                                        RuleGeneratorControllerHelper.STATE_SPACE_TEMP_DIR,
                                                        bpmnModel.getName());
        grooveJarRunner.generateStateSpace(graphGrammarDir.getPath(), stateSpaceTempFile, true);

        readStateSpaceAndCheckActivities(response, stateSpaceTempFile);
    }

    private void readStateSpaceAndCheckActivities(ModelCheckingResponse response,
                                                  String stateSpaceTempFile) throws IOException {
        try {
            // Read the state space file and find the executed activities
            final Set<String> executedActivities = findExecutedActivitiesInStateSpace(stateSpaceTempFile);

            // Compare to all activities
            final Set<String> allActivityNames = getAllActivityNames();
            allActivityNames.removeAll(executedActivities);

            recordNoDeadActivitiesResult(response, allActivityNames);
        }
        catch (NoSuchFileException exception) {
            throw new ModelCheckingException(
                    "The state space could not be generated or generation timed out after 60 seconds.");
        }
    }

    private void recordNoDeadActivitiesResult(ModelCheckingResponse response, Set<String> deadActivities) {
        if (deadActivities.isEmpty()) {
            response.addPropertyCheckingResult(new BPMNPropertyCheckingResult(ModelCheckingProperty.NO_DEAD_ACTIVITIES,
                                                                              true,
                                                                              ""));
        } else {
            String deadActivitiesHint = String.format("Dead activities: %s", String.join(",", deadActivities));
            response.addPropertyCheckingResult(new BPMNPropertyCheckingResult(ModelCheckingProperty.NO_DEAD_ACTIVITIES,
                                                                              false,
                                                                              deadActivitiesHint));
        }
    }

    private Set<String> getAllActivityNames() {
        return this.bpmnModel.getParticipants().stream().flatMap(process -> getAllActivityNames(process).stream()).collect(
                Collectors.toSet());
    }

    private Set<String> getAllActivityNames(BPMNProcess process) {
        // Get all activities from subprocesses
        final Set<String> allActivityNames = process.getSubProcesses().flatMap(subprocess -> getAllActivityNames(
                subprocess).stream()).collect(Collectors.toSet());
        // Get all activities from event subprocesses
        process.getEventSubprocesses().flatMap(eventSubprocess -> getAllActivityNames(eventSubprocess).stream()).forEach(
                allActivityNames::add);

        addActivityNamesForProcess(process, allActivityNames);
        return allActivityNames;
    }

    private Set<String> getAllActivityNames(BPMNEventSubprocess process) {
        // Get all activities from subprocesses
        Set<String> allActivityNames = process.getEventSubprocesses().flatMap(eventSubprocess -> getAllActivityNames(
                eventSubprocess).stream()).collect(Collectors.toSet());

        addActivityNamesForProcess(process, allActivityNames);
        return allActivityNames;
    }

    private void addActivityNamesForProcess(AbstractBPMNProcess process, Set<String> allActivityNames) {
        process.getFlowNodes().filter(FlowNode::isTask).map(FlowNode::getName).forEach(allActivityNames::add);
    }

    private Set<String> findExecutedActivitiesInStateSpace(String stateSpaceTempFile) throws IOException {
        final Pattern regEx = Pattern.compile("<string>(.*)_end</string>");

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
