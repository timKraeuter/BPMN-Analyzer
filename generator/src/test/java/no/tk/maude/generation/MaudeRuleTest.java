package no.tk.maude.generation;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class MaudeRuleTest {

  @Test
  void generateFSMRule() {
    MaudeObject preObject = createFSMObject("X", "\"red\"");
    MaudeObject postObject = createFSMObject("X", "\"green\"");

    MaudeRule fsmRule =
        new MaudeRuleBuilder()
            .startRule("turnGreen")
            .addPreObject(preObject)
            .addPostObject(postObject)
            .buildRule();

    assertThat(
        fsmRule.generateRuleString(),
        is(
            "rl [turnGreen] :  < X : FSM | state : \"red\" > => "
                + "< X : FSM | state : \"green\" > ."));
  }

  @Test
  void generateMultiObjectRule() {
    MaudeObject preObject1 = createFSMObject("X", "\"red\"");
    MaudeObject preObject2 = createFSMObject("Y", "\"red\"");

    MaudeObject postObject1 = createFSMObject("X", "\"green\"");
    MaudeObject postObject2 = createFSMObject("Y", "\"green\"");

    MaudeRule fsmRule =
        new MaudeRuleBuilder()
            .startRule("turnGreen2x")
            .addPreObject(preObject1)
            .addPreObject(preObject2)
            .addPostObject(postObject1)
            .addPostObject(postObject2)
            .buildRule();

    assertThat(
        fsmRule.generateRuleString(),
        is(
            "rl [turnGreen2x] :  < X : FSM | state : \"red\" > < Y : FSM | state : "
                + "\"red\" > => < X : FSM | state : \"green\" > < Y : FSM | state : "
                + "\"green\" > ."));
  }

  private MaudeObject createFSMObject(String oid, String stateAttrValue) {
    return new MaudeObjectBuilder()
        .oid(oid)
        .oidType("FSM")
        .addAttributeValue("state", stateAttrValue)
        .build();
  }

  @Test
  void generateRuleExceptionsTest() {
    MaudeRuleBuilder builder = new MaudeRuleBuilder();
    assertThrows(MaudeGenerationException.class, builder::buildRule);
    builder.startRule("ruleName");
    assertThrows(MaudeGenerationException.class, builder::buildRule);
    MaudeObject maudeObject = createFSMObject("X", "\"red\"");

    builder.addPreObject(maudeObject);
    // No post object
    assertThrows(MaudeGenerationException.class, builder::buildRule);
    builder.addPostObject(maudeObject);
    // No exception when name and one pre object and post object are present.
    assertNotNull(builder.buildRule());
  }

  @Test
  void generateRuleVarsTest() {
    final String fsms = "fsms";
    final String otherVarGroup = "otherVarGroup";
    final MaudeRuleBuilder ruleBuilder =
        new MaudeRuleBuilder()
            .startRule("turnGreen2x")
            .addVar(fsms, "FSM", "X")
            .addVar(fsms, "FSM", "Y")
            .addVar(otherVarGroup, "Type", "Z");

    assertThat(
        ruleBuilder.getVars(),
        is("vars X Y : FSM . --- fsms\r\n    vars Z : Type . --- otherVarGroup"));
  }
}
