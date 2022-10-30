package groove.behaviortransformer;

import behavior.Behavior;
import behavior.BehaviorVisitor;
import behavior.activity.ActivityDiagram;
import behavior.bpmn.BPMNCollaboration;
import behavior.bpmn.auxiliary.exceptions.ShouldNotHappenRuntimeException;
import behavior.fsm.FiniteStateMachine;
import behavior.petrinet.PetriNet;
import behavior.picalculus.NamedPiProcess;
import com.google.common.collect.Maps;
import groove.GrooveGxlHelper;
import groove.behaviortransformer.bpmn.BPMNToGrooveTransformer;
import groove.graph.GrooveGraph;
import groove.graph.GrooveNode;
import groove.graph.GrooveValue;
import groove.graph.rule.GrooveGraphRule;
import groove.graph.rule.GrooveRuleBuilder;
import groove.graph.rule.GrooveRuleWriter;
import groove.gxl.Graph;
import groove.gxl.Gxl;
import groove.gxl.Node;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.io.FileUtils;
import util.ValueWrapper;

public class BehaviorToGrooveTransformer {
  static final String START_GST = "/start.gst";
  static final String START = "start";
  private static final String ACTIVITY_DIAGRAM_TYPE_GRAPH_FILE_NAME = "type";
  private static final String PI_TYPE_GRAPH_FILE_NAME = "Type";
  private static final String BPMN_DIAGRAM_TYPE_GRAPH_FILE_NAME = "bpmn_e_model";
  private static final String FSM_TYPE_GRAPH_FILE_NAME = "fsm_e_model";
  public static final String TYPE_GRAPH = "typeGraph";

  static Gxl createGxlFromGrooveGraph(GrooveGraph graph, boolean doLayout) {
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

    if (doLayout) {
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
      File grooveFolder,
      String graphGrammarName,
      Map<String, Set<String>> nameToToBeSynchedRuleNames,
      Behavior... behaviors) {
    File graphGrammarSubFolder = this.makeSubFolder(graphGrammarName, grooveFolder);

    final boolean[] piProcessIncluded = {false};
    Set<String> typeGraphs = new LinkedHashSet<>();
    Set<GrooveGraph> startGraphs = new LinkedHashSet<>();
    Map<String, GrooveGraphRule> allRules = new LinkedHashMap<>();

    Arrays.stream(behaviors)
        .forEach(
            behavior ->
                behavior.accept(
                    new BehaviorVisitor() {
                      @Override
                      public void handle(FiniteStateMachine finiteStateMachine) {
                        FSMToGrooveTransformer transformer = new FSMToGrooveTransformer();

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
                        PNToGrooveTransformer transformer = new PNToGrooveTransformer();

                        startGraphs.add(transformer.generateStartGraph(petriNet));
                        transformer
                            .generateRules(petriNet)
                            .forEach(rule -> allRules.put(rule.getRuleName(), rule));
                        // Copy type graph if needed in the future!
                      }

                      @Override
                      public void handle(BPMNCollaboration collaboration) {
                        BPMNToGrooveTransformer transformer = new BPMNToGrooveTransformer();

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
                        piProcessIncluded[0] = true;

                        PiCalcToGrooveTransformer transformer = new PiCalcToGrooveTransformer();

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
                        // TODO: implement ActivityDiagram
                        typeGraphs.add(ACTIVITY_DIAGRAM_TYPE_GRAPH_FILE_NAME);
                        throw new UnsupportedOperationException();
                      }
                    }));

    final Map<String, String> additionalProperties = Maps.newHashMap();
    if (piProcessIncluded[0]) {
      additionalProperties.put("checkDangling", "true");
    }
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
      File targetFolder) {
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

    GrooveRuleWriter.writeRules(synchedRules, targetFolder);
    GrooveRuleWriter.writeRules(unsynchedRules.stream(), targetFolder);
  }

  private void mergeAndWriteStartGraphs(File graphGrammarSubFolder, Set<GrooveGraph> startGraphs) {
    Optional<GrooveGraph> startGraph =
        startGraphs.stream()
            .reduce((graph, graph2) -> graph.union(graph2, (name1, name2) -> name1));
    startGraph.ifPresent(
        graph -> GrooveTransformer.writeStartGraph(graphGrammarSubFolder, graph, true));
  }

  public File generateGrooveGrammar(Behavior behavior, File targetFolder) {
    ValueWrapper<File> result = new ValueWrapper<>();
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
                    collaboration, targetFolder, false));
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

  private File generateGrooveGrammarForActivityDiagram(
      ActivityDiagram activityDiagram, File targetFolder) {
    File graphGrammarSubFolder = this.makeSubFolder(activityDiagram, targetFolder);
    ActivityDiagramToGrooveTransformer transformer = new ActivityDiagramToGrooveTransformer(true);

    transformer.generateAndWriteStartGraph(activityDiagram, graphGrammarSubFolder);

    transformer.generateAndWriteRules(activityDiagram, graphGrammarSubFolder);

    final Map<String, String> additionalProperties = Maps.newHashMap();
    additionalProperties.put(TYPE_GRAPH, ACTIVITY_DIAGRAM_TYPE_GRAPH_FILE_NAME);
    this.generatePropertiesFile(graphGrammarSubFolder, START, additionalProperties);

    return graphGrammarSubFolder;
  }

