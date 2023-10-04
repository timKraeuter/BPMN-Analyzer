package no.tk.groove;

import static no.tk.util.FileTestHelper.getResource;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import no.tk.groove.graph.GrooveNode;
import no.tk.groove.graph.rule.GrooveGraphRule;
import no.tk.groove.graph.rule.GrooveRuleBuilder;
import no.tk.groove.graph.rule.GrooveRuleWriter;
import no.tk.util.FileTestHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@SuppressWarnings("ConstantConditions")
class RuleGenerationTest {

  @BeforeEach
  void setUp() {
    GrooveNode.idCounter.set(-1);
  }

  @Test
  void generateAddNodeRuleTest() throws Exception {
    Path tempDir = Files.createTempDirectory("");

    GrooveRuleBuilder ruleBuilder = new GrooveRuleBuilder();
    ruleBuilder.startRule("addSingleNode");
    ruleBuilder.addNode("node");
    ruleBuilder.buildRule();
    GrooveRuleWriter.writeRules(tempDir, ruleBuilder.getRules(), true);

    Path expected_rule = getResource("addSingleNode.gpr");
    Path generated_rule = Path.of(tempDir.toString(), "addSingleNode.gpr");
    FileTestHelper.testFileEquals(expected_rule, generated_rule);
  }

  @Test
  void generateDeleteNodeRuleTest() throws Exception {
    Path tempDir = Files.createTempDirectory("");

    GrooveRuleBuilder ruleBuilder = new GrooveRuleBuilder();
    ruleBuilder.startRule("deleteSingleNode");
    ruleBuilder.deleteNode("node");
    ruleBuilder.buildRule();
    GrooveRuleWriter.writeRules(tempDir, ruleBuilder.getRules(), true);

    Path expected_rule = getResource("deleteSingleNode.gpr");
    Path generated_rule = Path.of(tempDir.toString(), "deleteSingleNode.gpr");
    FileTestHelper.testFileEquals(expected_rule, generated_rule);
  }

  @Test
  void generateAddEdgeRuleTest() throws Exception {
    Path tempDir = Files.createTempDirectory("");

    GrooveRuleBuilder ruleBuilder = new GrooveRuleBuilder();
    ruleBuilder.startRule("addNodesWithEdge");
    GrooveNode source = ruleBuilder.addNode("source");
    GrooveNode target = ruleBuilder.addNode("target");
    ruleBuilder.addEdge("edge", source, target);
    ruleBuilder.buildRule();
    GrooveRuleWriter.writeRules(tempDir, ruleBuilder.getRules(), true);

    Path expected_rule = getResource("addNodesWithEdge.gpr");
    Path generated_rule = Path.of(tempDir.toString(), "addNodesWithEdge.gpr");
    FileTestHelper.testFileEquals(expected_rule, generated_rule);
  }

  @Test
  void generateContextEdgeRuleTest() throws Exception {
    Path tempDir = Files.createTempDirectory("");

    GrooveRuleBuilder ruleBuilder = new GrooveRuleBuilder();
    ruleBuilder.startRule("contextEdge");
    GrooveNode source = ruleBuilder.contextNode("source");
    GrooveNode target = ruleBuilder.contextNode("target");
    ruleBuilder.contextEdge("edge", source, target);
    ruleBuilder.buildRule();
    GrooveRuleWriter.writeRules(tempDir, ruleBuilder.getRules(), true);

    Path expected_rule = getResource("contextEdge.gpr");
    Path generated_rule = Path.of(tempDir.toString(), "contextEdge.gpr");
    FileTestHelper.testFileEquals(expected_rule, generated_rule);
  }

