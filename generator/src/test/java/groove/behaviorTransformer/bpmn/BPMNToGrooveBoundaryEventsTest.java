package groove.behaviorTransformer.bpmn;

import org.junit.jupiter.api.Test;

import java.io.IOException;

public class BPMNToGrooveBoundaryEventsTest extends BPMNToGrooveTestBase {

    /**
     * See test case <a href="https://cawemo.com/share/3b55577a-e7ed-4729-a046-4d79fd11c941">"Subprocess - Interrupting Boundary Events"</a> in cawemo.
     */
    @Test
    void testSubProcessInterruptingBoundaryEvents() throws IOException {
        testGrooveGenerationForBPMNResourceFile("subprocess-interrupting-boundary-events.bpmn");
    }

    // TODO: Non-Interrupting test + Task tests.
}
