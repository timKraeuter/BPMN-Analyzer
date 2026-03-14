package no.tk.groove.runner;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Path;
import no.tk.groove.runner.checking.ModelCheckingResult;
import no.tk.groove.runner.checking.TemporalLogic;
import no.tk.util.FileTestHelper;
import org.junit.jupiter.api.Test;

class GrooveJarRunnerTest {

  /**
   * If this tests does not terminate, one possible reasons is that the gradle JVM/JDK does not
   * match the Java JDK.
   */
  @Test
  void testGenerateStateSpace() throws Exception {
    GrooveJarRunner grooveJarRunner = new GrooveJarRunner(false);
    String tempOutputFile =
        System.getProperty("java.io.tmpdir") + "/grooveJarRunner/statespace.txt";
    Path stateSpace =
        grooveJarRunner.generateStateSpace(getCyclicNoLayoutGraphGrammar(), tempOutputFile, true);

    // Check state space files
    Path expected = FileTestHelper.getResource("grooveJarRunner/statespace.txt");
    FileTestHelper.testFileEquals(expected, stateSpace);
  }

  private String getCyclicNoLayoutGraphGrammar() {
    return FileTestHelper.getResource("bpmn/groove/cyclicNoLayout.gps").toString();
  }

  /**
   * If this tests does not terminate, one possible reasons is that the gradle JVM/JDK does not
   * match the Java JDK.
   */
  @Test
  void testCTLModelChecking() throws IOException, InterruptedException {
    GrooveJarRunner grooveJarRunner = new GrooveJarRunner(false);
    String trueCTLProperty = "AG(true)";
    String wrongCTLProperty = "AG(false)";

    ModelCheckingResult result1 =
        grooveJarRunner.checkCTL(getCyclicNoLayoutGraphGrammar(), trueCTLProperty);

    assertThat(result1.usedLogic(), is(TemporalLogic.CTL));
    assertThat(result1.property(), is(trueCTLProperty));
    assertTrue(result1.valid());
    assertFalse(result1.hasError());

    ModelCheckingResult result2 =
        grooveJarRunner.checkCTL(getCyclicNoLayoutGraphGrammar(), wrongCTLProperty);

    assertThat(result2.usedLogic(), is(TemporalLogic.CTL));
    assertThat(result2.property(), is(wrongCTLProperty));
    assertFalse(result2.valid());
    assertFalse(result2.hasError());
  }

  /**
   * If this tests does not terminate, one possible reasons is that the gradle JVM/JDK does not
   * match the Java JDK.
   */
  @Test
  void testCTLModelCheckingError() throws IOException, InterruptedException {
    GrooveJarRunner grooveJarRunner = new GrooveJarRunner(false);
    String property = "G(!false)";

    ModelCheckingResult result =
        grooveJarRunner.checkCTL(getCyclicNoLayoutGraphGrammar(), property);

    assertThat(result.usedLogic(), is(TemporalLogic.CTL));
    assertThat(result.property(), is(property));
    assertTrue(result.hasError());
    assertFalse(result.valid());
    assertThat(
        result.error(),
        is(
            "Error: Error while parsing 'G(!false)': Temporal operator 'G' should be nested inside path quantifier in CTL formula"));
  }
}
