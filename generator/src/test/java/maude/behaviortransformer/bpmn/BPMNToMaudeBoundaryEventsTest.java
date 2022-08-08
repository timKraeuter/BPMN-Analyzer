package maude.behaviortransformer.bpmn;

import maude.behaviortransformer.BPMNMaudeTestHelper;
import org.junit.jupiter.api.Test;

import java.io.IOException;

class BPMNToMaudeBoundaryEventsTest implements BPMNMaudeTestHelper {

    /**
     * See test case <a href="https://cawemo.com/share/3b55577a-e7ed-4729-a046-4d79fd11c941">"Subprocess - Interrupting Boundary Events"</a> in cawemo.
     */
    @Test
    void testSubProcessInterruptingBoundaryEvents() throws IOException {
        testBPMNMaudeGeneration("subprocess-interrupting-boundary-events");
    }

    /**
     * See test case <a href="https://cawemo.com/share/656b7c63-34e3-404f-9399-bbed9e22a8b7">"Subprocess - Non-Interrupting Boundary Events"</a> in cawemo.
     */
    @Test
    void testSubProcessNonInterruptingBoundaryEvents() throws IOException {
        // Careful infinite state space!
        testBPMNMaudeGeneration("subprocess-non-interrupting-boundary-events");
    }

    /**
     * See test case <a href="https://cawemo.com/share/9acd9a23-65d7-46cd-bad5-b5b874333567">"Task - Interrupting Boundary Events"</a> in cawemo.
     */
    @Test
    void testTaskInterruptingBoundaryEvents() throws IOException {
        testBPMNMaudeGeneration("task-interrupting-boundary-events");
    }

    /**
     * See test case <a href="https://cawemo.com/share/8a322a8a-95ff-4773-aeb7-fba9635eefbb">"Task - Non-Interrupting Boundary Events"</a> in cawemo.
     */
    @Test
    void testTaskNonInterruptingBoundaryEvents() throws IOException {
        // Careful infinite state space!
        testBPMNMaudeGeneration("task-non-interrupting-boundary-events");
    }
}
