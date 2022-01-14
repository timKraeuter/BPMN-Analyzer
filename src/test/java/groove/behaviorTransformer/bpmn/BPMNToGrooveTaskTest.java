package groove.behaviorTransformer.bpmn;

import behavior.bpmn.BPMNProcessModel;
import behavior.bpmn.Task;
import behavior.bpmn.auxiliary.BPMNProcessBuilder;
import behavior.bpmn.events.EndEvent;
import behavior.bpmn.events.StartEvent;
import behavior.bpmn.gateways.ExclusiveGateway;
import behavior.bpmn.gateways.ParallelGateway;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class BPMNToGrooveTaskTest extends BPMNToGrooveTest {

    /**
     * See test case <a href="https://cawemo.com/share/e9bca9c5-c750-487f-becf-737bbd6ea19b">"Sequential Activities"</a> in cawemo.
     */
    @Test
    void testSequentialActivities() throws IOException {
        final StartEvent start = new StartEvent("start");
        Task a = new Task("A");
        Task b = new Task("B");
        final EndEvent end = new EndEvent("end");

        final String modelName = "sequentialActivities";
        final BPMNProcessModel processModel = new BPMNProcessBuilder()
                .name(modelName)
                .startEvent(start)
                .sequenceFlow(start, a)
                .sequenceFlow(a, b)
                .sequenceFlow(b, end)
                .endEvent(end)
                .build();
        // TODO: test prefix
        this.setFileNameFilter(x -> false); // Expect type graph here.
        this.checkGrooveGeneration(processModel);
    }

    /**
     * See test case <a href="https://cawemo.com/share/9fdaa163-2b27-4787-99df-1ecf55971f14">"Implicit exclusive gateway"</a> in cawemo.
     */
    @Test
    void testImplicitExclusiveGateway() throws IOException {
        final StartEvent start = new StartEvent("start");
        final ExclusiveGateway e1 = new ExclusiveGateway("e1");
        Task a = new Task("A");
        Task b = new Task("B");
        final EndEvent end = new EndEvent("end");

        final String modelName = "implicitExclusiveGateway";
        final BPMNProcessModel processModel = new BPMNProcessBuilder()
                .name(modelName)
                .startEvent(start)
                .sequenceFlow(start, e1)
                .sequenceFlow(e1, a)
                .sequenceFlow(e1, b)
                .sequenceFlow(a, b).sequenceFlow(b, end)
                .endEvent(end)
                .build();

        this.checkGrooveGeneration(processModel);
    }

    /**
     * See test case <a href="https://cawemo.com/share/5e855137-d237-4bf7-bbf4-639c8e6093e0">"Implicit parallel gateway"</a> in cawemo.
     */
    @Test
    void testImplicitParallelGateway() throws IOException {
        final StartEvent start = new StartEvent("start");
        final ParallelGateway p1 = new ParallelGateway("p1");
        Task a = new Task("A");
        Task b = new Task("B");
        Task c = new Task("C");
        final EndEvent end = new EndEvent("end");

        final String modelName = "implicitParallelGateway";
        final BPMNProcessModel processModel = new BPMNProcessBuilder()
                .name(modelName)
                .startEvent(start)
                .sequenceFlow(start, a)
                .sequenceFlow(a, b)
                .sequenceFlow(a, c)
                .sequenceFlow(b, p1)
                .sequenceFlow(c, p1)
                .sequenceFlow(p1, end)
                .endEvent(end)
                .build();

        this.checkGrooveGeneration(processModel);
    }
}
