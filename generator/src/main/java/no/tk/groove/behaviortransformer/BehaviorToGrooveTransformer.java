package no.tk.groove.behaviortransformer;

import com.google.common.collect.Maps;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import no.tk.behavior.Behavior;
import no.tk.behavior.BehaviorVisitor;
import no.tk.behavior.activity.ActivityDiagram;
import no.tk.behavior.bpmn.BPMNCollaboration;
import no.tk.behavior.bpmn.auxiliary.exceptions.ShouldNotHappenRuntimeException;
import no.tk.behavior.fsm.FiniteStateMachine;
import no.tk.behavior.petrinet.PetriNet;
import no.tk.behavior.picalculus.NamedPiProcess;
import no.tk.groove.GrooveGxlHelper;
import no.tk.groove.behaviortransformer.bpmn.BPMNToGrooveTransformer;
import no.tk.groove.graph.GrooveGraph;
import no.tk.groove.graph.GrooveNode;
import no.tk.groove.graph.GrooveValue;
import no.tk.groove.graph.rule.GrooveGraphRule;
import no.tk.groove.graph.rule.GrooveRuleBuilder;
import no.tk.groove.graph.rule.GrooveRuleWriter;
import no.tk.groove.gxl.Graph;
import no.tk.groove.gxl.Gxl;
import no.tk.groove.gxl.Node;
import no.tk.util.ValueWrapper;
import org.apache.commons.io.file.PathUtils;

public class BehaviorToGrooveTransformer {

  static final String START_GST = "start.gst";
  static final String START = "start";
  private static final String ACTIVITY_DIAGRAM_TYPE_GRAPH_FILE_NAME = "type";
  private static final String PI_TYPE_GRAPH_FILE_NAME = "Type";
  private static final String BPMN_DIAGRAM_TYPE_GRAPH_FILE_NAME = "bpmn_e_model";
  private static final String FSM_TYPE_GRAPH_FILE_NAME = "fsm_e_model";
  public static final String TYPE_GRAPH = "typeGraph";
  private final boolean layout;

  public BehaviorToGrooveTransformer(boolean layout) {
    this.layout = layout;
  }

  static Gxl createGxlFromGrooveGraph(GrooveGraph graph, boolean layout) {
    String gxlGraphName = String.format("%s_%s", graph.getName(), START);
    Gxl gxl = new Gxl();
    Graph gxlGraph = GrooveGxlHelper.createStandardGxlGraph(gxlGraphName, gxl);

    Map<String, String> idToNodeLabel = new HashMap<>();
    Map<String, Node> grooveNodeIdToGxlNode = new HashMap<>();

    graph
        .nodes()
        .forEach(
            node -> {
              Node gxlNode =
                  GrooveGxlHelper.createNodeWithName(node.getId(), node.getName(), gxlGraph);
              // Add flags
              node.getFlags()
                  .forEach(flag -> GrooveGxlHelper.addFlagToNode(gxlGraph, gxlNode, flag));
              // Add data nodes/attributes
              node.getAttributes()
                  .forEach(
                      (name, value) ->
                          addNodeAttribute(gxlGraph, idToNodeLabel, gxlNode, name, value));

              idToNodeLabel.put(node.getId(), node.getName());
              grooveNodeIdToGxlNode.put(node.getId(), gxlNode);
            });
    graph
        .edges()
        .forEach(
            edge ->
                GrooveGxlHelper.createEdgeWithName(
                    gxlGraph,
                    grooveNodeIdToGxlNode.get(edge.getSourceNode().getId()),
                    grooveNodeIdToGxlNode.get(edge.getTargetNode().getId()),
                    edge.getName()));

    if (layout) {
      GrooveGxlHelper.layoutGraph(gxlGraph, idToNodeLabel);
    }
    return gxl;
  }

  private static void addNodeAttribute(
      Graph graph,
      Map<String, String> idToNodeLabel,
      Node attributeHolder,
      String attributeName,
      GrooveValue<?> attributeValue) {
    String attributeNodeName =
        String.format("%s:%s", attributeValue.getTypeName(), attributeValue.getValue());
    Node dataNode =
        GrooveGxlHelper.createNodeWithName(GrooveNode.getNextNodeId(), attributeNodeName, graph);
    GrooveGxlHelper.createEdgeWithName(graph, attributeHolder, dataNode, attributeName);

    idToNodeLabel.put(dataNode.getId(), attributeNodeName);
  }

