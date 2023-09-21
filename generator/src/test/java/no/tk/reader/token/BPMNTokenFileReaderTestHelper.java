package no.tk.reader.token;

import java.io.IOException;
import java.nio.file.Path;
import no.tk.behavior.bpmn.reader.token.BPMNTokenFileReader;
import no.tk.behavior.bpmn.reader.token.model.BPMNProcessSnapshot;
import no.tk.util.FileTestHelper;

public interface BPMNTokenFileReaderTestHelper {

  String AP_TEST_PATH = "bpmn/atomicPropositions/";

  default BPMNProcessSnapshot readBPMNSnapshotFromResource(String resourcePath) throws IOException {
    @SuppressWarnings("ConstantConditions")
    Path model = FileTestHelper.getResource(AP_TEST_PATH + resourcePath);
    BPMNTokenFileReader bpmnTokenFileReader = new BPMNTokenFileReader();
    return bpmnTokenFileReader.readModelFromFilePath(model);
  }
}
