package util;

import groove.runner.GrooveJarRunner;
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
    File stateSpace =
        grooveJarRunner.generateStateSpace(
            "../groove/bin/circular.gps", "../groove/bin/statespaces/statespace.txt", true);

    // Check state space files
    File expected = new File(this.getClass().getResource("/statespace.txt").getFile());
    FileTestHelper.testFileEquals(expected, stateSpace);
  }

  /**
   * If this tests does not terminate, one possible reasons is that the gradle JVM/JDK does not
   * match the Java JDK.
   */
  @Test
  void testModelChecking() throws IOException, InterruptedException {
    GrooveJarRunner grooveJarRunner = new GrooveJarRunner();
    grooveJarRunner.checkCTL("../groove/bin/circular.gps", "AG(!false)", true);
  }
}
