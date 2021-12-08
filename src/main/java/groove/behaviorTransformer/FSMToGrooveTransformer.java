package groove.behaviorTransformer;

import behavior.fsm.FiniteStateMachine;
import com.google.common.collect.Sets;
import groove.graph.GrooveGraph;
import groove.graph.GrooveNode;
import groove.graph.GrooveRuleGenerator;

import java.io.File;

public class FSMToGrooveTransformer {

    public GrooveGraph generateStartGraph(FiniteStateMachine finiteStateMachine, boolean addPrefix) {
        String potentialPrefix = this.getPrefixOrEmpty(finiteStateMachine, addPrefix);
        GrooveNode startStateNode = new GrooveNode(potentialPrefix + finiteStateMachine.getStartState().getName());
        return new GrooveGraph(finiteStateMachine.getName(), Sets.newHashSet(startStateNode), Sets.newHashSet());
    }

    GrooveGraph generateStartGraph(FiniteStateMachine finiteStateMachine) {
        return this.generateStartGraph(finiteStateMachine, false);
    }

    void generateFSMRules(FiniteStateMachine finiteStateMachine, File subFolder) {
        this.generateFSMRules(finiteStateMachine, subFolder, false);
    }

    void generateFSMRules(FiniteStateMachine finiteStateMachine, File subFolder, Boolean addPrefix) {
        GrooveRuleGenerator ruleGenerator = new GrooveRuleGenerator();
        finiteStateMachine.getTransitions().forEach(transition -> {
            String potentialPrefix = this.getPrefixOrEmpty(finiteStateMachine, addPrefix);
            ruleGenerator.startRule(potentialPrefix + transition.getName());

            ruleGenerator.deleteNode(potentialPrefix + transition.getSource().getName());
            ruleGenerator.addNode(potentialPrefix + transition.getTarget().getName());

            ruleGenerator.generateRule();
        });
        ruleGenerator.writeRules(subFolder);
    }

    private String getPrefixOrEmpty(FiniteStateMachine finiteStateMachine, Boolean addPrefix) {
        return addPrefix ? finiteStateMachine.getName() + "_" : "";
    }
}
