package no.tk.groove.behaviortransformer;

import java.nio.file.Path;
import no.tk.behavior.Behavior;
import no.tk.behavior.BehaviorVisitor;
import no.tk.behavior.activity.ActivityDiagram;
import no.tk.behavior.bpmn.BPMNCollaboration;
import no.tk.behavior.fsm.FiniteStateMachine;
import no.tk.behavior.petrinet.PetriNet;
import no.tk.behavior.picalculus.NamedPiProcess;
import no.tk.groove.behaviortransformer.bpmn.BPMNToGrooveTransformer;
import no.tk.util.ValueWrapper;

public class BehaviorToGrooveTransformer {

  private static final String ACTIVITY_DIAGRAM_TYPE_GRAPH_FILE_NAME = "type";
  private static final String PI_TYPE_GRAPH_FILE_NAME = "Type";
  private static final String BPMN_DIAGRAM_TYPE_GRAPH_FILE_NAME = "bpmn_e_model";
  private static final String FSM_TYPE_GRAPH_FILE_NAME = "fsm_e_model";
  public static final String TYPE_GRAPH = "typeGraph";
  public static final String MATCH_INJECTIVE = "matchInjective";
  public static final String CHECK_DANGLING = "checkDangling";
  private final boolean layout;

  public BehaviorToGrooveTransformer(boolean layout) {
    this.layout = layout;
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
    ActivityDiagramToGrooveTransformer transformer = new ActivityDiagramToGrooveTransformer(true);
    transformer
        .builder
        .name(activityDiagram.getName())
        .addProperty(TYPE_GRAPH, ACTIVITY_DIAGRAM_TYPE_GRAPH_FILE_NAME);

    return transformer.buildAndWriteGTS(activityDiagram, targetFolder);
  }

  private Path generateGrooveGrammarForPiProcess(NamedPiProcess piProcess, Path targetFolder) {
    PiCalcToGrooveTransformer transformer = new PiCalcToGrooveTransformer(layout);
    transformer
        .builder
        .name(piProcess.getName())
        .addProperty(TYPE_GRAPH, PI_TYPE_GRAPH_FILE_NAME)
        .addProperty(CHECK_DANGLING, "true");

    return transformer.buildAndWriteGTS(piProcess, targetFolder);
  }

  public Path generateGrooveGrammarForBPMNProcessModel(
      BPMNCollaboration collaboration, Path targetFolder) {
    BPMNToGrooveTransformer transformer = new BPMNToGrooveTransformer(layout);

    transformer
        .builder
        .name(collaboration.getName())
        .addProperty(MATCH_INJECTIVE, "true")
        .addProperty(TYPE_GRAPH, BPMN_DIAGRAM_TYPE_GRAPH_FILE_NAME);

    return transformer.buildAndWriteGTS(collaboration, targetFolder);
  }

  private Path generateGrooveGrammarForPN(PetriNet petriNet, Path targetFolder) {
    return new PNToGrooveTransformer(layout).buildAndWriteGTS(petriNet, targetFolder);
  }

  private Path generateGrooveGrammarForFSM(
      FiniteStateMachine finiteStateMachine, Path targetFolder) {
    FSMToGrooveTransformer transformer = new FSMToGrooveTransformer(layout);
    transformer
        .builder
        .name(finiteStateMachine.getName())
        .addProperty(TYPE_GRAPH, FSM_TYPE_GRAPH_FILE_NAME);

    return transformer.buildAndWriteGTS(finiteStateMachine, targetFolder);
  }
}
