package groove.behaviorTransformer;

import behavior.fsm.FiniteStateMachine;
import groove.graph.GrooveRuleGenerator;

import java.io.File;

public class FSMToGrooveTransformer {

    void generateFSMStartGraphFile(FiniteStateMachine finiteStateMachine, File targetFolder) {
        final String startStateName = finiteStateMachine.getStartState().getName();

        BehaviorToGrooveTransformer.createStartGraphWithOneNode(targetFolder, startStateName);
    }

    void generateFSMRules(FiniteStateMachine finiteStateMachine, File subFolder) {
        this.generateFSMRules(finiteStateMachine, subFolder, false);
    }

    void generateFSMRules(FiniteStateMachine finiteStateMachine, File subFolder, Boolean addPrefix) {
        GrooveRuleGenerator ruleGenerator = new GrooveRuleGenerator();
        finiteStateMachine.getTransitions().forEach(transition -> {
            String potentialPrefix = addPrefix ? finiteStateMachine.getName() + "_" : "";
            ruleGenerator.startRule(potentialPrefix + transition.getName());

            ruleGenerator.deleteNode(potentialPrefix + transition.getSource().getName());
            ruleGenerator.addNode(potentialPrefix + transition.getTarget().getName());

            ruleGenerator.generateRule();
        });
        ruleGenerator.writeRules(subFolder);
    }
}
