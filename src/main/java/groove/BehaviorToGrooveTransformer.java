package groove;

import behavior.Behavior;
import behavior.BehaviorVisitor;
import behavior.fsm.FiniteStateMachine;
import groove.gxl.Graph;
import groove.gxl.Gxl;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class BehaviorToGrooveTransformer {
    void generateGrooveGrammar(Behavior behavior, File targetFolder) {
        behavior.handle(new BehaviorVisitor() {
            @Override
            public void accept(FiniteStateMachine finiteStateMachine) {
                BehaviorToGrooveTransformer.this.generateGrooveGrammarForAFSM(finiteStateMachine, targetFolder);
            }
        });
    }

    private void generateGrooveGrammarForAFSM(FiniteStateMachine finiteStateMachine, File grooveDir) {
        File graphGrammarSubFolder = new File(grooveDir + "/" + finiteStateMachine.getName() + ".gps");
        graphGrammarSubFolder.mkdir();

        this.generateFSMStartGraphFile(finiteStateMachine, graphGrammarSubFolder);

        this.generateFSMRules(finiteStateMachine, graphGrammarSubFolder);

        this.generatePropertiesFile(graphGrammarSubFolder);
    }

    private void generateFSMRules(FiniteStateMachine finiteStateMachine, File subFolder) {
        GrooveRuleGenerator ruleGenerator = new GrooveRuleGenerator();
        finiteStateMachine.getTransitions().forEach(transition -> {
            ruleGenerator.startRule(transition.getName());

            ruleGenerator.deleteNode(transition.getSource().getName());
            ruleGenerator.addNode(transition.getTarget().getName());

            ruleGenerator.generateRule();
            ruleGenerator.writeRules(subFolder);
        });
    }

    private void generatePropertiesFile(File subFolder) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();

        String propertiesContent = String.format("# %s (Groove rule generator)\n" +
                        "location=%s\n" +
                        "startGraph=start\n" +
                        "grooveVersion=5.8.1\n" +
                        "grammarVersion=3.7",
                dtf.format(now),
                subFolder.getPath());
        File properties_file = new File(subFolder + "/" + "system.properties");
        try {
            FileUtils.writeStringToFile(properties_file, propertiesContent, StandardCharsets.UTF_8, false);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void generateFSMStartGraphFile(FiniteStateMachine finiteStateMachine, File targetFolder) {
        Gxl gxl = new Gxl();
        Graph graph = GxlHelper.createStandardGxlGraph("start", gxl);
        GxlHelper.createNodeWithName("n0", finiteStateMachine.getStartState().getName(), graph);

        File startGraphFile = new File(targetFolder.getPath() + "/start.gst");
        GxlToXMLConverter.toXml(gxl, startGraphFile);
    }
}
