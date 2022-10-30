package behavior.bpmn.reader;

import static groove.behaviortransformer.bpmn.BPMNToGrooveTestBase.BPMN_BPMN_MODELS_SEMANTICS_TEST_FOLDER;

import behavior.bpmn.BPMNCollaboration;
import java.io.File;
import java.util.function.UnaryOperator;

public interface BPMNFileReaderTestHelper {

  default BPMNCollaboration readModelFromResourceFolder(String resourceFileName) {
    String resourcePath = BPMN_BPMN_MODELS_SEMANTICS_TEST_FOLDER + resourceFileName;
    return readModelFromResource(resourcePath);
  }

  default BPMNCollaboration readModelFromResourceFolder(
      String resourceFileName, UnaryOperator<String> elementNameTransformer) {
    String resourcePath = BPMN_BPMN_MODELS_SEMANTICS_TEST_FOLDER + resourceFileName;
    return readModelFromResource(resourcePath, elementNameTransformer);
  }

  default BPMNCollaboration readModelFromResource(String resourcePath) {
    return readModelFromResource(resourcePath, elementName -> elementName);
  }

  default BPMNCollaboration readModelFromResource(
      String resourcePath, UnaryOperator<String> elementNameTransformer) {
    @SuppressWarnings("ConstantConditions")
    File model = new File(this.getClass().getResource(resourcePath).getFile());
    BPMNFileReader bpmnFileReader = new BPMNFileReader(elementNameTransformer);
    return bpmnFileReader.readModelFromFile(model);
  }
}
