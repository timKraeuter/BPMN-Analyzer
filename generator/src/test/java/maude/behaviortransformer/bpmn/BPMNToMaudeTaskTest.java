package maude.behaviortransformer.bpmn;

import maude.behaviortransformer.MaudeTestHelper;
import org.junit.jupiter.api.Test;

import java.io.IOException;

class BPMNToMaudeTaskTest implements MaudeTestHelper {

    /**
     * See test case <a href="https://cawemo.com/share/e9bca9c5-c750-487f-becf-737bbd6ea19b">"Sequential Tasks"</a>
     * in cawemo.
     */
    @Test
    void testSequentialTasks() throws IOException {
        testBPMNMaudeGeneration("sequential-activities");
    }
}
