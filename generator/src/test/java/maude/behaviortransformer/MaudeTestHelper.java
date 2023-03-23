package maude.behaviortransformer;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import util.FileTestHelper;

public interface MaudeTestHelper {

  default String readExpectedMaudeModule(String folder, String resourceFileName)
      throws IOException {
    Path maudeModel = getMaudeModuleFile(folder, resourceFileName);
    return Files.readString(maudeModel).replaceAll("\r?\n", "\r\n");
    // force identical line separators;
  }

  private Path getMaudeModuleFile(String folder, String resourceFileName) {
    String resourcePath = folder + resourceFileName + ".maude";
    return FileTestHelper.getResource(resourcePath);
  }

  default void replaceWithActualIfNeeded(
      String folder, String resourceFileName, String actualMaudeModule, String expectedMaudeModule)
      throws IOException {
    String expectedFileFilePath = "src/test/resources/" + folder + resourceFileName + ".maude";
    if (!actualMaudeModule.equals(expectedMaudeModule)) {
      // Only replace if reduced to true. Run model!
      Files.writeString(Path.of(expectedFileFilePath), actualMaudeModule, Charset.defaultCharset());
      System.out.println("Replaced module with actual!");
    }
  }
}
