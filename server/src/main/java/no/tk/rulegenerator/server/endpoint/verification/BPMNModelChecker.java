package no.tk.rulegenerator.server.endpoint.verification;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import no.tk.behavior.bpmn.AbstractBPMNProcess;
import no.tk.behavior.bpmn.BPMNCollaboration;
import no.tk.behavior.bpmn.BPMNEventSubprocess;
import no.tk.behavior.bpmn.BPMNProcess;
import no.tk.behavior.bpmn.FlowNode;
import no.tk.behavior.bpmn.auxiliary.exceptions.ShouldNotHappenRuntimeException;
import no.tk.behavior.bpmn.auxiliary.visitors.DoNothingFlowNodeVisitor;
import no.tk.behavior.bpmn.events.EndEvent;
import no.tk.groove.runner.GrooveJarRunner;
import no.tk.groove.runner.checking.ModelCheckingResult;
import no.tk.groove.runner.checking.TemporalLogic;
import no.tk.rulegenerator.server.endpoint.RuleGeneratorControllerHelper;
import no.tk.rulegenerator.server.endpoint.dtos.BPMNPropertyCheckingResult;
import no.tk.rulegenerator.server.endpoint.dtos.BPMNSpecificProperty;
import no.tk.rulegenerator.server.endpoint.dtos.BPMNSpecificPropertyCheckingResponse;
import no.tk.rulegenerator.server.endpoint.dtos.ModelCheckingResponse;
import no.tk.rulegenerator.server.endpoint.verification.exception.ModelCheckingException;
import no.tk.util.ValueWrapper;

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
      Set<BPMNSpecificProperty> propertiesToBeChecked) throws InterruptedException, IOException {
    BPMNSpecificPropertyCheckingResponse response =
        new BPMNSpecificPropertyCheckingResponse(new ArrayList<>());

    for (BPMNSpecificProperty property : propertiesToBeChecked) {
      this.checkPropertyAndRecordResult(property, response);
    }
    response.sortResults();
    return response;
  }

  private void checkPropertyAndRecordResult(
      BPMNSpecificProperty property, BPMNSpecificPropertyCheckingResponse response)
      throws InterruptedException, IOException {
    switch (property) {
      case PROPER_COMPLETION -> this.checkProperCompletion(response);
      case NO_DEAD_ACTIVITIES -> this.checkNoDeadActivities(response);
      case SAFENESS -> this.checkSafeness(response);
      case OPTION_TO_COMPLETE -> this.checkOptionToComplete(response);
      default -> throw new IllegalStateException("Unexpected value: " + property);
    }
  }

  private void checkProperCompletion(BPMNSpecificPropertyCheckingResponse response)
      throws IOException, InterruptedException {
    // Find all end events
    Map<String, String> nameToIDEndEvents =
        getFlowNodesMatchingFilter(
            flowNode -> {
              ValueWrapper<Boolean> wrapper = new ValueWrapper<>(false);
              flowNode.accept(
                  new DoNothingFlowNodeVisitor() {
                    @Override
                    public void handle(EndEvent endEvent) {
                      wrapper.setValue(true);
                    }
                  });
              return wrapper.getValueIfExists();
            });

    // Generate state space for graph grammar.
    final GrooveJarRunner grooveJarRunner = new GrooveJarRunner();
    final String stateSpaceTempFile =
        String.format("%s%s.txt", this.getStateSpaceDirPath(), bpmnModel.getName() + "_StateSpace");
    grooveJarRunner.generateStateSpace(graphGrammarDir.toString(), stateSpaceTempFile, true);

    // Check if any end event was executed twice in the same path of the state space leading to
    // AllTerminated!

    for (Entry<String, String> nameAndID : nameToIDEndEvents.entrySet()) {
      System.out.printf("TODO check end event %s (%s)\n", nameAndID.getKey(), nameAndID.getValue());
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
      final Map<String, String> nameToIdActivityMap = getFlowNodesMatchingFilter(FlowNode::isTask);
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

  private Map<String, String> getFlowNodesMatchingFilter(Predicate<FlowNode> filter) {
    return this.bpmnModel.getParticipants().stream()
        .flatMap(process -> getFlowNodesMatchingFilter(process, filter).entrySet().stream())
        .collect(Collectors.toMap(Entry::getKey, Entry::getValue));
  }

  private Map<String, String> getFlowNodesMatchingFilter(
      BPMNProcess process, Predicate<FlowNode> filter) {
    // Get all activities from subprocesses
    final Map<String, String> nameToIdFlowNodeMap =
        process
            .allSubProcesses()
            .flatMap(
                subprocess -> getFlowNodesMatchingFilter(subprocess, filter).entrySet().stream())
            .collect(Collectors.toMap(Entry::getKey, Entry::getValue));
    // Get all activities from event subprocesses
    process
        .eventSubprocesses()
        .flatMap(
            eventSubprocess ->
                getFlowNodesMatchingFilter(eventSubprocess, filter).entrySet().stream())
        .forEach(
            nameToIdEntry ->
                nameToIdFlowNodeMap.put(nameToIdEntry.getKey(), nameToIdEntry.getValue()));

    addFlowNodeNamesForProcess(process, nameToIdFlowNodeMap, filter);
    return nameToIdFlowNodeMap;
  }

  private Map<String, String> getFlowNodesMatchingFilter(
      BPMNEventSubprocess process, Predicate<FlowNode> filter) {
    // Get all activities from subprocesses
    Map<String, String> nameToIdFlowNodeMap =
        process
            .eventSubprocesses()
            .flatMap(
                eventSubprocess ->
                    getFlowNodesMatchingFilter(eventSubprocess, filter).entrySet().stream())
            .collect(Collectors.toMap(Entry::getKey, Entry::getValue));

    addFlowNodeNamesForProcess(process, nameToIdFlowNodeMap, filter);
    return nameToIdFlowNodeMap;
  }

  private void addFlowNodeNamesForProcess(
      AbstractBPMNProcess process,
      Map<String, String> nameToIdFlowNodeMap,
      Predicate<FlowNode> filter) {
    process
        .flowNodes()
        .filter(filter)
        .forEach(task -> nameToIdFlowNodeMap.put(task.getName(), task.getId()));
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
