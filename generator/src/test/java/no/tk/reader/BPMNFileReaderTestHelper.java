package no.tk.reader;

import static no.tk.groove.behaviortransformer.bpmn.BPMNToGrooveTestBase.BPMN_MODELS_SEMANTICS_TEST_FOLDER;

import java.io.IOException;
import java.nio.file.Path;
import java.util.function.UnaryOperator;
import no.tk.behavior.bpmn.BPMNCollaboration;
import no.tk.behavior.bpmn.reader.BPMNFileReader;
import no.tk.util.FileTestHelper;

public interface BPMNFileReaderTestHelper {

  default BPMNCollaboration readModelFromResourceFolder(String resourceFileName)
      throws IOException {
    String resourcePath = BPMN_MODELS_SEMANTICS_TEST_FOLDER + resourceFileName;
    return readModelFromResource(resourcePath);
  }

  default BPMNCollaboration readModelFromResourceFolder(
      String resourceFileName, UnaryOperator<String> elementNameTransformer) throws IOException {
    String resourcePath = BPMN_MODELS_SEMANTICS_TEST_FOLDER + resourceFileName;
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
