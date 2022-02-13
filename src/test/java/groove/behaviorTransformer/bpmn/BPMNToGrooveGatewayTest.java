package groove.behaviorTransformer.bpmn;

import behavior.bpmn.BPMNCollaboration;
import behavior.bpmn.activities.tasks.ReceiveTask;
import behavior.bpmn.activities.tasks.Task;
import behavior.bpmn.auxiliary.BPMNCollaborationBuilder;
import behavior.bpmn.events.*;
import behavior.bpmn.gateways.EventBasedGateway;
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
        final BPMNCollaboration collaboration = new BPMNCollaborationBuilder()
                .name(modelName)
                .processName(modelName)
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
                .build();

        // Exclusive Gateways rules could be optimized, by setting the token position to the exlusive gateway not the individual flow incoming.
        // This leads to only one rule for each outgoing flow not also incoming flow and combinations!
        this.checkGrooveGeneration(collaboration);
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
        final BPMNCollaboration collaboration = new BPMNCollaborationBuilder()
                .name(modelName)
                .processName(modelName)
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
                .build();

        this.checkGrooveGeneration(collaboration);
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
        final BPMNCollaboration collaboration = new BPMNCollaborationBuilder()
                .name(modelName)
                .processName(modelName)
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
                .build();

        this.checkGrooveGeneration(collaboration);
    }

    /**
     * See test case <a href="https://cawemo.com/share/e5ab5920-be7c-435f-8d58-964760455caf">"Inclusive Gateway"</a> in cawemo.
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
        final BPMNCollaboration collaboration = new BPMNCollaborationBuilder()
                .name(modelName)
                .processName(modelName)
                .startEvent(start)
                .sequenceFlow(start, i1)
                .sequenceFlow(i1, a)
                .sequenceFlow(i1, b)
                .sequenceFlow(a, i2)
                .sequenceFlow(b, i2)
                .sequenceFlow(i2, end)
                .build();

        this.checkGrooveGeneration(collaboration);
    }

    /**
     * See test case <a href="https://cawemo.com/share/4edc1064-1a2f-46ba-b4bd-9bd3fceea7ae">"Inclusive Gateway - Complex"</a> in cawemo.
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
        final BPMNCollaboration collaboration = new BPMNCollaborationBuilder()
                .name(modelName)
                .processName(modelName)
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
                .build();

        this.checkGrooveGeneration(collaboration);
    }

    /**
     * See test case <a href="https://cawemo.com/share/c16c4923-dfa0-4a15-ade3-b47acb40ad66">"Exclusive Event Based Gateway"</a> in cawemo.
     */
    @Test
    void testExclusiveEventBasedGateway() throws IOException {
        // TODO: Add Signal event to testcase.
        final StartEvent start_p1 = new StartEvent("start_p1");
        final EventBasedGateway eventG = new EventBasedGateway("eventG");
        IntermediateCatchEvent r_msg1 = new IntermediateCatchEvent("r_msg1", IntermediateCatchEventType.MESSAGE);
        ReceiveTask r_msg2 = new ReceiveTask("r_msg2");
        final EndEvent end1_p1 = new EndEvent("end1_p1");
        final EndEvent end2_p1 = new EndEvent("end2_p1");

        final StartEvent start_p2 = new StartEvent("start_p2");
        ExclusiveGateway e1 = new ExclusiveGateway("e1");
        IntermediateThrowEvent t_msg1 = new IntermediateThrowEvent("t_msg1", IntermediateThrowEventType.MESSAGE);
        IntermediateThrowEvent t_msg2 = new IntermediateThrowEvent("t_msg2", IntermediateThrowEventType.MESSAGE);
        final EndEvent end1_p2 = new EndEvent("end1_p2");
        final EndEvent end2_p2 = new EndEvent("end2_p2");

        final String modelName = "eventBasedGateway";
        final BPMNCollaboration collaboration = new BPMNCollaborationBuilder()
                .name(modelName)
                .messageFlow(t_msg1, r_msg1)
                .messageFlow(t_msg2, r_msg2)
                .processName("p1")
                .startEvent(start_p1)
                .sequenceFlow(start_p1, eventG)
                .sequenceFlow(eventG, r_msg1)
                .sequenceFlow(eventG, r_msg2)
                .sequenceFlow(r_msg1, end1_p1)
                .sequenceFlow(r_msg2, end2_p1)
                .buildProcess()
                .processName("p2")
                .startEvent(start_p2)
                .sequenceFlow(start_p2, e1)
                .sequenceFlow(e1, t_msg1)
                .sequenceFlow(e1, t_msg2)
                .sequenceFlow(t_msg1, end1_p2)
                .sequenceFlow(t_msg2, end2_p2)
                .build();

        this.checkGrooveGeneration(collaboration);
    }

}
