package no.tk.groove.behaviortransformer.bpmn;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.file.Path;
import no.tk.behavior.bpmn.auxiliary.exceptions.GrooveGenerationRuntimeException;
import no.tk.util.FileTestHelper;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;

class BPMNTransformerDriverTest extends BPMNToGrooveTestBase {

  @Test
  void onlyOneArgTest() {
    String[] args = {"123"};
    assertThrows(GrooveGenerationRuntimeException.class, () -> BPMNTransformerDriver.main(args));
  }

  @Test
  void noFileExistsTest() {
    String[] args = {"notAFile", "./"};
    assertThrows(GrooveGenerationRuntimeException.class, () -> BPMNTransformerDriver.main(args));
  }

  @Test
  void mainTest() throws Exception {
    String bpmnFileName = "cyclic.bpmn";
    String resourcePath = BPMN_BPMN_MODELS_SEMANTICS_TEST_FOLDER + bpmnFileName;
    Path pathToBPMNModel = FileTestHelper.getResource(resourcePath);

    Path tempDirectoryPath = Path.of(FileUtils.getTempDirectoryPath(), "bpmn");

    String[] args = {pathToBPMNModel.toString(), tempDirectoryPath.toString(), "true"};

    BPMNTransformerDriver.main(args);

    checkGenerationEqualToExpected(
        fixedRules::contains, "cyclic", Path.of(tempDirectoryPath.toString(), "cyclic.gps"));
  }

  @Test
  void mainTestNoLayout() throws Exception {
    String bpmnFileName = "cyclicNoLayout.bpmn";
    String resourcePath = BPMN_BPMN_MODELS_SEMANTICS_TEST_FOLDER + bpmnFileName;
    Path pathToBPMNModel = FileTestHelper.getResource(resourcePath);

    Path tempDirectoryPath = Path.of(FileUtils.getTempDirectoryPath(), "bpmn");

    String[] args = {pathToBPMNModel.toString(), tempDirectoryPath.toString()};

    BPMNTransformerDriver.main(args);

    checkGenerationEqualToExpected(
        fixedRules::contains,
        "cyclicNoLayout",
        Path.of(tempDirectoryPath.toString(), "cyclicNoLayout.gps"));
  }
}
