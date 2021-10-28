package groove;

import behavior.fsm.FiniteStateMachine;
import behavior.fsm.State;
import behavior.fsm.Transition;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

class BehaviorToGrooveTransformerTest {
    //    private static final String outputPath = "C:/Source/groove/bin";
    private static final String outputPath = FileUtils.getTempDirectoryPath();

    @Test
    void testFSMGeneration() throws IOException {
        State start = new State("start");
        FiniteStateMachine fsm = new FiniteStateMachine("abc", start);
        State s1 = new State("s1");
        State s2 = new State("s2");
        State s3 = new State("s3");
        fsm.addTransition(new Transition("a", start, s1));
        fsm.addTransition(new Transition("b", s1, s2));
        fsm.addTransition(new Transition("c", s2, s3));

        BehaviorToGrooveTransformer transformer = new BehaviorToGrooveTransformer();
        File outputDir = new File(outputPath);
        transformer.generateGrooveGrammar(fsm, outputDir);

        // assert
        File expectedDir = new File(this.getClass().getResource("/abc.gps").getFile());
        FileTestHelper.testDirEquals(
                expectedDir,
                new File(outputDir + "/abc.gps"),
                fileName -> fileName.equals("system.properties")); // Ignore the system.properties file because it contains a timestamp and a dir.

        File propertiesFile = new File(this.getClass().getResource("/abc.gps/system.properties").getFile());
        Assertions.assertTrue(
                FileUtils.readFileToString(propertiesFile, StandardCharsets.UTF_8).endsWith(
                        "startGraph=start\n" +
                                "grooveVersion=5.8.1\n" +
                                "grammarVersion=3.7"));
    }
}