  void generateGrooveGrammar(
      Path grooveFolder,
      String graphGrammarName,
      Map<String, Set<String>> nameToToBeSynchedRuleNames,
      Behavior... behaviors) {
    Path graphGrammarSubFolder = this.makeSubFolder(graphGrammarName, grooveFolder);

    Set<String> typeGraphs = new LinkedHashSet<>();
    Set<GrooveGraph> startGraphs = new LinkedHashSet<>();
    Map<String, GrooveGraphRule> allRules = new LinkedHashMap<>();
    final Map<String, String> additionalProperties = Maps.newHashMap();

    Arrays.stream(behaviors)
        .forEach(
            behavior ->
                behavior.accept(
                    new BehaviorVisitor() {
                      @Override
                      public void handle(FiniteStateMachine finiteStateMachine) {
                        FSMToGrooveTransformer transformer = new FSMToGrooveTransformer(layout);

                        startGraphs.add(transformer.generateStartGraph(finiteStateMachine));
                        transformer
                            .generateRules(finiteStateMachine)
                            .forEach(rule -> allRules.put(rule.getRuleName(), rule));
                        // Copy type graph
                        transformer.generateAndWriteRulesFurther(
                            finiteStateMachine, graphGrammarSubFolder);
                        typeGraphs.add(FSM_TYPE_GRAPH_FILE_NAME);
                      }

                      @Override
                      public void handle(PetriNet petriNet) {
                        PNToGrooveTransformer transformer = new PNToGrooveTransformer(layout);

                        startGraphs.add(transformer.generateStartGraph(petriNet));
                        transformer
                            .generateRules(petriNet)
                            .forEach(rule -> allRules.put(rule.getRuleName(), rule));
                        // Copy type graph if needed in the future!
                      }

                      @Override
                      public void handle(BPMNCollaboration collaboration) {
                        BPMNToGrooveTransformer transformer = new BPMNToGrooveTransformer(layout);
                        additionalProperties.put("matchInjective", "true");

                        startGraphs.add(transformer.generateStartGraph(collaboration));
                        transformer
                            .generateRules(collaboration)
                            .forEach(rule -> allRules.put(rule.getRuleName(), rule));
                        // Copy type graph
                        transformer.generateAndWriteRulesFurther(
                            collaboration, graphGrammarSubFolder);
                        typeGraphs.add(BPMN_DIAGRAM_TYPE_GRAPH_FILE_NAME);
                      }

                      @Override
                      public void handle(NamedPiProcess piProcess) {
                        additionalProperties.put("checkDangling", "true");

                        PiCalcToGrooveTransformer transformer =
                            new PiCalcToGrooveTransformer(layout);

                        startGraphs.add(transformer.generateStartGraph(piProcess));
                        transformer
                            .generateRules(piProcess)
                            .forEach(rule -> allRules.put(rule.getRuleName(), rule));
                        // Copy type graph
                        transformer.generateAndWriteRulesFurther(piProcess, graphGrammarSubFolder);
                        typeGraphs.add(PI_TYPE_GRAPH_FILE_NAME);
                      }

                      @Override
                      public void handle(ActivityDiagram activityDiagram) {
                        typeGraphs.add(ACTIVITY_DIAGRAM_TYPE_GRAPH_FILE_NAME);
                        throw new UnsupportedOperationException();
                      }
                    }));
    if (!typeGraphs.isEmpty()) {
      additionalProperties.put(TYPE_GRAPH, String.join(" ", typeGraphs));
    }
    this.generatePropertiesFile(graphGrammarSubFolder, START, additionalProperties);

    // Merge start graphs and write the final one.
    this.mergeAndWriteStartGraphs(graphGrammarSubFolder, startGraphs);

    this.writeAndSynchRules(nameToToBeSynchedRuleNames, allRules, graphGrammarSubFolder);
  }

