package util;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import groove.runner.GrooveJarRunner;
import groove.runner.checking.ModelCheckingResult;
import groove.runner.checking.TemporalLogic;
import java.io.IOException;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class GrooveJarRunnerTest {

  /**
   * If this tests does not terminate, one possible reasons is that the gradle JVM/JDK does not
   * match the Java JDK.
   */
  @Test
  void testGenerateStateSpace() throws Exception {
    GrooveJarRunner grooveJarRunner = new GrooveJarRunner();
    String tempOutputFile =
        System.getProperty("java.io.tmpdir") + "/grooveJarRunner/statespace.txt";
    Path stateSpace =
        grooveJarRunner.generateStateSpace(getCircularGraphGrammar(), tempOutputFile, true);

    // Check state space files
    Path expected = FileTestHelper.getResource("grooveJarRunner/statespace.txt");
    FileTestHelper.testFileEquals(expected, stateSpace);
  }

  private String getCircularGraphGrammar() {
    return FileTestHelper.getResource("grooveJarRunner/circular.gps").toString();
  }

  /**
   * If this tests does not terminate, one possible reasons is that the gradle JVM/JDK does not
   * match the Java JDK.
   */
  @Test
  void testCTLModelChecking() throws IOException, InterruptedException {
    GrooveJarRunner grooveJarRunner = new GrooveJarRunner();
    String trueCTLProperty = "AG(true)";
    String wrongCTLProperty = "AG(false)";

    ModelCheckingResult result1 =
        grooveJarRunner.checkCTL(getCircularGraphGrammar(), trueCTLProperty);

    assertThat(result1.getUsedLogic(), is(TemporalLogic.CTL));
    assertThat(result1.getProperty(), is(trueCTLProperty));
    assertTrue(result1.isValid());
    assertFalse(result1.hasError());

    ModelCheckingResult result2 =
        grooveJarRunner.checkCTL(getCircularGraphGrammar(), wrongCTLProperty);

    assertThat(result2.getUsedLogic(), is(TemporalLogic.CTL));
    assertThat(result2.getProperty(), is(wrongCTLProperty));
    assertFalse(result2.isValid());
    assertFalse(result2.hasError());
  }

  /**
   * If this tests does not terminate, one possible reasons is that the gradle JVM/JDK does not
   * match the Java JDK.
   */
  @Test
  void testCTLModelCheckingError() throws IOException, InterruptedException {
    GrooveJarRunner grooveJarRunner = new GrooveJarRunner();
    String property = "G(!false)";

    ModelCheckingResult result = grooveJarRunner.checkCTL(getCircularGraphGrammar(), property);

    assertThat(result.getUsedLogic(), is(TemporalLogic.CTL));
    assertThat(result.getProperty(), is(property));
    assertTrue(result.hasError());
    assertFalse(result.isValid());
    assertThat(
        result.getError(),
        is(
            "Error: nl.utwente.groove.util.parse.FormatException: Temporal operator 'G' should be nested inside path quantifier in CTL formula"));
  }
}
