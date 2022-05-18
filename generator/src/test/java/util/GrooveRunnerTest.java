package util;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

class GrooveRunnerTest {
    private final String grooveBinDir = new File(this.getClass().getResource("/groove/bin").getFile()).getPath();

    /**
     * If this tests does not terminate, one possible reasons is that the gradle JVM/JDK does not match the Java JDK.
     */
    @Test
    void testGenerateStateSpace() throws IOException, InterruptedException {
        GrooveRunner grooveRunner = new GrooveRunner(grooveBinDir);
        File stateSpace =
                grooveRunner.generateStateSpace(grooveBinDir + "/bpmn/call-activity-complex.gps",
                                                grooveBinDir + "/statespaces/statespace.txt",
                                                true);

        // Check state space files
        File expected = new File(this.getClass().getResource("/statespace.txt").getFile());
        FileTestHelper.testFileEquals(expected, stateSpace);
    }
}