  private void writeAndSynchRules(
      Map<String, Set<String>> nameToToBeSynchedRuleNames,
      Map<String, GrooveGraphRule> indexedRules,
      Path targetFolder) {
    Map<String, Set<GrooveGraphRule>> nameToToBeSynchedRules = new LinkedHashMap<>();
    Set<GrooveGraphRule> unsynchedRules = new LinkedHashSet<>(indexedRules.values());

    nameToToBeSynchedRuleNames.forEach(
        (newRuleName, ruleNames) -> {
          Set<GrooveGraphRule> rules =
              ruleNames.stream().map(indexedRules::get).collect(Collectors.toSet());
          rules.forEach(unsynchedRules::remove);
          nameToToBeSynchedRules.put(newRuleName, rules);
        });

    Stream<GrooveGraphRule> synchedRules =
        GrooveRuleBuilder.createSynchedRules(nameToToBeSynchedRules);

    GrooveRuleWriter.writeRules(targetFolder, synchedRules, layout);
    GrooveRuleWriter.writeRules(targetFolder, unsynchedRules.stream(), layout);
  }

  private void mergeAndWriteStartGraphs(Path graphGrammarSubFolder, Set<GrooveGraph> startGraphs) {
    Optional<GrooveGraph> startGraph =
        startGraphs.stream()
            .reduce((graph, graph2) -> graph.union(graph2, (name1, name2) -> name1));
    startGraph.ifPresent(
        graph -> GrooveTransformer.writeStartGraph(graphGrammarSubFolder, graph, layout));
  }

  public Path generateGrooveGrammar(Behavior behavior, Path targetFolder) {
    ValueWrapper<Path> result = new ValueWrapper<>();
    behavior.accept(
        new BehaviorVisitor() {
          @Override
          public void handle(FiniteStateMachine finiteStateMachine) {
            result.setValue(
                BehaviorToGrooveTransformer.this.generateGrooveGrammarForFSM(
                    finiteStateMachine, targetFolder));
          }

          @Override
          public void handle(PetriNet petriNet) {
            result.setValue(
                BehaviorToGrooveTransformer.this.generateGrooveGrammarForPN(
                    petriNet, targetFolder));
          }

          @Override
          public void handle(BPMNCollaboration collaboration) {
            result.setValue(
                BehaviorToGrooveTransformer.this.generateGrooveGrammarForBPMNProcessModel(
                    collaboration, targetFolder));
          }

          @Override
          public void handle(NamedPiProcess piProcess) {
            result.setValue(
                BehaviorToGrooveTransformer.this.generateGrooveGrammarForPiProcess(
                    piProcess, targetFolder));
          }

          @Override
          public void handle(ActivityDiagram activityDiagram) {
            result.setValue(
                BehaviorToGrooveTransformer.this.generateGrooveGrammarForActivityDiagram(
                    activityDiagram, targetFolder));
          }
        });
    return result.getValueIfExists();
  }

  private Path generateGrooveGrammarForActivityDiagram(
      ActivityDiagram activityDiagram, Path targetFolder) {
    Path graphGrammarSubFolder = this.makeSubFolder(activityDiagram, targetFolder);
    ActivityDiagramToGrooveTransformer transformer = new ActivityDiagramToGrooveTransformer(true);

    transformer.generateAndWriteStartGraph(activityDiagram, graphGrammarSubFolder);

    transformer.generateAndWriteRules(activityDiagram, graphGrammarSubFolder);

    final Map<String, String> additionalProperties = Maps.newHashMap();
    additionalProperties.put(TYPE_GRAPH, ACTIVITY_DIAGRAM_TYPE_GRAPH_FILE_NAME);
    this.generatePropertiesFile(graphGrammarSubFolder, START, additionalProperties);

    return graphGrammarSubFolder;
  }

  private Path generateGrooveGrammarForPiProcess(NamedPiProcess piProcess, Path grooveDir) {
    Path graphGrammarSubFolder = this.makeSubFolder(piProcess, grooveDir);
    PiCalcToGrooveTransformer transformer = new PiCalcToGrooveTransformer(layout);

    transformer.generateAndWriteStartGraph(piProcess, graphGrammarSubFolder);

    transformer.generateAndWriteRules(piProcess, graphGrammarSubFolder);

    final Map<String, String> additionalProperties = Maps.newHashMap();
    additionalProperties.put(TYPE_GRAPH, PI_TYPE_GRAPH_FILE_NAME);
    additionalProperties.put("checkDangling", "true");
    this.generatePropertiesFile(graphGrammarSubFolder, START, additionalProperties);

    return graphGrammarSubFolder;
  }

