package groove.behaviorTransformer.bpmn;

import behavior.bpmn.BPMNCollaboration;
import behavior.bpmn.reader.BPMNFileReaderTestHelper;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class BPMNToGrooveComplexDiagramsTest extends BPMNToGrooveTestBase implements BPMNFileReaderTestHelper {

    /**
     * See test case <a href="https://cawemo.com/share/9b143426-50ed-4621-83af-b30e29273077">"Cyclic"</a> in cawemo.
     */
    @Test
    void testCyclic() throws IOException {
        String resourcePath = "/bpmn/bpmnModelsSemanticsTest/cyclic.bpmn";
        BPMNCollaboration collaboration = readModelFromResource(resourcePath);

        this.checkGrooveGeneration(collaboration);
    }
}
