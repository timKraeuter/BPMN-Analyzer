package groove.behaviorTransformer.bpmn;

import behavior.bpmn.BPMNProcessModel;
import behavior.bpmn.Task;
import behavior.bpmn.auxiliary.BPMNProcessBuilder;
import behavior.bpmn.events.*;
import behavior.bpmn.gateways.ExclusiveGateway;
import behavior.bpmn.gateways.ParallelGateway;
import org.junit.jupiter.api.Test;

import java.io.IOException;

class BPMNToGrooveEventAndAdvancedTest extends BPMNToGrooveTest {

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

    /**
     * See test case <a href="https://cawemo.com/share/e1777355-d0cc-45d0-8f01-87d08ba2b5ef">"[EXT]"</a> in cawemo.
     */
    @Test
    void testTwoEndEvents() throws IOException {
        final StartEvent start = new StartEvent("start");
        final ParallelGateway p1 = new ParallelGateway("p1");
        Task a1 = new Task("a1");
        Task a2 = new Task("a2");
        final EndEvent end1 = new EndEvent("end1");
        final EndEvent end2 = new EndEvent("end2");

        final String modelName = "twoEndEvents";
        final BPMNProcessModel processModel = new BPMNProcessBuilder()
                .name(modelName)
                .startEvent(start)
                .sequenceFlow(start, p1)
                .sequenceFlow(p1, a1)
                .sequenceFlow(p1, a2)
                .sequenceFlow(a1, end1)
                .sequenceFlow(a2, end2)
                .endEvent(end1)
                .endEvent(end2)
                .build();

        this.checkGrooveGeneration(processModel);
    }

    /**
     * See test case <a href="https://cawemo.com/share/519f49aa-e3ec-4d6d-8425-3933f93f974d">"Link Event"</a> in cawemo.
     */
    @Test
    void testLinkEvent() throws IOException {
        final StartEvent start = new StartEvent("start");
        final ParallelGateway p1 = new ParallelGateway("p1");
        LinkEvent throw_link1 = new LinkEvent("Link1", LinkEventType.THROW);
        LinkEvent throw_link2 = new LinkEvent("Link2", LinkEventType.THROW);
        LinkEvent catch_link1 = new LinkEvent("Link1", LinkEventType.CATCH);
        LinkEvent catch_link2 = new LinkEvent("Link2", LinkEventType.CATCH);
        final ParallelGateway p2 = new ParallelGateway("p2");
        final EndEvent end = new EndEvent("end");

        final String modelName = "linkEvent";
        final BPMNProcessModel processModel = new BPMNProcessBuilder()
                .name(modelName)
                .startEvent(start)
                .sequenceFlow(start, p1)
                .sequenceFlow(p1, throw_link1)
                .sequenceFlow(p1, throw_link2)
                .sequenceFlow(catch_link1, p2)
                .sequenceFlow(catch_link2, p2)
                .sequenceFlow(p2, end)
                .endEvent(end)
                .build();

        this.checkGrooveGeneration(processModel);
    }

    /**
     * See test case <a href="https://cawemo.com/share/e579995b-65f3-4146-a974-f136f5fd949b">"Terminate End Event"</a> in cawemo.
     */
    @Test
    void testTerminateEndEvent() throws IOException {
        final StartEvent start = new StartEvent("start");
        final ParallelGateway p1 = new ParallelGateway("p1");
        Task a = new Task("A");
        Task b = new Task("B");
        final EndEvent end = new EndEvent("end");
        final EndEvent terminate_end = new EndEvent("terminate_end", EndEventType.TERMINATION);

        final String modelName = "terminateEndEvent";
        final BPMNProcessModel processModel = new BPMNProcessBuilder()
                .name(modelName)
                .startEvent(start)
                .sequenceFlow(start, p1)
                .sequenceFlow(p1, a)
                .sequenceFlow(p1, b)
                .sequenceFlow(a, end)
                .sequenceFlow(b, terminate_end)
                .endEvent(end)
                .endEvent(terminate_end)
                .build();

        this.checkGrooveGeneration(processModel);
    }
}