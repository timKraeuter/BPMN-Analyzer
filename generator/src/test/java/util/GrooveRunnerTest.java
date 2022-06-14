package util;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

class GrooveRunnerTest {

    /**
     * If this tests does not terminate, one possible reasons is that the gradle JVM/JDK does not match the Java JDK.
     */
    @Test
    void testGenerateStateSpace() throws IOException, InterruptedException {
        GrooveRunner grooveRunner = new GrooveRunner();
        File stateSpace =
                grooveRunner.generateStateSpace("../groove/bin/circular.gps",
                                                "../groove/bin/statespaces/statespace.txt",
                                                true);

        // Check state space files
        File expected = new File(this.getClass().getResource("/statespace.txt").getFile());
        FileTestHelper.testFileEquals(expected, stateSpace);
    }
}