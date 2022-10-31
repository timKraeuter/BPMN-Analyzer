package maude.behaviortransformer;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import org.apache.commons.io.FileUtils;

public interface MaudeTestHelper {

  default String readExpectedMaudeModule(String folder, String resourceFileName)
      throws IOException {
    File maudeModel = getMaudeModuleFile(folder, resourceFileName);
    return FileUtils.readFileToString(maudeModel, StandardCharsets.UTF_8)
        .replaceAll("\r?\n", "\r\n");
    // force identical line separators;
  }

  private File getMaudeModuleFile(String folder, String resourceFileName) {
    String resourcePath = folder + resourceFileName + ".maude";

    @SuppressWarnings("ConstantConditions")
    File maudeModel = new File(this.getClass().getResource(resourcePath).getFile());
    return maudeModel;
  }

  default void replaceWithActualIfNeeded(
      String folder, String resourceFileName, String actualMaudeModule, String expectedMaudeModule)
      throws IOException {
    String expectedFileFilePath = "src/test/resources/" + folder + resourceFileName + ".maude";
    if (!actualMaudeModule.equals(expectedMaudeModule)) {
      // Only replace if reduced to true. Run model!
      FileUtils.writeStringToFile(
          new File(expectedFileFilePath), actualMaudeModule, Charset.defaultCharset());
      System.out.println("Replaced module with actual!");
    }
  }
}
