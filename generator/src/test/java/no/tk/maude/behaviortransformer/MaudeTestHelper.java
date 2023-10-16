package no.tk.maude.behaviortransformer;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import no.tk.behavior.fsm.FSMStateAtomicProposition;
import no.tk.behavior.fsm.FiniteStateMachine;
import no.tk.util.FileTestHelper;

public interface MaudeTestHelper {
  String MAUDE_FSM_MODULE_FOLDER = "fsm/maude/";
  String DOT_MAUDE = ".maude";

  default void testFSMMaudeGeneration(
      FiniteStateMachine fsm, Set<FSMStateAtomicProposition> props, String finalQuery)
      throws IOException {
    String actualMaudeModule = new FSMToMaudeTransformer(fsm, props).generate(finalQuery);

    String expectedMaudeModule = readExpectedMaudeModule(MAUDE_FSM_MODULE_FOLDER, fsm.getName());
    if (!actualMaudeModule.equals(expectedMaudeModule)) {
      System.out.println(actualMaudeModule);
    }
    assertThat(actualMaudeModule, is(expectedMaudeModule));
  }

  default String readExpectedMaudeModule(String folder, String resourceFileName)
      throws IOException {
    Path maudeModel = getMaudeModuleFile(folder, resourceFileName);
    return readMaudeFileAndSanitize(maudeModel);
    // force identical line separators;
  }

  default String readMaudeFileAndSanitize(Path maudeModel) throws IOException {
    return Files.readString(maudeModel).replaceAll("\r?\n", "\r\n");
  }

  private Path getMaudeModuleFile(String folder, String resourceFileName) {
    String resourcePath = folder + resourceFileName + DOT_MAUDE;
    return FileTestHelper.getResource(resourcePath);
  }

  default void replaceWithActualIfNeeded(
      String folder, String resourceFileName, String actualMaudeModule, String expectedMaudeModule)
      throws IOException {
    String expectedFilePath = "src/test/resources/" + folder + resourceFileName + DOT_MAUDE;
    if (!actualMaudeModule.equals(expectedMaudeModule)) {
      // Only replace if reduced to true. Run model in the future!
      Files.writeString(Path.of(expectedFilePath), actualMaudeModule, Charset.defaultCharset());
      System.out.println("Replaced module with actual!");
    }
  }
}
