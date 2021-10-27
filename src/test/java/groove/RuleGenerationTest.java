package groove;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class RuleGenerationTest {

    @BeforeEach
    void setUp() {
        GrooveNode.idCounter.set(-1);
    }

    @Test
    void generateAddNodeRuleTest() throws IOException {
        File tempDir = FileUtils.getTempDirectory();

        GrooveRuleGenerator ruleGenerator = new GrooveRuleGenerator();
        ruleGenerator.newRule("addSingleNode");
        ruleGenerator.addNode("node");
        ruleGenerator.generateRule();
        ruleGenerator.writeRules(tempDir);

        File expected_rule = new File(this.getClass().getResource("/addSingleNode.gpr").getFile());
        File generated_rule = new File(tempDir + "/addSingleNode.gpr");
        assertThat(
                FileUtils.readFileToString(generated_rule, StandardCharsets.UTF_8),
                is(FileUtils.readFileToString(expected_rule, StandardCharsets.UTF_8)));
    }

    @Test
    void generateDeleteNodeRuleTest() throws IOException {
        File tempDir = FileUtils.getTempDirectory();

        GrooveRuleGenerator ruleGenerator = new GrooveRuleGenerator();
        ruleGenerator.newRule("deleteSingleNode");
        ruleGenerator.deleteNode("node");
        ruleGenerator.generateRule();
        ruleGenerator.writeRules(tempDir);

        File expected_rule = new File(this.getClass().getResource("/deleteSingleNode.gpr").getFile());
        File generated_rule = new File(tempDir + "/deleteSingleNode.gpr");
        assertThat(
                FileUtils.readFileToString(generated_rule, StandardCharsets.UTF_8),
                is(FileUtils.readFileToString(expected_rule, StandardCharsets.UTF_8)));
    }

    @Test
    void generateAddEdgeRuleTest() throws IOException {
        File tempDir = FileUtils.getTempDirectory();

        GrooveRuleGenerator ruleGenerator = new GrooveRuleGenerator();
        ruleGenerator.newRule("addNodesWithEdge");
        GrooveNode source = ruleGenerator.addNode("source");
        GrooveNode target = ruleGenerator.addNode("target");
        ruleGenerator.addEdge("edge", source, target);
        ruleGenerator.generateRule();
        ruleGenerator.writeRules(tempDir);

        File expected_rule = new File(this.getClass().getResource("/addNodesWithEdge.gpr").getFile());
        File generated_rule = new File(tempDir + "/addNodesWithEdge.gpr");
        assertThat(
                FileUtils.readFileToString(generated_rule, StandardCharsets.UTF_8),
                is(FileUtils.readFileToString(expected_rule, StandardCharsets.UTF_8)));
    }
}