  public Path generateGrooveGrammarForBPMNProcessModel(
      BPMNCollaboration collaboration, Path grooveDir) {
    Path graphGrammarSubFolder = this.makeSubFolder(collaboration, grooveDir);
    BPMNToGrooveTransformer transformer = new BPMNToGrooveTransformer(layout);

    // Generate start graph
    transformer.generateAndWriteStartGraph(collaboration, graphGrammarSubFolder);
    // Generate rules
    transformer.generateAndWriteRules(collaboration, graphGrammarSubFolder);

    final Map<String, String> additionalProperties = Maps.newHashMap();
    additionalProperties.put("matchInjective", "true");
    additionalProperties.put(TYPE_GRAPH, BPMN_DIAGRAM_TYPE_GRAPH_FILE_NAME);
    this.generatePropertiesFile(graphGrammarSubFolder, START, additionalProperties);

    return graphGrammarSubFolder;
  }

  private Path generateGrooveGrammarForPN(PetriNet petriNet, Path grooveDir) {
    Path graphGrammarSubFolder = this.makeSubFolder(petriNet, grooveDir);
    PNToGrooveTransformer transformer = new PNToGrooveTransformer(layout);

    // Generate start graph
    transformer.generateAndWriteStartGraph(petriNet, graphGrammarSubFolder);

    // Generate rules
    transformer.generateAndWriteRules(petriNet, graphGrammarSubFolder);

    this.generatePropertiesFile(graphGrammarSubFolder, START, Maps.newHashMap());

    return graphGrammarSubFolder;
  }

  private Path generateGrooveGrammarForFSM(FiniteStateMachine finiteStateMachine, Path grooveDir) {
    Path graphGrammarSubFolder = this.makeSubFolder(finiteStateMachine, grooveDir);
    FSMToGrooveTransformer transformer = new FSMToGrooveTransformer(layout);

    // Generate start graph
    transformer.generateAndWriteStartGraph(finiteStateMachine, graphGrammarSubFolder);
    // Generate rules
    transformer.generateAndWriteRules(finiteStateMachine, graphGrammarSubFolder);

    final Map<String, String> additionalProperties = Maps.newHashMap();
    additionalProperties.put(TYPE_GRAPH, FSM_TYPE_GRAPH_FILE_NAME);
    this.generatePropertiesFile(graphGrammarSubFolder, START, additionalProperties);

    return graphGrammarSubFolder;
  }

  private Path makeSubFolder(Behavior behavior, Path grooveDir) {
    return this.makeSubFolder(behavior.getName(), grooveDir);
  }

  private Path makeSubFolder(String folderName, Path grooveDir) {
    Path graphGrammarSubFolder = Paths.get(grooveDir.toString(), folderName + ".gps");
    createEmptyDir(graphGrammarSubFolder);
    return graphGrammarSubFolder;
  }

  private static void createEmptyDir(Path graphGrammarSubFolder) {
    try {
      Files.createDirectories(graphGrammarSubFolder);
      if (!PathUtils.isEmpty(graphGrammarSubFolder)) {
        PathUtils.cleanDirectory(graphGrammarSubFolder);
      }
    } catch (IOException e) {
      throw new EmptyFolderCouldNotBeCreatedException(
          String.format(
              "The empty subfolder %s for the groove rule generation could not be created.",
              graphGrammarSubFolder),
          e);
    }
  }

  private void generatePropertiesFile(
      Path subFolder, String startGraph, Map<String, String> additionalProperties) {
    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    LocalDateTime now = LocalDateTime.now(ZoneId.systemDefault());

    String propertiesContent =
        String.format(
            "# %s (Groove rule generator)%nlocation=%s%nstartGraph=%s%n%sgrooveVersion=6.0.2%ngrammarVersion=3.7",
            dtf.format(now),
            subFolder,
            startGraph,
            this.getAdditionalProperties(additionalProperties));
    Path propertiesFile = Paths.get(subFolder.toString(), "system.properties");
    try {
      Files.writeString(propertiesFile, propertiesContent);
    } catch (IOException e) {
      throw new ShouldNotHappenRuntimeException(e);
    }
  }

  private String getAdditionalProperties(Map<String, String> additionalProperties) {
    return additionalProperties.entrySet().stream()
        .reduce("", (prop1, prop2) -> prop1 + prop2 + "\n", (key, value) -> key + "=" + value);
  }
}
