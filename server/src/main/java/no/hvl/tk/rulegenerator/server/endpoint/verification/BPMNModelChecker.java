package no.hvl.tk.rulegenerator.server.endpoint.verification;

import behavior.bpmn.*;
import behavior.bpmn.auxiliary.exceptions.ShouldNotHappenRuntimeException;
import groove.runner.GrooveJarRunner;
import groove.runner.checking.ModelCheckingResult;
import groove.runner.checking.TemporalLogic;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import no.hvl.tk.rulegenerator.server.endpoint.RuleGeneratorControllerHelper;
import no.hvl.tk.rulegenerator.server.endpoint.dtos.BPMNPropertyCheckingResult;
import no.hvl.tk.rulegenerator.server.endpoint.dtos.BPMNSpecificProperty;
import no.hvl.tk.rulegenerator.server.endpoint.dtos.BPMNSpecificPropertyCheckingRequest;
import no.hvl.tk.rulegenerator.server.endpoint.dtos.BPMNSpecificPropertyCheckingResponse;
import no.hvl.tk.rulegenerator.server.endpoint.dtos.ModelCheckingResponse;
import no.hvl.tk.rulegenerator.server.endpoint.verification.exception.ModelCheckingException;

public class BPMNModelChecker {

  private final File graphGrammarDir;
  private final BPMNCollaboration bpmnModel;

  public BPMNModelChecker(File graphGrammarDir, BPMNCollaboration bpmnModel) {
    this.graphGrammarDir = graphGrammarDir;
    this.bpmnModel = bpmnModel;
  }

  public ModelCheckingResponse checkTemporalLogicProperty(TemporalLogic logic, String property)
      throws IOException, InterruptedException {
    if (logic == TemporalLogic.CTL) {
      final GrooveJarRunner grooveJarRunner = new GrooveJarRunner();
      ModelCheckingResult propertyCheckingResult =
          grooveJarRunner.checkCTL(graphGrammarDir.getPath(), property);
      return new ModelCheckingResponse(propertyCheckingResult.isValid());
    }
    throw new ShouldNotHappenRuntimeException("Only CTL model checking is currently supported!");
  }

  public BPMNSpecificPropertyCheckingResponse checkBPMNProperties(
      BPMNSpecificPropertyCheckingRequest propertyCheckingRequest)
      throws InterruptedException, IOException {
    BPMNSpecificPropertyCheckingResponse response = new BPMNSpecificPropertyCheckingResponse();

    for (BPMNSpecificProperty property : propertyCheckingRequest.getPropertiesToBeChecked()) {
      this.checkPropertyAndRecordResult(property, response);
    }
    response.sortResults();
    return response;
  }

  private void checkPropertyAndRecordResult(
      BPMNSpecificProperty property, BPMNSpecificPropertyCheckingResponse response)
      throws InterruptedException, IOException {
    switch (property) {
      case NO_DEAD_ACTIVITIES:
        this.checkNoDeadActivities(response);
        break;
      case SAFENESS:
        this.checkSafeness(response);
        break;
      case OPTION_TO_COMPLETE:
        this.checkOptionToComplete(response);
        break;
      default:
        throw new IllegalStateException("Unexpected value: " + property);
    }
  }

  private void checkOptionToComplete(BPMNSpecificPropertyCheckingResponse response) {
    // Not supported atm. We would run an LTL query but there is a bug in Groove.
    response.addPropertyCheckingResult(
        new BPMNPropertyCheckingResult(
            BPMNSpecificProperty.OPTION_TO_COMPLETE,
            false,
            "Checking BPMN-specific properties is not implemented in the web interface yet "
                + "due to the following bug in Groove https://sourceforge.net/p/groove/bugs/499/"));
  }

