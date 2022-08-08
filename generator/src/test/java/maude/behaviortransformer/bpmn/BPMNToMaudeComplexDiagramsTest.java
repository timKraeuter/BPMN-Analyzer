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
        // frewrite [25] init .
        // The command above leads to termination but there is an infinite cycle in the state space!
        testBPMNMaudeGeneration("cyclic");
    }
}
