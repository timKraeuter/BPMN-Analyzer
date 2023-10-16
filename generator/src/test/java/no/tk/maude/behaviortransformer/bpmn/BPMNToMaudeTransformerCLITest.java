package no.tk.maude.behaviortransformer.bpmn;

import static no.tk.groove.behaviortransformer.bpmn.BPMNToGrooveTestBase.BPMN_MODELS_SEMANTICS_TEST_FOLDER;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.IOException;
import java.nio.file.Path;
import no.tk.util.FileTestHelper;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;

class BPMNToMaudeTransformerCLITest implements BPMNMaudeTestHelper {

  @Test
  void testSimple() throws IOException {
    String modelName = "cyclic";
    String resourcePath = BPMN_MODELS_SEMANTICS_TEST_FOLDER + modelName + DOT_BPMN;
    Path pathToBPMNModel = FileTestHelper.getResource(resourcePath);

    Path tempDirectoryPath = Path.of(FileUtils.getTempDirectoryPath());

    String[] args = {pathToBPMNModel.toString(), tempDirectoryPath.toString()};

    BPMNToMaudeTransformerCLI.main(args);

    String actualMaudeModule =
        readMaudeFileAndSanitize(Path.of(tempDirectoryPath.toString(), modelName + DOT_MAUDE));
    String expectedMaudeModule = readExpectedMaudeModule(MAUDE_MODULE_FOLDER, modelName);
    assertThat(actualMaudeModule, is(expectedMaudeModule));
  }
}
