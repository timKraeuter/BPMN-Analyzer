package groove.behaviortransformer;

import behavior.Behavior;
import groove.graph.GrooveNode;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Function;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import util.FileTestHelper;

public abstract class BehaviorToGrooveTransformerTestHelper {

  //  private final String outputPath = "C:/Source/groove/bin";
  private final String outputPath = FileUtils.getTempDirectoryPath();

  private Function<String, Boolean> fileNameFilter = x -> false;

  @BeforeEach
  void setUp() {
    GrooveNode.idCounter.set(-1);
    this.fileNameFilter = x -> false;
    this.setUpFurther();
  }

  protected abstract void setUpFurther();

  public abstract String getTestResourcePathSubFolderName();

  public abstract String getOutputPathSubFolderName();

  public String getOutputPathIncludingSubFolder() {
    return this.outputPath + File.separator + this.getOutputPathSubFolderName();
  }

  public void setFileNameFilter(Function<String, Boolean> fileNameFilter) {
    this.fileNameFilter = fileNameFilter;
  }

  public void checkGrooveGeneration(Behavior behavior) throws IOException {
    this.checkGrooveGeneration(behavior, this.fileNameFilter, false);
  }

  public void checkGrooveGenerationWithIDs(Behavior behavior) throws IOException {
    this.checkGrooveGeneration(behavior, this.fileNameFilter, true);
  }

  @SuppressWarnings("ConstantConditions")
  private void checkGrooveGeneration(
      Behavior behavior, Function<String, Boolean> fileNameFilter, boolean useIDs)
      throws IOException {
    String modelName = behavior.getName();
    BehaviorToGrooveTransformer transformer = new BehaviorToGrooveTransformer();
    Path outputDir = Path.of(this.getOutputPathIncludingSubFolder());
    Path grammarDir = transformer.generateGrooveGrammar(behavior, outputDir, useIDs);

    // assert
    checkGenerationEqualToExpected(fileNameFilter, modelName, grammarDir);

    Path propertiesFile = Path.of(outputDir.toString(), modelName + ".gps/system.properties");
    this.checkPropertiesFile(propertiesFile);
  }

  protected void checkGenerationEqualToExpected(
      Function<String, Boolean> fileNameFilter, String modelName, Path grammarDir)
      throws IOException {
    Path expectedDir =
        FileTestHelper.getResource(getTestResourcePathSubFolderName() + "/" + modelName + ".gps");
    FileTestHelper.testDirEquals(
        expectedDir,
        grammarDir,
        // Ignore the system.propertie file because it contains a timestamp and a dir.
        fileName -> fileName.equals("system.properties") || fileNameFilter.apply(fileName));
  }

  void checkPropertiesFile(Path propertiesFile) throws IOException {
    Assertions.assertTrue(
        Files.readString(propertiesFile)
            .replaceAll("\r?\n", "\r\n")
            // force identical line separators
            .endsWith("grooveVersion=5.8.1\r\n" + "grammarVersion=3.7"));
  }
}
