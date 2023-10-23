package no.tk.groove.behaviortransformer.bpmn;

import static no.tk.groove.behaviortransformer.GrooveTransformerHelper.createStringNodeLabel;

import io.github.timkraeuter.groove.graph.GrooveGraphBuilder;
import io.github.timkraeuter.groove.graph.GrooveNode;
import io.github.timkraeuter.groove.rule.GrooveRuleBuilder;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import no.tk.behavior.bpmn.BPMNCollaboration;
import no.tk.behavior.bpmn.BPMNProcess;
import no.tk.behavior.bpmn.auxiliary.exceptions.ShouldNotHappenRuntimeException;
import no.tk.behavior.bpmn.events.StartEvent;
import no.tk.behavior.bpmn.events.StartEventType;
import no.tk.groove.behaviortransformer.GrooveTransformer;

public class BPMNToGrooveTransformer extends GrooveTransformer<BPMNCollaboration> {

  public static final String TYPE_GRAPH_FILE_NAME = "bpmn_e_model.gty";
  public static final String TERMINATE_RULE_FILE_NAME = "Terminate.gpr";
  // Graph conditions for model-checking
  public static final String ALL_TERMINATED_FILE_NAME = "AllTerminated.gpr";
  public static final String UNSAFE_FILE_NAME = "Unsafe.gpr";

  public BPMNToGrooveTransformer(boolean layout) {
    super(layout);
  }

  @Override
  public void generateStartGraph(
      BPMNCollaboration collaboration, GrooveGraphBuilder startGraphBuilder) {
    startGraphBuilder.name(collaboration.getName());

    collaboration.getParticipants().stream()
        .filter(
            process ->
                process.getStartEvents().stream()
                    .anyMatch(startEvent -> startEvent.getType() == StartEventType.NONE))
        .forEach(
            process -> {
              GrooveNode processInstance =
                  new GrooveNode(BPMNToGrooveTransformerConstants.TYPE_PROCESS_SNAPSHOT);
              GrooveNode processName = new GrooveNode(createStringNodeLabel(process.getName()));
              startGraphBuilder.addEdge(
                  BPMNToGrooveTransformerConstants.NAME, processInstance, processName);
              GrooveNode running = new GrooveNode(BPMNToGrooveTransformerConstants.TYPE_RUNNING);
              startGraphBuilder.addEdge(
                  BPMNToGrooveTransformerConstants.STATE, processInstance, running);
              addStartTokens(startGraphBuilder, process, processInstance);
            });
  }

  private void addStartTokens(
      GrooveGraphBuilder startGraphBuilder, BPMNProcess process, GrooveNode processInstance) {
    process
        .getStartEvents()
        .forEach(
            startEvent -> {
              if (startEvent.getType() == StartEventType.NONE) {
                addTokenToEachOutgoingFlow(startGraphBuilder, processInstance, startEvent);
              }
            });
  }

  private void addTokenToEachOutgoingFlow(
      GrooveGraphBuilder startGraphBuilder, GrooveNode processInstance, StartEvent startEvent) {
    startEvent
        .getOutgoingFlows()
        .forEach(
            incFlow -> {
              GrooveNode token = new GrooveNode(BPMNToGrooveTransformerConstants.TYPE_TOKEN);
              GrooveNode tokenName =
                  new GrooveNode(createStringNodeLabel(incFlow.getDescriptiveName()));
              startGraphBuilder.addEdge(
                  BPMNToGrooveTransformerConstants.POSITION, token, tokenName);
              startGraphBuilder.addEdge(
                  BPMNToGrooveTransformerConstants.TOKENS, processInstance, token);
            });
  }

  @Override
  public void generateRules(BPMNCollaboration collaboration, GrooveRuleBuilder ruleBuilder) {
    // TODO: fix: generate called in constructor.
    new BPMNRuleGenerator(ruleBuilder, collaboration);
  }

  @Override
  public void generateAndWriteRulesFurther(BPMNCollaboration collaboration, Path targetFolder) {
    this.copyTypeGraphAndFixedRules(targetFolder);
  }

  private void copyTypeGraphAndFixedRules(Path targetFolder) {
    InputStream typeGraph =
        this.getClass()
            .getResourceAsStream(
                BPMNToGrooveTransformerConstants.FIXED_RULES_AND_TYPE_GRAPH_DIR
                    + TYPE_GRAPH_FILE_NAME);
    InputStream terminateRule =
        this.getClass()
            .getResourceAsStream(
                BPMNToGrooveTransformerConstants.FIXED_RULES_AND_TYPE_GRAPH_DIR
                    + TERMINATE_RULE_FILE_NAME);
    InputStream unsafeGraph =
        this.getClass()
            .getResourceAsStream(
                BPMNToGrooveTransformerConstants.FIXED_RULES_AND_TYPE_GRAPH_DIR + UNSAFE_FILE_NAME);
    InputStream allterminatedGraph =
        this.getClass()
            .getResourceAsStream(
                BPMNToGrooveTransformerConstants.FIXED_RULES_AND_TYPE_GRAPH_DIR
                    + ALL_TERMINATED_FILE_NAME);
    try {
      Files.copy(typeGraph, Path.of(targetFolder.toString(), TYPE_GRAPH_FILE_NAME));
      Files.copy(terminateRule, Path.of(targetFolder.toString(), TERMINATE_RULE_FILE_NAME));
      Files.copy(unsafeGraph, Path.of(targetFolder.toString(), UNSAFE_FILE_NAME));
      Files.copy(allterminatedGraph, Path.of(targetFolder.toString(), ALL_TERMINATED_FILE_NAME));
    } catch (IOException e) {
      throw new ShouldNotHappenRuntimeException(e);
    }
  }
}
