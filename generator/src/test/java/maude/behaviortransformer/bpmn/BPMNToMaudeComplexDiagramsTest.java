package maude.behaviortransformer.bpmn;

import maude.behaviortransformer.BPMNMaudeTestHelper;
import org.junit.jupiter.api.Test;

import java.io.IOException;

class BPMNToMaudeComplexDiagramsTest implements BPMNMaudeTestHelper {

    /**
     * See test case <a href="https://cawemo.com/share/9b143426-50ed-4621-83af-b30e29273077">"Cyclic"</a> in cawemo.
     */
    @Test
    void testCyclic() throws IOException {
        testBPMNMaudeGenerationWithCustomQuery("cyclic", CAN_TERMINATE_QUERY);
    }
}
