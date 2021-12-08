package groove;

import com.google.common.collect.Sets;
import groove.graph.GrooveGraphRule;
import groove.graph.GrooveNode;
import groove.graph.GrooveRuleGenerator;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import util.FileTestHelper;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@SuppressWarnings("ConstantConditions")
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

    @Test
    void generateSynchedRulesTest() {
        File tempDir = FileUtils.getTempDirectory();

        GrooveRuleGenerator generator1 = new GrooveRuleGenerator();
        generator1.startRule("addEdge1");
        GrooveNode s1 = generator1.addNode("s1");
        GrooveNode t1 = generator1.addNode("t1");
        generator1.addEdge("edge1", s1, t1);
        GrooveGraphRule r1 = generator1.generateRule();

        generator1.startRule("addEdge2");
        GrooveNode s2 = generator1.addNode("s2");
        GrooveNode t2 = generator1.addNode("t2");
        generator1.addEdge("edge2", s2, t2);
        GrooveGraphRule r2 = generator1.generateRule();

        Map<String, Set<GrooveGraphRule>> nameToToBeSynchedRules = new HashMap<>();
        nameToToBeSynchedRules.put("addEdge1AddEdge2", Sets.newHashSet(r1, r2));
        GrooveRuleGenerator synchGenerator = GrooveRuleGenerator.createSynchedRules(nameToToBeSynchedRules);

        synchGenerator.writeRules(tempDir);

        File expected_rule = new File(this.getClass().getResource("/addEdge1AddEdge2.gpr").getFile());
        File generated_rule = new File(tempDir + "/addEdge1AddEdge2.gpr");
        FileTestHelper.testFileEquals(expected_rule, generated_rule);
    }
}
