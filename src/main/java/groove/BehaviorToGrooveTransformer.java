package groove;

import behavior.Behavior;
import behavior.BehaviorVisitor;
import behavior.fsm.FiniteStateMachine;
import groove.gxl.Graph;
import groove.gxl.Gxl;

import java.io.File;

public class BehaviorToGrooveTransformer {
    void generateGrooveGrammar(Behavior behavior, File targetFolder) {
        behavior.handle(new BehaviorVisitor() {
            @Override
            public void accept(FiniteStateMachine finiteStateMachine) {
                BehaviorToGrooveTransformer.this.generateGrooveGrammarForAFSM(finiteStateMachine, targetFolder);
            }
        });
    }

    private void generateGrooveGrammarForAFSM(FiniteStateMachine finiteStateMachine, File targetFolder) {
        GrooveRuleGenerator ruleGenerator = new GrooveRuleGenerator();
        finiteStateMachine.getTransitions().forEach(transition -> {
            ruleGenerator.startRule(transition.getName());

            ruleGenerator.deleteNode(transition.getSource().getName());
            ruleGenerator.addNode(transition.getTarget().getName());

            ruleGenerator.generateRule();
            ruleGenerator.writeRules(targetFolder);
        });

        // Generate start graph.
        this.generateStartGraphFile(finiteStateMachine, targetFolder);

        // Generate system.properties file pointing to the start graph.
    }

    private void generateStartGraphFile(FiniteStateMachine finiteStateMachine, File targetFolder) {
        Gxl gxl = new Gxl();
        Graph graph = GxlHelper.createStandardGxlGraph("start", gxl);
        GxlHelper.createNodeWithName("n0", finiteStateMachine.getStartState().getName(), graph);

        File startGraphFile = new File(targetFolder.getPath() + "/start.gst");
        GxlToXMLConverter.toXml(gxl, startGraphFile);
    }
}
