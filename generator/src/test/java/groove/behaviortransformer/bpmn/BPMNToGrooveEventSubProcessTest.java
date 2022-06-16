package groove.behaviortransformer.bpmn;

import org.junit.jupiter.api.Test;

import java.io.IOException;

class BPMNToGrooveEventSubProcessTest extends BPMNToGrooveTestBase {


    /**
     * See test case
     * <a href="https://cawemo.com/share/fcd16a64-f192-49f6-ac34-e42af8d246f7">"Event sub process - Non-interrupting"</a> in cawemo.
     */
    @Test
    void testEventSubProcessNonInterrupting() throws IOException {
        testGrooveGenerationForBPMNResourceFile("event-sub-process-non-interrupting.bpmn");
    }


    /**
     * See test case
     * <a href="https://cawemo.com/share/ebb4b70a-f16a-4716-963e-2e6cda16550f">"Event sub process - Interrupting"</a>
     * in cawemo.
     */
    @Test
    void testEventSubProcessInterrupting() throws IOException {
        testGrooveGenerationForBPMNResourceFile("event-sub-process-interrupting.bpmn");
    }
}
