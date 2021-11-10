package groove;

import org.apache.commons.io.FileUtils;
import org.eclipse.elk.core.RecursiveGraphLayoutEngine;
import org.eclipse.elk.core.util.BasicProgressMonitor;
import org.eclipse.elk.graph.ElkEdge;
import org.eclipse.elk.graph.ElkNode;
import org.eclipse.elk.graph.util.ElkGraphUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import util.FileTestHelper;

import java.io.File;

public class RuleGenerationTest {

    @BeforeEach
    void setUp() {
        GrooveNode.idCounter.set(-1);
    }

    @Test
    void generateAddNodeRuleTest() {
        File tempDir = FileUtils.getTempDirectory();

        GrooveRuleGenerator ruleGenerator = new GrooveRuleGenerator();
        ruleGenerator.startRule("addSingleNode");
        ruleGenerator.addNode("node");
        ruleGenerator.generateRule();
        ruleGenerator.writeRules(tempDir);

        File expected_rule = new File(this.getClass().getResource("/addSingleNode.gpr").getFile());
        File generated_rule = new File(tempDir + "/addSingleNode.gpr");
        FileTestHelper.testFileEquals(expected_rule, generated_rule);
    }

    @Test
    void generateDeleteNodeRuleTest() {
        File tempDir = FileUtils.getTempDirectory();

        GrooveRuleGenerator ruleGenerator = new GrooveRuleGenerator();
        ruleGenerator.startRule("deleteSingleNode");
        ruleGenerator.deleteNode("node");
        ruleGenerator.generateRule();
        ruleGenerator.writeRules(tempDir);

        File expected_rule = new File(this.getClass().getResource("/deleteSingleNode.gpr").getFile());
        File generated_rule = new File(tempDir + "/deleteSingleNode.gpr");
        FileTestHelper.testFileEquals(expected_rule, generated_rule);
    }

    @Test
    void generateAddEdgeRuleTest() {
        File tempDir = FileUtils.getTempDirectory();

        GrooveRuleGenerator ruleGenerator = new GrooveRuleGenerator();
        ruleGenerator.startRule("addNodesWithEdge");
        GrooveNode source = ruleGenerator.addNode("source");
        GrooveNode target = ruleGenerator.addNode("target");
        ruleGenerator.addEdge("edge", source, target);
        ruleGenerator.generateRule();
        ruleGenerator.writeRules(tempDir);

        File expected_rule = new File(this.getClass().getResource("/addNodesWithEdge.gpr").getFile());
        File generated_rule = new File(tempDir + "/addNodesWithEdge.gpr");
        FileTestHelper.testFileEquals(expected_rule, generated_rule);
    }
}
