package behavior.bpmn.reader;

import behavior.bpmn.BPMNCollaboration;

import java.io.File;
import java.util.function.UnaryOperator;

import static groove.behaviortransformer.bpmn.BPMNToGrooveTestBase.BPMN_BPMN_MODELS_SEMANTICS_TEST_FOLDER;

public interface BPMNFileReaderTestHelper {

    default BPMNCollaboration readModelFromResourceFolder(String resourceFileName) {
        String resourcePath = BPMN_BPMN_MODELS_SEMANTICS_TEST_FOLDER + resourceFileName;
        return readModelFromResource(resourcePath);
    }

    default BPMNCollaboration readModelFromResource(String resourcePath) {
        @SuppressWarnings("ConstantConditions") File model =
                new File(this.getClass().getResource(resourcePath).getFile());
        BPMNFileReader bpmnFileReader = new BPMNFileReader();
        return bpmnFileReader.readModelFromFile(model);
    }

    default BPMNCollaboration readModelFromResource(String resourcePath,
                                                    UnaryOperator<String> elementNameTransformer) {
        @SuppressWarnings("ConstantConditions") File model =
                new File(this.getClass().getResource(resourcePath).getFile());
        BPMNFileReader bpmnFileReader = new BPMNFileReader(elementNameTransformer);
        return bpmnFileReader.readModelFromFile(model);
    }
}
