package util;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import groove.runner.GrooveJarRunner;
import groove.runner.checking.ModelCheckingResult;
import groove.runner.checking.TemporalLogic;
import java.io.File;
import java.io.IOException;
import org.junit.jupiter.api.Test;

class GrooveJarRunnerTest {

  /**
   * If this tests does not terminate, one possible reasons is that the gradle JVM/JDK does not
   * match the Java JDK.
   */
  @Test
  void testGenerateStateSpace() throws IOException, InterruptedException {
    GrooveJarRunner grooveJarRunner = new GrooveJarRunner();
    String tempOutputFile =
        System.getProperty("java.io.tmpdir") + "/grooveJarRunner/statespace.txt";
    File stateSpace =
        grooveJarRunner.generateStateSpace(getCircularGraphGrammar(), tempOutputFile, true);

    // Check state space files
    File expected =
        new File(this.getClass().getResource("/grooveJarRunner/statespace.txt").getFile());
    FileTestHelper.testFileEquals(expected, stateSpace);
  }

  private String getCircularGraphGrammar() {
    return new File(this.getClass().getResource("/grooveJarRunner/circular.gps").getFile())
        .getAbsolutePath();
  }

  /**
   * If this tests does not terminate, one possible reasons is that the gradle JVM/JDK does not
   * match the Java JDK.
   */
  @Test
  void testCTLModelChecking() throws IOException, InterruptedException {
    GrooveJarRunner grooveJarRunner = new GrooveJarRunner();
    String trueCTLProperty = "AG(!false)";
    String wrongCTLProperty = "AG(!true)";

    ModelCheckingResult result1 =
        grooveJarRunner.checkCTL(getCircularGraphGrammar(), trueCTLProperty);

    assertThat(result1.getUsedLogic(), is(TemporalLogic.CTL));
    assertThat(result1.getProperty(), is(trueCTLProperty));
    assertTrue(result1.isValid());

    ModelCheckingResult result2 =
        grooveJarRunner.checkCTL(getCircularGraphGrammar(), wrongCTLProperty);

    assertThat(result2.getUsedLogic(), is(TemporalLogic.CTL));
    assertThat(result2.getProperty(), is(wrongCTLProperty));
    assertFalse(result2.isValid());
  }
}
