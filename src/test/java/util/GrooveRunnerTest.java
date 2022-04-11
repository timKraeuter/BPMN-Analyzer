package util;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

class GrooveRunnerTest {
    private final String grooveBinDir = new File(this.getClass().getResource("/groove/bin").getFile()).getPath();

    @Test
    void testGenerateStateSpace() throws IOException {
        GrooveRunner grooveRunner = new GrooveRunner(grooveBinDir);
        File stateSpace = grooveRunner.generateStateSpace(grooveBinDir + "\\bpmn\\call-activity-complex.gps",
                                                          grooveBinDir + "\\statespaces\\statespace.txt",
                                                          false);

        // Check state space files
        File expected = new File(this.getClass().getResource("/statespace.txt").getFile());
        System.out.println("Expected " + expected);
        System.out.println("State space " + stateSpace);
        FileTestHelper.testFileEquals(expected, stateSpace);
    }
}