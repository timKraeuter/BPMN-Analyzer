package groove.behaviorTransformer;

import behavior.fsm.FiniteStateMachine;
import com.google.common.collect.Sets;
import groove.graph.GrooveGraph;
import groove.graph.GrooveNode;
import groove.graph.GrooveRuleGenerator;

public class FSMToGrooveTransformer implements GrooveTransformer<FiniteStateMachine> {

    @Override
    public GrooveGraph generateStartGraph(FiniteStateMachine finiteStateMachine, boolean addPrefix) {
        String potentialPrefix = this.getPrefixOrEmpty(finiteStateMachine, addPrefix);
        GrooveNode startStateNode = new GrooveNode(potentialPrefix + finiteStateMachine.getStartState().getName());
        return new GrooveGraph(finiteStateMachine.getName(), Sets.newHashSet(startStateNode), Sets.newHashSet());
    }

    @Override
    public GrooveRuleGenerator generateRules(FiniteStateMachine finiteStateMachine, boolean addPrefix) {
        GrooveRuleGenerator ruleGenerator = new GrooveRuleGenerator(finiteStateMachine, addPrefix);
        finiteStateMachine.getTransitions().forEach(transition -> {
            ruleGenerator.startRule(transition.getName());

            ruleGenerator.deleteNode(transition.getSource().getName());
            ruleGenerator.addNode(transition.getTarget().getName());

            ruleGenerator.generateRule();
        });
        return ruleGenerator;
    }

    private String getPrefixOrEmpty(FiniteStateMachine finiteStateMachine, Boolean addPrefix) {
        return addPrefix ? finiteStateMachine.getName() + "_" : "";
    }
}
