package groove.behaviortransformer;

import behavior.bpmn.auxiliary.exceptions.ShouldNotHappenRuntimeException;
import behavior.fsm.FiniteStateMachine;
import groove.graph.GrooveGraph;
import groove.graph.GrooveGraphBuilder;
import groove.graph.GrooveNode;
import groove.graph.rule.GrooveGraphRule;
import groove.graph.rule.GrooveRuleBuilder;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.stream.Stream;

import static groove.behaviortransformer.GrooveTransformerHelper.createStringNodeLabel;

public class FSMToGrooveTransformer implements GrooveTransformer<FiniteStateMachine> {

    public static final String FSM_TYPE_GRAPH_DIR = "/StateMachineTypeGraph";

    // Node types
    private static final String TYPE_STATE = TYPE + "State";
    private static final String TYPE_STATE_MACHINE_SNAPSHOT = TYPE + "StateMachineSnapshot";

    // Edge names/attribute names
    private static final String NAME = "name";
    private static final String CURRENT_STATE = "currentState";

    @Override
    public void generateAndWriteRulesFurther(FiniteStateMachine fsm, File targetFolder) {
        this.copyTypeGraph(targetFolder);
    }

    private void copyTypeGraph(File targetFolder) {
        //noinspection ConstantConditions must be present!. Otherwise, tests will also fail!
        File sourceDirectory = new File(this.getClass().getResource(FSM_TYPE_GRAPH_DIR).getFile());
        try {
            FileUtils.copyDirectory(sourceDirectory, targetFolder);
        }
        catch (IOException e) {
            throw new ShouldNotHappenRuntimeException(e);
        }
    }

    @Override
    public GrooveGraph generateStartGraph(FiniteStateMachine finiteStateMachine) {
        final String stateMachineName = finiteStateMachine.getName();
        final GrooveGraphBuilder builder = new GrooveGraphBuilder().setName(stateMachineName);
        GrooveNode startStateNode = new GrooveNode(TYPE_STATE);
        GrooveNode stateMachineNode = new GrooveNode(TYPE_STATE_MACHINE_SNAPSHOT);
        builder.addEdge(CURRENT_STATE, stateMachineNode, startStateNode);

        builder.addEdge(NAME, stateMachineNode, new GrooveNode(createStringNodeLabel(stateMachineName)));
        final String startStateName = finiteStateMachine.getStartState().getName();
        builder.addEdge(NAME, startStateNode, new GrooveNode(createStringNodeLabel(startStateName)));

        return builder.build();
    }

    @Override
    public Stream<GrooveGraphRule> generateRules(FiniteStateMachine finiteStateMachine) {
        GrooveRuleBuilder ruleBuilder = new GrooveRuleBuilder();
        finiteStateMachine.getTransitions().forEach(transition -> {
            ruleBuilder.startRule(transition.getName());

            final GrooveNode stateMachine = ruleBuilder.contextNode(TYPE_STATE_MACHINE_SNAPSHOT);
            ruleBuilder.contextEdge(NAME,
                                    stateMachine,
                                    ruleBuilder.contextNode(createStringNodeLabel(finiteStateMachine.getName())));

            final GrooveNode previousState = ruleBuilder.deleteNode(TYPE_STATE);
            ruleBuilder.contextEdge(
                    NAME,
                    previousState,
                    ruleBuilder.contextNode(createStringNodeLabel(transition.getSource().getName())));
            ruleBuilder.deleteEdge(CURRENT_STATE, stateMachine, previousState);

            final GrooveNode newState = ruleBuilder.addNode(TYPE_STATE);
            ruleBuilder.contextEdge(
                    NAME,
                    newState,
                    ruleBuilder.contextNode(createStringNodeLabel(transition.getTarget().getName())));
            ruleBuilder.addEdge(CURRENT_STATE, stateMachine, newState);

            ruleBuilder.buildRule();
        });
        return ruleBuilder.getRules();
    }

    @Override
    public boolean isLayoutActivated() {
        return true; // TODO: implement layout as parameter!
    }

    private String getPrefixOrEmpty(FiniteStateMachine finiteStateMachine, boolean addPrefix) {
        return addPrefix ? finiteStateMachine.getName() + "_" : "";
    }
}