  @Test
  void generateNodeWithFlagTest() throws Exception {
    Path tempDir = Files.createTempDirectory("");

    GrooveRuleBuilder ruleBuilder = new GrooveRuleBuilder();
    ruleBuilder.startRule("nodeWithFlag");
    GrooveNode node = ruleBuilder.addNode("node");
    node.addFlag("root");
    ruleBuilder.buildRule();
    GrooveRuleWriter.writeRules(tempDir, ruleBuilder.getRules(), true);

    Path expected_rule = getResource("nodeWithFlag.gpr");
    Path generated_rule = Path.of(tempDir.toString(), "nodeWithFlag.gpr");
    FileTestHelper.testFileEquals(expected_rule, generated_rule);
  }

  @Test
  void generateTwoRuleSynchTest() throws Exception {
    Path tempDir = Files.createTempDirectory("");

    GrooveRuleBuilder generator1 = new GrooveRuleBuilder();
    generator1.startRule("addEdge1");
    GrooveNode s1 = generator1.addNode("s1");
    GrooveNode t1 = generator1.addNode("t1");
    generator1.addEdge("edge1", s1, t1);
    GrooveGraphRule r1 = generator1.buildRule();

    generator1.startRule("addEdge2");
    GrooveNode s2 = generator1.addNode("s2");
    GrooveNode t2 = generator1.addNode("t2");
    generator1.addEdge("edge2", s2, t2);
    GrooveGraphRule r2 = generator1.buildRule();

    Map<String, Set<GrooveGraphRule>> nameToToBeSynchedRules = new LinkedHashMap<>();

    Set<GrooveGraphRule> toBeSynched =
        new LinkedHashSet<>(); // Fixed iteration order needed for the testcase.
    toBeSynched.add(r1);
    toBeSynched.add(r2);

    nameToToBeSynchedRules.put("twoRuleSynch", toBeSynched);
    Stream<GrooveGraphRule> synchedRules =
        GrooveRuleBuilder.createSynchedRules(nameToToBeSynchedRules);

    GrooveRuleWriter.writeRules(tempDir, synchedRules, true);

    Path expected_rule = getResource("twoRuleSynch.gpr");
    Path generated_rule = Path.of(tempDir.toString(), "twoRuleSynch.gpr");
    FileTestHelper.testFileEquals(expected_rule, generated_rule);
  }

  @Test
  void generateThreeRuleSynchTest() throws Exception {
    Path tempDir = Files.createTempDirectory("");

    GrooveRuleBuilder generator1 = new GrooveRuleBuilder();
    generator1.startRule("addEdge1");
    GrooveNode s1 = generator1.addNode("s1");
    GrooveNode t1 = generator1.addNode("t1");
    generator1.addEdge("edge1", s1, t1);
    GrooveGraphRule r1 = generator1.buildRule();

    generator1.startRule("addEdge2");
    GrooveNode s2 = generator1.addNode("s2");
    GrooveNode t2 = generator1.addNode("t2");
    generator1.deleteNode("delete");
    generator1.addEdge("edge2", s2, t2);
    GrooveGraphRule r2 = generator1.buildRule();

    generator1.startRule("addEdge3");
    GrooveNode s3 = generator1.contextNode("s3");
    GrooveNode t3 = generator1.contextNode("t3");
    generator1.deleteEdge("edge3", s3, t3);
    GrooveGraphRule r3 = generator1.buildRule();

    Map<String, Set<GrooveGraphRule>> nameToToBeSynchedRules = new LinkedHashMap<>();

    Set<GrooveGraphRule> toBeSynched =
        new LinkedHashSet<>(); // Fixed iteration order needed for the testcase.
    toBeSynched.add(r1);
    toBeSynched.add(r2);
    toBeSynched.add(r3);

    nameToToBeSynchedRules.put("threeRuleSynch", toBeSynched);
    Stream<GrooveGraphRule> synchedRules =
        GrooveRuleBuilder.createSynchedRules(nameToToBeSynchedRules);

    GrooveRuleWriter.writeRules(tempDir, synchedRules, true);

    Path expected_rule = getResource("threeRuleSynch.gpr");
    Path generated_rule = Path.of(tempDir.toString(), "threeRuleSynch.gpr");
    FileTestHelper.testFileEquals(expected_rule, generated_rule);
  }
}
