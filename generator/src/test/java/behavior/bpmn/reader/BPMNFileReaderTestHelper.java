package behavior.bpmn.reader;

import behavior.bpmn.BPMNCollaboration;

import java.io.File;
import java.util.function.Function;

public interface BPMNFileReaderTestHelper {

    default BPMNCollaboration readModelFromResource(String resourcePath) {
        @SuppressWarnings("ConstantConditions") File model =
                new File(this.getClass().getResource(resourcePath).getFile());
        BPMNFileReader bpmnFileReader = new BPMNFileReader();
        return bpmnFileReader.readModelFromFile(model);
    }

    default BPMNCollaboration readModelFromResource(String resourcePath,
                                                    Function<String, String> elementNameTransformer) {
        @SuppressWarnings("ConstantConditions") File model =
                new File(this.getClass().getResource(resourcePath).getFile());
        BPMNFileReader bpmnFileReader = new BPMNFileReader(elementNameTransformer);
        return bpmnFileReader.readModelFromFile(model);
    }
}
