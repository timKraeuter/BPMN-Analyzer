package groove.behaviorTransformer;

import behavior.fsm.FiniteStateMachine;
import com.google.common.collect.Sets;
import groove.graph.GrooveGraph;
import groove.graph.GrooveNode;
import groove.graph.rule.GrooveGraphRule;
import groove.graph.rule.GrooveRuleBuilder;

import java.util.stream.Stream;

public class FSMToGrooveTransformer implements GrooveTransformer<FiniteStateMachine> {

    @Override
    public GrooveGraph generateStartGraph(FiniteStateMachine finiteStateMachine, boolean addPrefix) {
        String potentialPrefix = this.getPrefixOrEmpty(finiteStateMachine, addPrefix);
        GrooveNode startStateNode = new GrooveNode(potentialPrefix + finiteStateMachine.getStartState().getName());
        return new GrooveGraph(finiteStateMachine.getName(), Sets.newHashSet(startStateNode), Sets.newHashSet());
    }

    @Override
    public Stream<GrooveGraphRule> generateRules(FiniteStateMachine finiteStateMachine, boolean addPrefix) {
        GrooveRuleBuilder ruleBuilder = new GrooveRuleBuilder(finiteStateMachine, addPrefix);
        finiteStateMachine.getTransitions().forEach(transition -> {
            ruleBuilder.startRule(transition.getName());

            ruleBuilder.deleteNode(transition.getSource().getName());
            ruleBuilder.addNode(transition.getTarget().getName());

            ruleBuilder.buildRule();
        });
        return ruleBuilder.getRules();
    }

    @Override
    public boolean isLayoutActivated() {
        return true; // TODO: implement layout as parameter!
    }

    private String getPrefixOrEmpty(FiniteStateMachine finiteStateMachine, Boolean addPrefix) {
        return addPrefix ? finiteStateMachine.getName() + "_" : "";
    }
}
