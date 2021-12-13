package groove.behaviorTransformer;

import behavior.fsm.FiniteStateMachine;
import com.google.common.collect.Sets;
import groove.graph.GrooveGraph;
import groove.graph.GrooveNode;
import groove.graph.GrooveRuleBuilder;

public class FSMToGrooveTransformer implements GrooveTransformer<FiniteStateMachine> {

    @Override
    public GrooveGraph generateStartGraph(FiniteStateMachine finiteStateMachine, boolean addPrefix) {
        String potentialPrefix = this.getPrefixOrEmpty(finiteStateMachine, addPrefix);
        GrooveNode startStateNode = new GrooveNode(potentialPrefix + finiteStateMachine.getStartState().getName());
        return new GrooveGraph(finiteStateMachine.getName(), Sets.newHashSet(startStateNode), Sets.newHashSet());
    }

    @Override
    public GrooveRuleBuilder generateRules(FiniteStateMachine finiteStateMachine, boolean addPrefix) {
        GrooveRuleBuilder ruleBuilder = new GrooveRuleBuilder(finiteStateMachine, addPrefix);
        finiteStateMachine.getTransitions().forEach(transition -> {
            ruleBuilder.startRule(transition.getName());

            ruleBuilder.deleteNode(transition.getSource().getName());
            ruleBuilder.addNode(transition.getTarget().getName());

            ruleBuilder.buildRule();
        });
        return ruleBuilder;
    }

    private String getPrefixOrEmpty(FiniteStateMachine finiteStateMachine, Boolean addPrefix) {
        return addPrefix ? finiteStateMachine.getName() + "_" : "";
    }
}
