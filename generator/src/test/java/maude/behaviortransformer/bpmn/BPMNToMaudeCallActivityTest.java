package maude.behaviortransformer.bpmn;

import maude.behaviortransformer.BPMNMaudeTestHelper;
import org.junit.jupiter.api.Test;

import java.io.IOException;

class BPMNToMaudeCallActivityTest implements BPMNMaudeTestHelper {


    /**
     * See test case <a href="https://cawemo.com/share/be4afe81-4adf-4b45-a933-5745316ee533">"Call activity - Simple"</a> in cawemo.
     */
    @Test
    void testCallActivity() throws IOException {
        testBPMNMaudeGeneration("call-activity-simple");
    }
}
