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
        GrooveRuleGenerator ruleGenerator = new GrooveRuleGenerator();
        finiteStateMachine.getTransitions().forEach(transition -> {
            ruleGenerator.startRule(transition.getName());

            ruleGenerator.deleteNode(transition.getSource().getName());
            ruleGenerator.addNode(transition.getTarget().getName());

            ruleGenerator.generateRule();
        });
        ruleGenerator.writeRules(subFolder);
    }
}
