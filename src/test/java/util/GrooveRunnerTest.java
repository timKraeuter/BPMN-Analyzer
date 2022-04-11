package util;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

class GrooveRunnerTest {
    private final String grooveBinDir = new File(this.getClass().getResource("/groove/bin").getFile()).getPath();

    @Test
    void testGenerateStateSpace() throws IOException, InterruptedException {
        GrooveRunner grooveRunner = new GrooveRunner(grooveBinDir);
        try {
            File stateSpace =
                    grooveRunner.generateStateSpace(grooveBinDir + File.separator + "bpmn" + File.separator + "call-activity-complex.gps",
                                                    FileUtils.getTempDirectoryPath() + File.separator + "statespaces" + File.separator +
                                                            "statespace.txt",
                                                    true);

            // Check state space files
            File expected = new File(this.getClass().getResource("/statespace.txt").getFile());
            System.out.println("Expected: " + expected);
            System.out.println("Actual: " + stateSpace);
            FileTestHelper.testFileEquals(expected, stateSpace);
        }
        catch (RuntimeException e) {
            e.printStackTrace();
            throw e;
        }
    }
}