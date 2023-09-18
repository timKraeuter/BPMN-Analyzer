package no.hvl.tk.rulegenerator.server.endpoint.verification;

import no.tk.behavior.bpmn.AbstractBPMNProcess;
import no.tk.behavior.bpmn.BPMNCollaboration;
import no.tk.behavior.bpmn.BPMNEventSubprocess;
import no.tk.behavior.bpmn.BPMNProcess;
import no.tk.behavior.bpmn.FlowNode;
import no.tk.behavior.bpmn.auxiliary.exceptions.ShouldNotHappenRuntimeException;
import no.tk.groove.runner.GrooveJarRunner;
import no.tk.groove.runner.checking.ModelCheckingResult;
import no.tk.groove.runner.checking.TemporalLogic;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
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

  public static final String OPTION_TO_COMPLETE_CTL = "AF(AllTerminated)";
  public static final String UNSAFE_CTL = "AG(!Unsafe)";
  private final Path graphGrammarDir;
  private final BPMNCollaboration bpmnModel;

  public BPMNModelChecker(Path graphGrammarDir, BPMNCollaboration bpmnModel) {
    this.graphGrammarDir = graphGrammarDir;
    this.bpmnModel = bpmnModel;
  }

  public ModelCheckingResponse checkTemporalLogicProperty(TemporalLogic logic, String property)
      throws IOException, InterruptedException {
    if (logic == TemporalLogic.CTL) {
      final GrooveJarRunner grooveJarRunner = new GrooveJarRunner();
      ModelCheckingResult propertyCheckingResult =
          grooveJarRunner.checkCTL(graphGrammarDir.toString(), property);
      return new ModelCheckingResponse(
          property, propertyCheckingResult.isValid(), propertyCheckingResult.getError());
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
      case NO_DEAD_ACTIVITIES -> this.checkNoDeadActivities(response);
      case SAFENESS -> this.checkSafeness(response);
      case OPTION_TO_COMPLETE -> this.checkOptionToComplete(response);
      default -> throw new IllegalStateException("Unexpected value: " + property);
    }
  }

  private void checkOptionToComplete(BPMNSpecificPropertyCheckingResponse response)
      throws IOException, InterruptedException {

    final GrooveJarRunner grooveJarRunner = new GrooveJarRunner();
    ModelCheckingResult safenessResult =
        grooveJarRunner.checkCTL(graphGrammarDir.toString(), OPTION_TO_COMPLETE_CTL);

    response.addPropertyCheckingResult(
        new BPMNPropertyCheckingResult(
            BPMNSpecificProperty.OPTION_TO_COMPLETE,
            safenessResult.isValid(),
            "CTL: " + OPTION_TO_COMPLETE_CTL));
  }

  private void checkSafeness(BPMNSpecificPropertyCheckingResponse response)
      throws IOException, InterruptedException {
    final GrooveJarRunner grooveJarRunner = new GrooveJarRunner();
    ModelCheckingResult safenessResult =
        grooveJarRunner.checkCTL(graphGrammarDir.toString(), UNSAFE_CTL);

    response.addPropertyCheckingResult(
        new BPMNPropertyCheckingResult(
            BPMNSpecificProperty.SAFENESS, safenessResult.isValid(), "CTL: " + UNSAFE_CTL));
  }

  private void checkNoDeadActivities(BPMNSpecificPropertyCheckingResponse response)
      throws InterruptedException, IOException {
    // Generate state space for graph grammar.
    final GrooveJarRunner grooveJarRunner = new GrooveJarRunner();
    final String stateSpaceTempFile =
        String.format("%s%s.txt", this.getStateSpaceDirPath(), bpmnModel.getName() + "_StateSpace");
    grooveJarRunner.generateStateSpace(graphGrammarDir.toString(), stateSpaceTempFile, true);

    readStateSpaceAndCheckActivities(response, stateSpaceTempFile);
  }

  private String getStateSpaceDirPath() {
    return RuleGeneratorControllerHelper.STATE_SPACE_TEMP_DIR
        + File.separator
        + RuleGeneratorControllerHelper.getGGOrStateSpaceDirName(bpmnModel.getName())
        + File.separator;
  }

  private void readStateSpaceAndCheckActivities(
      BPMNSpecificPropertyCheckingResponse response, String stateSpaceTempFile) throws IOException {
    try {
      // Read the state space file and find the executed activities
      final Set<String> executedActivities = findExecutedActivitiesInStateSpace(stateSpaceTempFile);

      // Compare to all activities
      final Map<String, String> nameToIdActivityMap = getAllActivities();
      Set<String> deadActivities = nameToIdActivityMap.keySet();
      deadActivities.removeAll(executedActivities);

      recordNoDeadActivitiesResult(response, nameToIdActivityMap, deadActivities);
    } catch (NoSuchFileException exception) {
      throw new ModelCheckingException(
          "The state space could not be generated or generation timed out after 60 seconds.");
    }
  }

  private void recordNoDeadActivitiesResult(
      BPMNSpecificPropertyCheckingResponse response,
      Map<String, String> nameToIdActivityMap,
      Set<String> deadActivities) {

    if (deadActivities.isEmpty()) {
      response.addPropertyCheckingResult(
          new BPMNPropertyCheckingResult(BPMNSpecificProperty.NO_DEAD_ACTIVITIES, true, ""));
    } else {
      String deadActivitiesHint =
          String.join(",", this.getIds(deadActivities, nameToIdActivityMap));
      response.addPropertyCheckingResult(
          new BPMNPropertyCheckingResult(
              BPMNSpecificProperty.NO_DEAD_ACTIVITIES, false, deadActivitiesHint));
    }
  }

  private Set<String> getIds(Set<String> deadActivities, Map<String, String> nameToIdActivityMap) {
    return deadActivities.stream().map(nameToIdActivityMap::get).collect(Collectors.toSet());
  }

  private Map<String, String> getAllActivities() {
    return this.bpmnModel.getParticipants().stream()
        .flatMap(process -> getAllActivities(process).entrySet().stream())
        .collect(Collectors.toMap(Entry::getKey, Entry::getValue));
  }

  private Map<String, String> getAllActivities(BPMNProcess process) {
    // Get all activities from subprocesses
    final Map<String, String> allActivityNames =
        process
            .getSubProcesses()
            .flatMap(subprocess -> getAllActivities(subprocess).entrySet().stream())
            .collect(Collectors.toMap(Entry::getKey, Entry::getValue));
    // Get all activities from event subprocesses
    process
        .getEventSubprocesses()
        .flatMap(eventSubprocess -> getAllActivities(eventSubprocess).entrySet().stream())
        .forEach(
            nameToIdEntry ->
                allActivityNames.put(nameToIdEntry.getKey(), nameToIdEntry.getValue()));

    addActivityNamesForProcess(process, allActivityNames);
    return allActivityNames;
  }

  private Map<String, String> getAllActivities(BPMNEventSubprocess process) {
    // Get all activities from subprocesses
    Map<String, String> nameToIdActivityMap =
        process
            .getEventSubprocesses()
            .flatMap(eventSubprocess -> getAllActivities(eventSubprocess).entrySet().stream())
            .collect(Collectors.toMap(Entry::getKey, Entry::getValue));

    addActivityNamesForProcess(process, nameToIdActivityMap);
    return nameToIdActivityMap;
  }

  private void addActivityNamesForProcess(
      AbstractBPMNProcess process, Map<String, String> nameToIdActivityMap) {
    process
        .getFlowNodes()
        .filter(FlowNode::isTask)
        .forEach(task -> nameToIdActivityMap.put(task.getName(), task.getId()));
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
