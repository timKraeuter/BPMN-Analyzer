package behavior.bpmn.reader;

import static groove.behaviortransformer.bpmn.BPMNToGrooveTestBase.BPMN_BPMN_MODELS_SEMANTICS_TEST_FOLDER;

import behavior.bpmn.BPMNCollaboration;
import java.io.IOException;
import java.nio.file.Path;
import java.util.function.UnaryOperator;
import util.FileTestHelper;

public interface BPMNFileReaderTestHelper {

  default BPMNCollaboration readModelFromResourceFolder(String resourceFileName)
      throws IOException {
    String resourcePath = BPMN_BPMN_MODELS_SEMANTICS_TEST_FOLDER + resourceFileName;
    return readModelFromResource(resourcePath);
  }

  default BPMNCollaboration readModelFromResourceFolder(
      String resourceFileName, UnaryOperator<String> elementNameTransformer) throws IOException {
    String resourcePath = BPMN_BPMN_MODELS_SEMANTICS_TEST_FOLDER + resourceFileName;
    return readModelFromResource(resourcePath, elementNameTransformer);
  }

  default BPMNCollaboration readModelFromResource(String resourcePath) throws IOException {
    return readModelFromResource(resourcePath, elementName -> elementName);
  }

  default BPMNCollaboration readModelFromResource(
      String resourcePath, UnaryOperator<String> elementNameTransformer) throws IOException {
    @SuppressWarnings("ConstantConditions")
    Path model = FileTestHelper.getResource(resourcePath);
    BPMNFileReader bpmnFileReader = new BPMNFileReader(elementNameTransformer);
    return bpmnFileReader.readModelFromFilePath(model);
  }
}