  private File generateGrooveGrammarForPiProcess(NamedPiProcess piProcess, File grooveDir) {
    File graphGrammarSubFolder = this.makeSubFolder(piProcess, grooveDir);
    PiCalcToGrooveTransformer transformer = new PiCalcToGrooveTransformer();

    transformer.generateAndWriteStartGraph(piProcess, graphGrammarSubFolder);

    transformer.generateAndWriteRules(piProcess, graphGrammarSubFolder);

    final Map<String, String> additionalProperties = Maps.newHashMap();
    additionalProperties.put(TYPE_GRAPH, PI_TYPE_GRAPH_FILE_NAME);
    additionalProperties.put("checkDangling", "true");
    this.generatePropertiesFile(graphGrammarSubFolder, START, additionalProperties);

    return graphGrammarSubFolder;
  }

  public File generateGrooveGrammarForBPMNProcessModel(
      BPMNCollaboration collaboration, File grooveDir, boolean useSFId) {
    File graphGrammarSubFolder = this.makeSubFolder(collaboration, grooveDir);
    BPMNToGrooveTransformer transformer = new BPMNToGrooveTransformer(useSFId);

    // Generate start graph
    transformer.generateAndWriteStartGraph(collaboration, graphGrammarSubFolder);
    // Generate rules
    transformer.generateAndWriteRules(collaboration, graphGrammarSubFolder);

    final Map<String, String> additionalProperties = Maps.newHashMap();
    additionalProperties.put(TYPE_GRAPH, BPMN_DIAGRAM_TYPE_GRAPH_FILE_NAME);
    this.generatePropertiesFile(graphGrammarSubFolder, START, additionalProperties);

    return graphGrammarSubFolder;
  }

  private File generateGrooveGrammarForPN(PetriNet petriNet, File grooveDir) {
    File graphGrammarSubFolder = this.makeSubFolder(petriNet, grooveDir);
    PNToGrooveTransformer transformer = new PNToGrooveTransformer();

    // Generate start graph
    transformer.generateAndWriteStartGraph(petriNet, graphGrammarSubFolder);

    // Generate rules
    transformer.generateAndWriteRules(petriNet, graphGrammarSubFolder);

    this.generatePropertiesFile(graphGrammarSubFolder, START, Maps.newHashMap());

    return graphGrammarSubFolder;
  }

  private File generateGrooveGrammarForFSM(FiniteStateMachine finiteStateMachine, File grooveDir) {
    File graphGrammarSubFolder = this.makeSubFolder(finiteStateMachine, grooveDir);
    FSMToGrooveTransformer transformer = new FSMToGrooveTransformer();

    // Generate start graph
    transformer.generateAndWriteStartGraph(finiteStateMachine, graphGrammarSubFolder);
    // Generate rules
    transformer.generateAndWriteRules(finiteStateMachine, graphGrammarSubFolder);

    final Map<String, String> additionalProperties = Maps.newHashMap();
    additionalProperties.put(TYPE_GRAPH, FSM_TYPE_GRAPH_FILE_NAME);
    this.generatePropertiesFile(graphGrammarSubFolder, START, additionalProperties);

    return graphGrammarSubFolder;
  }

  private File makeSubFolder(Behavior behavior, File grooveDir) {
    return this.makeSubFolder(behavior.getName(), grooveDir);
  }

  private File makeSubFolder(String folderName, File grooveDir) {
    File graphGrammarSubFolder = new File(grooveDir + File.separator + folderName + ".gps");
    if (!graphGrammarSubFolder.mkdirs()) {
      // Clean dir if not fresh
      cleanSubDir(graphGrammarSubFolder);
    }
    return graphGrammarSubFolder;
  }

  private static void cleanSubDir(File graphGrammarSubFolder) {
    try {
      FileUtils.cleanDirectory(graphGrammarSubFolder);
    } catch (IOException e) {
      throw new FolderCouldNotBeCleanedException(
          String.format(
              "The subfolder %s for the groove rule generation could not be cleaned.",
              graphGrammarSubFolder.getAbsolutePath()),
          e);
    }
  }

  private void generatePropertiesFile(
      File subFolder, String startGraph, Map<String, String> additionalProperties) {
    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    LocalDateTime now = LocalDateTime.now(ZoneId.systemDefault());

    String propertiesContent =
        String.format(
            "# %s (Groove rule generator)%nlocation=%s%nstartGraph=%s%n%sgrooveVersion=5.8.1%ngrammarVersion=3.7",
            dtf.format(now),
            subFolder.getPath(),
            startGraph,
            this.getAdditionalProperties(additionalProperties));
    File propertiesFile = new File(subFolder + File.separator + "system.properties");
    try {
      FileUtils.writeStringToFile(propertiesFile, propertiesContent, StandardCharsets.UTF_8, false);
    } catch (IOException e) {
      throw new ShouldNotHappenRuntimeException(e);
    }
  }

  private String getAdditionalProperties(Map<String, String> additionalProperties) {
    return additionalProperties.entrySet().stream()
        .reduce("", (prop1, prop2) -> prop1 + prop2 + "\n", (key, value) -> key + "=" + value);
  }
}
