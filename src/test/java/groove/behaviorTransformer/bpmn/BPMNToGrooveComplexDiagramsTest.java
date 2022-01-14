package groove.behaviorTransformer.bpmn;

import behavior.bpmn.BPMNProcessModel;
import behavior.bpmn.Task;
import behavior.bpmn.auxiliary.BPMNProcessBuilder;
import behavior.bpmn.events.EndEvent;
import behavior.bpmn.events.StartEvent;
import behavior.bpmn.gateways.ExclusiveGateway;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class BPMNToGrooveComplexDiagramsTest extends BPMNToGrooveTestBase {

    /**
     * See test case <a href="https://cawemo.com/share/9b143426-50ed-4621-83af-b30e29273077">"[CYC]"</a> in cawemo.
     */
    @Test
    void testCyclic() throws IOException {
        final StartEvent start = new StartEvent("start");
        final EndEvent end = new EndEvent("end");
        Task a0 = new Task("a0");
        final ExclusiveGateway e1 = new ExclusiveGateway("e1");
        Task a1 = new Task("a1");
        Task a2 = new Task("a2");
        Task a3 = new Task("a3");
        final ExclusiveGateway e2 = new ExclusiveGateway("e2");
        final ExclusiveGateway e3 = new ExclusiveGateway("e3");
        final ExclusiveGateway e4 = new ExclusiveGateway("e4");

        final String modelName = "cyclic";
        final BPMNProcessModel processModel = new BPMNProcessBuilder()
                .name(modelName)
                .startEvent(start)
                .sequenceFlow(start, a0)
                .sequenceFlow(a0, e1)
                .sequenceFlow(e1, a1)
                .sequenceFlow(e1, a2)
                .sequenceFlow(a1, e2)
                .sequenceFlow(a2, e3)
                .sequenceFlow(e2, a3)
                .sequenceFlow(a3, e3)
                .sequenceFlow(e3, e4)
                .sequenceFlow(e4, e2)
                .sequenceFlow(e4, end)
                .endEvent(end)
                .build();

        this.checkGrooveGeneration(processModel);
    }
}
