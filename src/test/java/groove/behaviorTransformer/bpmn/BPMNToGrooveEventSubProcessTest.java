package groove.behaviorTransformer.bpmn;

import org.junit.jupiter.api.Test;

import java.io.IOException;

public class BPMNToGrooveEventSubProcessTest extends BPMNToGrooveTestBase {


    /**
     * See test case
     * <a href="https://cawemo.com/share/fcd16a64-f192-49f6-ac34-e42af8d246f7">"Event sub process - Non-interrupting"</a> in cawemo.
     */
    @Test
    void testEventSubProcessNonInterrupting() throws IOException {
        testGrooveGenerationForBPMNResourceFile("event-sub-process-non-interrupting.bpmn");
    }
}
