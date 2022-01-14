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

public abstract class BehaviorToGrooveTransformerTestHelper {
    private final String outputPath = "C:/Source/groove/bin";
//    String outputPath = "B:/Source/groove/bin";
//    String outputPath = FileUtils.getTempDirectoryPath();

    private boolean addPrefix = false;
    private Function<String, Boolean> fileNameFilter = x -> false;

    @BeforeEach
    void setUp() {
        GrooveNode.idCounter.set(-1);
        this.addPrefix = false;
        this.fileNameFilter = x -> false;
        this.setUpFurther();
    }

    protected abstract void setUpFurther();

    public abstract String getOutputPathSubFolderName();

    public String getOutputPathIncludingSubFolder() {
        return this.outputPath + "/" + this.getOutputPathSubFolderName();
    }

    public void setAddPrefix(boolean addPrefix) {
        this.addPrefix = addPrefix;
    }

    public void setFileNameFilter(Function<String, Boolean> fileNameFilter) {
        this.fileNameFilter = fileNameFilter;
    }

    public void checkGrooveGeneration(Behavior behavior) throws IOException {
        this.checkGrooveGeneration(behavior, this.addPrefix, this.fileNameFilter);
    }

    @SuppressWarnings("ConstantConditions")
    private void checkGrooveGeneration(Behavior behavior, boolean addPrefix, Function<String, Boolean> fileNameFilter) throws IOException {
        String modelName = behavior.getName();
        BehaviorToGrooveTransformer transformer = new BehaviorToGrooveTransformer();
        File outputDir = new File(this.getOutputPathIncludingSubFolder());
        transformer.generateGrooveGrammar(behavior, outputDir, addPrefix);

        // assert
        File expectedDir = new File(this.getClass().getResource("/" + this.getOutputPathSubFolderName() + "/" + modelName + ".gps").getFile());
        FileTestHelper.testDirEquals(
                expectedDir,
                new File(outputDir + "/" + modelName + ".gps"),
                fileName -> fileName.equals("system.properties") || fileNameFilter.apply(fileName)); // Ignore the system.properties file because it contains a timestamp and a dir.

        File propertiesFile = new File(outputDir + "/" + modelName + ".gps/system.properties");
        this.checkPropertiesFile(propertiesFile);
    }

    void checkPropertiesFile(File propertiesFile) throws IOException {
        Assertions.assertTrue(FileUtils.readFileToString(propertiesFile, StandardCharsets.UTF_8).replaceAll("\r?\n", "\r\n") // force identical line separators
                                       .endsWith("grooveVersion=5.8.1\r\n" + "grammarVersion=3.7"));
    }
}