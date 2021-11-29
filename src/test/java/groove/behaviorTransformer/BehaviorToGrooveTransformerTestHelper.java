package groove.behaviorTransformer;

import behavior.Behavior;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Assertions;
import util.FileTestHelper;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

interface BehaviorToGrooveTransformerTestHelper {
    //    private static final String outputPath = "C:/Source/groove/bin";
    //    private static final String outputPath = "B:/Source/groove/bin";
    String outputPath = FileUtils.getTempDirectoryPath();

    @SuppressWarnings("ConstantConditions")
    default void checkGrooveGeneration(String modelName, Behavior behavior) throws IOException {
        BehaviorToGrooveTransformer transformer = new BehaviorToGrooveTransformer();
        File outputDir = new File(outputPath);
        transformer.generateGrooveGrammar(behavior, outputDir);

        // assert
        File expectedDir = new File(this.getClass().getResource("/" + modelName + ".gps").getFile());
        FileTestHelper.testDirEquals(
                expectedDir,
                new File(outputDir + "/" + modelName + ".gps"),
                fileName -> fileName.equals("system.properties")); // Ignore the system.properties file because it contains a timestamp and a dir.

        File propertiesFile = new File(this.getClass().getResource("/" + modelName + ".gps/system.properties").getFile());
        this.checkPropertiesFile(propertiesFile);
    }

    default void checkPropertiesFile(File propertiesFile) throws IOException {
        Assertions.assertTrue(
                FileUtils.readFileToString(propertiesFile, StandardCharsets.UTF_8)
                         .replaceAll("\r?\n", "\r\n") // force identical line separators
                         .endsWith(
                                 "startGraph=start\r\n" +
                                         "grooveVersion=5.8.1\r\n" +
                                         "grammarVersion=3.7"));
    }
}