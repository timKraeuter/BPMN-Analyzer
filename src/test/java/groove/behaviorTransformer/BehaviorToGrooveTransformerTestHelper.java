package groove.behaviorTransformer;

import behavior.Behavior;
import groove.graph.GrooveNode;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import util.FileTestHelper;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.function.Function;

interface BehaviorToGrooveTransformerTestHelper {
    //    String outputPath = "C:/Source/groove/bin";
    //    String outputPath = "B:/Source/groove/bin";
    String outputPath = FileUtils.getTempDirectoryPath();

    @BeforeEach
    default void setUp() {
        GrooveNode.idCounter.set(-1);
    }

    default void checkGrooveGeneration(Behavior behavior) throws IOException {
        this.checkGrooveGeneration(behavior, false);
    }

    default void checkGrooveGeneration(Behavior behavior, boolean addPrefix) throws IOException {
        this.checkGrooveGeneration(behavior, addPrefix, x -> false);
    }

    @SuppressWarnings("ConstantConditions")
    default void checkGrooveGeneration(
            Behavior behavior,
            boolean addPrefix,
            Function<String, Boolean> fileNameFilter) throws IOException {
        String modelName = behavior.getName();
        BehaviorToGrooveTransformer transformer = new BehaviorToGrooveTransformer();
        File outputDir = new File(outputPath);
        transformer.generateGrooveGrammar(behavior, outputDir, addPrefix);

        // assert
        File expectedDir = new File(this.getClass().getResource("/" + modelName + ".gps").getFile());
        FileTestHelper.testDirEquals(
                expectedDir,
                new File(outputDir + "/" + modelName + ".gps"),
                fileName -> fileName.equals("system.properties") || fileNameFilter.apply(fileName)); // Ignore the system.properties file because it contains a timestamp and a dir.

        File propertiesFile = new File(outputDir + "/" + modelName + ".gps/system.properties");
        this.checkPropertiesFile(propertiesFile);
    }

    default void checkPropertiesFile(File propertiesFile) throws IOException {
        Assertions.assertTrue(
                FileUtils.readFileToString(propertiesFile, StandardCharsets.UTF_8)
                        .replaceAll("\r?\n", "\r\n") // force identical line separators
                        .endsWith(
                                "grooveVersion=5.8.1\r\n" +
                                        "grammarVersion=3.7"));
    }
}