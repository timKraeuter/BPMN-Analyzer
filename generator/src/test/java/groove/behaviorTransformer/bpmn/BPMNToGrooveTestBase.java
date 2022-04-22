package groove.behaviorTransformer.bpmn;

import behavior.bpmn.BPMNCollaboration;
import behavior.bpmn.reader.BPMNFileReaderTestHelper;
import groove.behaviorTransformer.BehaviorToGrooveTransformerTestHelper;

import java.io.IOException;

public abstract class BPMNToGrooveTestBase extends BehaviorToGrooveTransformerTestHelper implements BPMNFileReaderTestHelper {
    public static final String BPMN_BPMN_MODELS_SEMANTICS_TEST_FOLDER = "/bpmn/bpmnModelsSemanticsTest/";

    static final String TYPE_GRAPH_FILE_NAME = "bpmn_e_model.gty";
    static final String TERMINATE_RULE_FILE_NAME = "Terminate.gpr";
    static final String INTERRUPTED_RULE_FILE_NAME = "SubprocessInterrupted.gpr";
    private static final String BPMN = "bpmn";

    @Override
    protected void setUpFurther() {
        // Default is to ignore the type graph, the terminate rule and the interrupt rule.
        this.setFileNameFilter(x -> x.equals(TYPE_GRAPH_FILE_NAME) || x.equals(TERMINATE_RULE_FILE_NAME) || x.equals(INTERRUPTED_RULE_FILE_NAME));
    }

    @Override
    public String getOutputPathSubFolderName() {
        return BPMN;
    }

    protected void testGrooveGenerationForBPMNResourceFile(String resourceFileName) throws IOException {
        String resourcePath = BPMN_BPMN_MODELS_SEMANTICS_TEST_FOLDER + resourceFileName;
        BPMNCollaboration collaboration = readModelFromResource(resourcePath);

        this.checkGrooveGeneration(collaboration);
    }
}
