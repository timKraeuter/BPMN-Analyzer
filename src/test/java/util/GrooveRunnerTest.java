package util;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

class GrooveRunnerTest {
    private final String grooveBinDir = new File(this.getClass().getResource("/groove/bin").getFile()).getPath();

    @Test
    void testGenerateStateSpace() throws IOException, InterruptedException {
        GrooveRunner grooveRunner = new GrooveRunner(grooveBinDir);
        try {
            File stateSpace = grooveRunner.generateStateSpace(grooveBinDir + "\\bpmn\\call-activity-complex.gps",
                                                              grooveBinDir + "\\statespaces\\statespace.txt",
                                                              true);

            // Check state space files
            File expected = new File(this.getClass().getResource("/statespace.txt").getFile());
            FileTestHelper.testFileEquals(expected, stateSpace);
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
            throw e;
        }
    }
}