package groove.behaviorTransformer.bpmn;

import behavior.bpmn.BPMNCollaboration;
import behavior.bpmn.reader.BPMNFileReaderTestHelper;
import com.google.common.collect.Sets;
import groove.behaviorTransformer.BehaviorToGrooveTransformerTestHelper;

import java.io.IOException;
import java.util.Set;

public abstract class BPMNToGrooveTestBase extends BehaviorToGrooveTransformerTestHelper implements BPMNFileReaderTestHelper {
    public static final String BPMN_BPMN_MODELS_SEMANTICS_TEST_FOLDER = "/bpmn/bpmnModelsSemanticsTest/";
    static final Set<String> fixedRules = Sets.newHashSet(BPMNToGrooveTransformer.ALL_TERMINATED_FILE_NAME,
                                                          BPMNToGrooveTransformer.TERMINATE_RULE_FILE_NAME,
                                                          BPMNToGrooveTransformer.UNSAFE_FILE_NAME,
                                                          BPMNToGrooveTransformer.TYPE_GRAPH_FILE_NAME);
    private static final String BPMN = "bpmn";

    @Override
    protected void setUpFurther() {
        // Default is to ignore the type graph, the terminate rule and the interrupt rule.
        this.setFileNameFilter(fixedRules::contains);
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
