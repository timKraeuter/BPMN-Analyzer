package no.tk.rulegenerator.server.endpoint.verification;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
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
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BPMNModelChecker {

  public static final String OPTION_TO_COMPLETE_CTL = "AF(AllTerminated)";
  public static final String UNSAFE_CTL = "AG(!Unsafe)";
  private final Path graphGrammarDir;
  private String stateSpace = "";
  private final BPMNCollaboration bpmnModel;
  protected static final Logger logger = LoggerFactory.getLogger(BPMNModelChecker.class);

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
    generateStateSpaceIfNeeded();

    // Find all terminated states
    Set<String> terminatedStateIds = findAllTerminatedStates(stateSpace);

    // Check if any end event was executed twice in the same path of the state space leading to
    // AllTerminated!

    for (String terminatedStateId : terminatedStateIds) {
      Optional<String> endEventName =
          checkIfAnyEndEventExecutedTwice(
              terminatedStateId, nameToIDEndEvents.keySet(), stateSpace);
      if (endEventName.isPresent()) {
        response.addPropertyCheckingResult(
            new BPMNPropertyCheckingResult(
                BPMNSpecificProperty.PROPER_COMPLETION,
                false,
                nameToIDEndEvents.get(endEventName.get())));
        return;
      }
    }
    response.addPropertyCheckingResult(
        new BPMNPropertyCheckingResult(BPMNSpecificProperty.PROPER_COMPLETION, true, ""));
  }

  private Optional<String> checkIfAnyEndEventExecutedTwice(
      String terminatedStateId, Set<String> endEventNames, String stateSpace) {
    String startState = getStartState(stateSpace);

    return checkIncomingTransitionsForNode(
        startState, stateSpace, endEventNames, new HashMap<>(), terminatedStateId);
  }

  private Optional<String> checkTransitionAndItsSource(
      String startState,
      String stateSpace,
      Set<String> endEventNames,
      Map<String, Boolean> seenEndEvents,
      Pair<String, String> currentTransition) {
    String transitionLabel = currentTransition.getRight();
    // Check the label
    for (String endEventName : endEventNames) {
      if (transitionLabel.equals(endEventName) || transitionLabel.startsWith(endEventName + "_")) {
        Boolean seen = seenEndEvents.get(endEventName);
        if (seen != null) {
          return Optional.of(endEventName);
        }
        seenEndEvents.put(endEventName, true);
      }
    }

    String transitionSource = currentTransition.getKey();
    if (transitionSource.equals(startState)) {
      return Optional.empty();
    }

    // Search further
    return checkIncomingTransitionsForNode(
        startState, stateSpace, endEventNames, seenEndEvents, transitionSource);
  }

  private Optional<String> checkIncomingTransitionsForNode(
      String startState,
      String stateSpace,
      Set<String> endEventNames,
      Map<String, Boolean> seenEndEvents,
      String node) {
    Set<Pair<String, String>> incomingTransitions = getIncomingTransitions(node, stateSpace);
    for (Pair<String, String> incomingTransition : incomingTransitions) {
      Optional<String> endEventName =
          checkTransitionAndItsSource(
              startState,
              stateSpace,
              endEventNames,
              new HashMap<>(seenEndEvents),
              incomingTransition);
      if (endEventName.isPresent()) {
        return endEventName;
      }
    }
    return Optional.empty();
  }

  private Set<Pair<String, String>> getIncomingTransitions(String nodeId, String stateSpace) {
    Pattern regEx =
        Pattern.compile(
            String.format(
                """
            <edge from="(.*)" to="%s">
            \\s*<attr name="label">
            \\s*<string>(.*)</string>
            """,
                nodeId));
    final Matcher matcher = regEx.matcher(stateSpace);
    Set<Pair<String, String>> transitionSourceAndLabel = new HashSet<>();
    while (matcher.find()) {
      String edgeSource = matcher.group(1);
      String edgeName = matcher.group(2);
      if (!edgeSource.equals(nodeId)) {
        transitionSourceAndLabel.add(Pair.of(edgeSource, edgeName));
      }
    }
    return transitionSourceAndLabel;
  }

  private String getStartState(String stateSpace) {
    Pattern regEx =
        Pattern.compile(
            """
            <edge from="(.*)" to=".*">
            \\s*<attr name="label">
            \\s*<string>start</string>
            """);
    Matcher matcher = regEx.matcher(stateSpace);
    if (!matcher.find()) {
      throw new ShouldNotHappenRuntimeException("Start state in state space could not be found!");
    }
    return matcher.group(1);
  }

  private static Set<String> findAllTerminatedStates(String stateSpace) {
    Pattern regEx =
        Pattern.compile(
            """
            <edge from="(.*)" to=".*">
            \\s*<attr name="label">
            \\s*<string>AllTerminated</string>
            """);
    final Matcher matcher = regEx.matcher(stateSpace);
    Set<String> terminatedStateIds = new HashSet<>();
    while (matcher.find()) {
      terminatedStateIds.add(matcher.group(1));
    }
    return terminatedStateIds;
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
    generateStateSpaceIfNeeded();

    response.addPropertyCheckingResult(
        new BPMNPropertyCheckingResult(
            BPMNSpecificProperty.SAFENESS, isSafe(stateSpace), "CTL: " + UNSAFE_CTL));
  }

  private static boolean isSafe(String stateSpace) {
    Pattern regEx =
        Pattern.compile(
            """
            <edge from=".*" to=".*">
            \\s*<attr name="label">
            \\s*<string>Unsafe</string>
            """);
    return !regEx.matcher(stateSpace).find();
  }

  private void generateStateSpaceIfNeeded() throws IOException, InterruptedException {
    // Generate state space for graph grammar.
    if (stateSpace.isEmpty()) {
      // Generate new state space
      final GrooveJarRunner grooveJarRunner = new GrooveJarRunner();
      final String stateSpaceTempFile = getStateSpaceTempFile();
      try {
        stateSpace =
            Files.readString(
                grooveJarRunner.generateStateSpace(
                    graphGrammarDir.toString(), stateSpaceTempFile, true));
      } catch (NoSuchFileException exception) {
        logger.error("State space file generation failed", exception);
        throw new ModelCheckingException(
            "The state space could not be generated or generation timed out after 60 seconds.");
      }
    }
  }

  private String getStateSpaceTempFile() {
    return String.format(
        "%s%s.txt", this.getStateSpaceDirPath(), bpmnModel.getName() + "_StateSpace");
  }

  private void checkNoDeadActivities(BPMNSpecificPropertyCheckingResponse response)
      throws InterruptedException, IOException {
    generateStateSpaceIfNeeded();

    readStateSpaceAndCheckActivities(response, stateSpace);
  }

  private String getStateSpaceDirPath() {
    return RuleGeneratorControllerHelper.STATE_SPACE_TEMP_DIR
        + File.separator
        + RuleGeneratorControllerHelper.getGGOrStateSpaceDirName(bpmnModel.getName())
        + File.separator;
  }

  private void readStateSpaceAndCheckActivities(
      BPMNSpecificPropertyCheckingResponse response, String stateSpace) {
    // Read the state space file and find the executed activities
    final Set<String> executedActivities = findExecutedActivitiesInStateSpace(stateSpace);

    // Compare to all activities
    final Map<String, String> nameToIdActivityMap = getFlowNodesMatchingFilter(FlowNode::isTask);
    Set<String> deadActivities = nameToIdActivityMap.keySet();
    deadActivities.removeAll(executedActivities);

    recordNoDeadActivitiesResult(response, nameToIdActivityMap, deadActivities);
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

  private Set<String> findExecutedActivitiesInStateSpace(String stateSpace) {
    final Pattern regEx = Pattern.compile("<string>(.*)_end</string>");

    final Matcher matcher = regEx.matcher(stateSpace);
    final Set<String> executedActivities = new HashSet<>();
    while (matcher.find()) {
      executedActivities.add(matcher.group(1));
    }
    return executedActivities;
  }
}
