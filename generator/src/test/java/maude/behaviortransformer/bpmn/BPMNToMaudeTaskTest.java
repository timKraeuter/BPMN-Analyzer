package maude.behaviortransformer.bpmn;

import behavior.bpmn.BPMNCollaboration;
import behavior.bpmn.reader.BPMNFileReaderTestHelper;
import org.junit.jupiter.api.Test;

import static groove.behaviortransformer.bpmn.BPMNToGrooveTestBase.BPMN_BPMN_MODELS_SEMANTICS_TEST_FOLDER;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class BPMNToMaudeTaskTest implements BPMNFileReaderTestHelper {

    /**
     * See test case <a href="https://cawemo.com/share/e9bca9c5-c750-487f-becf-737bbd6ea19b">"Sequential Tasks"</a>
     * in cawemo.
     */
    @Test
    void testSequentialTasks() {
        BPMNCollaboration collaboration = readModelFromResourceFolder("sequential-activities.bpmn");

        BPMNToMaudeTransformer transformer = new BPMNToMaudeTransformer(collaboration);

        String maudeModule = transformer.generate("<> True");
        System.out.println(maudeModule);

        assertThat(maudeModule, is("abc"));
    }

    private BPMNCollaboration readModelFromResourceFolder(String resourceFileName) {
        String resourcePath = BPMN_BPMN_MODELS_SEMANTICS_TEST_FOLDER + resourceFileName;
        return readModelFromResource(resourcePath);
    }


}
