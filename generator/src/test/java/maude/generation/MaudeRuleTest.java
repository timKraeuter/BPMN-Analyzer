package maude.generation;

import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MaudeRuleTest {

    @Test
    void generateFSMRule() {
        MaudeObject preObject = createFSMObject("X", "\"red\"");
        MaudeObject postObject = createFSMObject("X", "\"green\"");

        MaudeRule fsmRule = new MaudeRuleBuilder()
                .ruleName("turnGreen")
                .addPreObject(preObject)
                .addPostObject(postObject)
                .build();

        assertThat(fsmRule.generateRule(), is("rl [turnGreen] :  < X : FSM | state : \"red\" > => " +
                                              "< X : FSM | state : \"green\" > ."));

    }

    @Test
    void generateMultiObjectRule() {
        MaudeObject preObject1 = createFSMObject("X", "\"red\"");
        MaudeObject preObject2 = createFSMObject("Y", "\"red\"");

        MaudeObject postObject1 = createFSMObject("X", "\"green\"");
        MaudeObject postObject2 = createFSMObject("Y", "\"green\"");

        MaudeRule fsmRule = new MaudeRuleBuilder()
                .ruleName("turnGreen2x")
                .addPreObject(preObject1)
                .addPreObject(preObject2)
                .addPostObject(postObject1)
                .addPostObject(postObject2)
                .build();

        assertThat(fsmRule.generateRule(), is("rl [turnGreen2x] :  < X : FSM | state : \"red\" > < Y : FSM | state : " +
                                              "\"red\" > => < X : FSM | state : \"green\" > < Y : FSM | state : " +
                                              "\"green\" > ."));
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
        assertThrows(MaudeGenerationException.class, builder::build);
        builder.ruleName("ruleName");
        assertThrows(MaudeGenerationException.class, builder::build);
        MaudeObject preObject = createFSMObject("X", "\"red\"");

        builder.addPreObject(preObject);
        // No exception when name and one pre object are present.
        assertNotNull(builder.build());
    }
}