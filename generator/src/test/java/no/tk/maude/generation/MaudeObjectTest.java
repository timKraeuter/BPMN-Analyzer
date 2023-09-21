package no.tk.maude.generation;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class MaudeObjectTest {

  @Test
  void generateFSMObjectTest() {
    MaudeObject maudeObject =
        new MaudeObjectBuilder()
            .oid("X")
            .oidType("FSM")
            .addAttributeValue("state", "\"red\"")
            .build();

    assertThat(maudeObject.generateObjectString(), is("< X : FSM | state : \"red\" >"));
  }

  @Test
  void generateBPMNObjectTest() {
    MaudeObject maudeObject =
        new MaudeObjectBuilder()
            .oid("sequential-activities")
            .oidType("ProcessSnapshot")
            .addAttributeValue("tokens", "(\"sequential-activities_start\")")
            .addAttributeValue("subprocesses", "none")
            .addAttributeValue("state", "Running")
            .build();

    assertThat(
        maudeObject.generateObjectString(),
        is(
            "< sequential-activities : ProcessSnapshot | tokens : "
                + "(\"sequential-activities_start\"), subprocesses : none, state : "
                + "Running >"));
  }

  @Test
  void generateObjectExceptionsTest() {
    MaudeObjectBuilder builder1 = new MaudeObjectBuilder();
    assertThrows(MaudeGenerationException.class, builder1::build);
    builder1.oid("X");
    assertThrows(MaudeGenerationException.class, builder1::build);
    builder1.oidType("FSM");

    // No exception when oid and oidType are specified.
    assertNotNull(builder1.build());

    MaudeObjectBuilder builder2 = new MaudeObjectBuilder().oidType("FSM");
    assertThrows(MaudeGenerationException.class, builder2::build);
  }
}
