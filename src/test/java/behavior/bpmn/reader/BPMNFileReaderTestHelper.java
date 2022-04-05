package behavior.bpmn.reader;

import behavior.bpmn.BPMNCollaboration;

import java.io.File;

public interface BPMNFileReaderTestHelper {

    default BPMNCollaboration readModelFromResource(String resourcePath) {
        @SuppressWarnings("ConstantConditions") File model = new File(this.getClass().getResource(resourcePath).getFile());
        BPMNFileReader bpmnFileReader = new BPMNFileReader();
        return bpmnFileReader.readModelFromFile(model);
    }
}
