package groove.behaviortransformer.bpmn;

import static org.junit.jupiter.api.Assertions.assertThrows;

import behavior.bpmn.auxiliary.exceptions.GrooveGenerationRuntimeException;
import java.io.File;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;

class BPMNTransformerDriverTest extends BPMNToGrooveTestBase {

  @Test
  void onlyOneArgTest() {
    String[] args = {"123"};
    assertThrows(GrooveGenerationRuntimeException.class, () -> BPMNTransformerDriver.main(args));
  }

  @Test
  void mainTest() {
    String bpmnFileName = "cyclic.bpmn";
    String resourcePath = BPMN_BPMN_MODELS_SEMANTICS_TEST_FOLDER + bpmnFileName;
    File model = new File(this.getClass().getResource(resourcePath).getFile());

    String tempDirectoryPath = FileUtils.getTempDirectoryPath();

    String[] args = {model.getAbsolutePath(), tempDirectoryPath};

    BPMNTransformerDriver.main(args);

    checkGenerationEqualToExpected(fixedRules::contains, "cyclic", new File(tempDirectoryPath));
  }
}
