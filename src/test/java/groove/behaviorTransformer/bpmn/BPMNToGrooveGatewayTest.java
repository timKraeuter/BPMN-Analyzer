package groove.behaviorTransformer.bpmn;

import behavior.bpmn.BPMNProcessModel;
import behavior.bpmn.Task;
import behavior.bpmn.auxiliary.BPMNProcessBuilder;
import behavior.bpmn.events.EndEvent;
import behavior.bpmn.events.StartEvent;
import behavior.bpmn.gateways.ExclusiveGateway;
import behavior.bpmn.gateways.InclusiveGateway;
import behavior.bpmn.gateways.ParallelGateway;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class BPMNToGrooveGatewayTest extends BPMNToGrooveTestBase {

    /**
     * See test case <a href="https://cawemo.com/share/9f739e59-c250-4f84-96cd-191914b07296">"Exclusive Gateway"</a> in cawemo.
     */
    @Test
    void testExclusiveGateway() throws IOException {
        // Build the process model from the NWPT example.
        final StartEvent start = new StartEvent("start");
        final EndEvent end = new EndEvent("end");
        Task a0 = new Task("a0");
        Task a1 = new Task("a1");
        final ExclusiveGateway e1 = new ExclusiveGateway("e1");
        Task a2_1 = new Task("a2_1");
        Task a2_2 = new Task("a2_2");
        final ExclusiveGateway e2 = new ExclusiveGateway("e2");
        Task a3 = new Task("a3");

        final String modelName = "exclusiveGateway";
        final BPMNProcessModel processModel = new BPMNProcessBuilder()
                .name(modelName)
                .startEvent(start)
                .sequenceFlow(start, a0)
                .sequenceFlow(a0, a1)
                .sequenceFlow(a1, e1)
                .sequenceFlow(e1, a2_1)
                .sequenceFlow(e1, a2_2)
                .sequenceFlow(a2_1, e2)
                .sequenceFlow(a2_2, e2)
                .sequenceFlow(e2, a3)
                .sequenceFlow(a3, end)
                .endEvent(end)
                .build();

        // Exclusive Gateways rules could be optimized, by setting the token position to the exlusive gateway not the individual flow incoming.
        // This leads to only one rule for each outgoing flow not also incoming flow and combinations!
        this.checkGrooveGeneration(processModel);
    }

    /**
     * See test case <a href="https://cawemo.com/share/7ac506cd-86f7-4c89-a946-1ab2b3707d92">"Parallel Gateway"</a> in cawemo.
     */
    @Test
    void testParallelGateway() throws IOException {
        final StartEvent start = new StartEvent("start");
        Task a0 = new Task("a0");
        Task a1 = new Task("a1");
        final ParallelGateway p1 = new ParallelGateway("p1");
        Task a2_1 = new Task("a2_1");
        Task a2_2 = new Task("a2_2");
        final ParallelGateway p2 = new ParallelGateway("p2");
        Task a3 = new Task("a3");
        final EndEvent end = new EndEvent("end");

        final String modelName = "parallelGateway";
        final BPMNProcessModel processModel = new BPMNProcessBuilder()
                .name(modelName)
                .startEvent(start)
                .sequenceFlow(start, a0)
                .sequenceFlow(a0, a1)
                .sequenceFlow(a1, p1)
                .sequenceFlow(p1, a2_1)
                .sequenceFlow(p1, a2_2)
                .sequenceFlow(a2_1, p2)
                .sequenceFlow(a2_2, p2)
                .sequenceFlow(p2, a3)
                .sequenceFlow(a3, end)
                .endEvent(end)
                .build();

        this.checkGrooveGeneration(processModel);
    }

    /**
     * See test case <a href="https://cawemo.com/share/d0d0439d-31da-4b34-b508-aa75bc2551c8">"Parallel Gateway - Complex"</a> in cawemo.
     */
    @Test
    void testParallelGatewayComplex() throws IOException {
        final StartEvent start = new StartEvent("start");
        Task a0 = new Task("a0");
        Task a1 = new Task("a1");
        final ParallelGateway p1 = new ParallelGateway("p1");
        Task a2_1 = new Task("a2_1");
        Task a2_2 = new Task("a2_2");
        final ParallelGateway p2 = new ParallelGateway("p2");
        Task a3 = new Task("a3");
        final ParallelGateway p3 = new ParallelGateway("p3");
        final EndEvent end = new EndEvent("end");

        final String modelName = "parallelGateway_complex";
        final BPMNProcessModel processModel = new BPMNProcessBuilder()
                .name(modelName)
                .startEvent(start)
                .sequenceFlow(start, a0)
                .sequenceFlow(a0, a1)
                .sequenceFlow(a1, p1)
                .sequenceFlow(p1, a2_1)
                .sequenceFlow(p1, a2_2)
                .sequenceFlow(a2_2, p2)
                .sequenceFlow(a2_1, p3)
                .sequenceFlow(p2, a3)
                .sequenceFlow(p2, p3)
                .sequenceFlow(a3, p3)
                .sequenceFlow(p3, end)
                .endEvent(end)
                .build();

        this.checkGrooveGeneration(processModel);
    }

    /**
     * See test case <a href="https://cawemo.com/share/e5ab5920-be7c-435f-8d58-964760455caf">"Inclusive gateway"</a> in cawemo.
     */
    @Test
    void testInclusiveGateway() throws IOException {
        final StartEvent start = new StartEvent("start");
        final InclusiveGateway i1 = new InclusiveGateway("i1");
        Task a = new Task("A");
        Task b = new Task("B");
        final InclusiveGateway i2 = new InclusiveGateway("i2");
        final EndEvent end = new EndEvent("end");

        final String modelName = "inclusiveGateway";
        final BPMNProcessModel processModel = new BPMNProcessBuilder()
                .name(modelName)
                .startEvent(start)
                .sequenceFlow(start, i1)
                .sequenceFlow(i1, a)
                .sequenceFlow(i1, b)
                .sequenceFlow(a, i2)
                .sequenceFlow(b, i2)
                .sequenceFlow(i2, end)
                .endEvent(end)
                .build();

        this.checkGrooveGeneration(processModel);
    }

    /**
     * See test case <a href="https://cawemo.com/share/4edc1064-1a2f-46ba-b4bd-9bd3fceea7ae">"Inclusive gateway complex"</a> in cawemo.
     */
    @Test
    void testInclusiveGatewayComplex() throws IOException {
        final StartEvent start = new StartEvent("start");
        final InclusiveGateway i1 = new InclusiveGateway("i1");
        Task a = new Task("A");
        final ParallelGateway p1 = new ParallelGateway("p1");
        Task b = new Task("B");
        Task c = new Task("C");
        final ParallelGateway p2 = new ParallelGateway("p2");
        final InclusiveGateway i2 = new InclusiveGateway("i2");
        final EndEvent end = new EndEvent("end");

        final String modelName = "inclusiveGatewayComplex";
        final BPMNProcessModel processModel = new BPMNProcessBuilder()
                .name(modelName)
                .startEvent(start)
                .sequenceFlow(start, i1)
                .sequenceFlow(i1, a)
                .sequenceFlow(i1, p1)
                .sequenceFlow(a, i2)
                .sequenceFlow(p1, b)
                .sequenceFlow(p1, c)
                .sequenceFlow(b, p2)
                .sequenceFlow(c, p2)
                .sequenceFlow(p2, i2)
                .sequenceFlow(i2, end)
                .endEvent(end)
                .build();

        this.checkGrooveGeneration(processModel);
    }

}