  private void checkSafeness(BPMNSpecificPropertyCheckingResponse response)
      throws IOException, InterruptedException {
    final GrooveJarRunner grooveJarRunner = new GrooveJarRunner();
    ModelCheckingResult safenessResult =
        grooveJarRunner.checkCTL(graphGrammarDir.getPath(), "AG(!Unsafe)");

    // Not supported atm. We would run an LTL query but there is a bug in Groove.
    response.addPropertyCheckingResult(
        new BPMNPropertyCheckingResult(
            BPMNSpecificProperty.SAFENESS, safenessResult.isValid(), ""));
  }

  private void checkNoDeadActivities(BPMNSpecificPropertyCheckingResponse response)
      throws InterruptedException, IOException {
    // Generate state space for graph grammar.
    final GrooveJarRunner grooveJarRunner = new GrooveJarRunner();
    final String stateSpaceTempFile =
        String.format(
            "%s%s.txt", RuleGeneratorControllerHelper.STATE_SPACE_TEMP_DIR, bpmnModel.getName());
    grooveJarRunner.generateStateSpace(graphGrammarDir.getPath(), stateSpaceTempFile, true);

    readStateSpaceAndCheckActivities(response, stateSpaceTempFile);
  }

  private void readStateSpaceAndCheckActivities(
      BPMNSpecificPropertyCheckingResponse response, String stateSpaceTempFile) throws IOException {
    try {
      // Read the state space file and find the executed activities
      final Set<String> executedActivities = findExecutedActivitiesInStateSpace(stateSpaceTempFile);

      // Compare to all activities
      final Set<String> allActivityNames = getAllActivityNames();
      allActivityNames.removeAll(executedActivities);

      recordNoDeadActivitiesResult(response, allActivityNames);
    } catch (NoSuchFileException exception) {
      throw new ModelCheckingException(
          "The state space could not be generated or generation timed out after 60 seconds.");
    }
  }

  private void recordNoDeadActivitiesResult(
      BPMNSpecificPropertyCheckingResponse response, Set<String> deadActivities) {
    if (deadActivities.isEmpty()) {
      response.addPropertyCheckingResult(
          new BPMNPropertyCheckingResult(BPMNSpecificProperty.NO_DEAD_ACTIVITIES, true, ""));
    } else {
      String deadActivitiesHint =
          String.format("Dead activities: %s", String.join(",", deadActivities));
      response.addPropertyCheckingResult(
          new BPMNPropertyCheckingResult(
              BPMNSpecificProperty.NO_DEAD_ACTIVITIES, false, deadActivitiesHint));
    }
  }

  private Set<String> getAllActivityNames() {
    return this.bpmnModel.getParticipants().stream()
        .flatMap(process -> getAllActivityNames(process).stream())
        .collect(Collectors.toSet());
  }

  private Set<String> getAllActivityNames(BPMNProcess process) {
    // Get all activities from subprocesses
    final Set<String> allActivityNames =
        process
            .getSubProcesses()
            .flatMap(subprocess -> getAllActivityNames(subprocess).stream())
            .collect(Collectors.toSet());
    // Get all activities from event subprocesses
    process
        .getEventSubprocesses()
        .flatMap(eventSubprocess -> getAllActivityNames(eventSubprocess).stream())
        .forEach(allActivityNames::add);

    addActivityNamesForProcess(process, allActivityNames);
    return allActivityNames;
  }

  private Set<String> getAllActivityNames(BPMNEventSubprocess process) {
    // Get all activities from subprocesses
    Set<String> allActivityNames =
        process
            .getEventSubprocesses()
            .flatMap(eventSubprocess -> getAllActivityNames(eventSubprocess).stream())
            .collect(Collectors.toSet());

    addActivityNamesForProcess(process, allActivityNames);
    return allActivityNames;
  }

  private void addActivityNamesForProcess(
      AbstractBPMNProcess process, Set<String> allActivityNames) {
    process
        .getFlowNodes()
        .filter(FlowNode::isTask)
        .map(FlowNode::getName)
        .forEach(allActivityNames::add);
  }

  private Set<String> findExecutedActivitiesInStateSpace(String stateSpaceTempFile)
      throws IOException {
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
