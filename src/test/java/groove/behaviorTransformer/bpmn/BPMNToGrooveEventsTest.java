package groove.behaviorTransformer.bpmn;

import behavior.bpmn.BPMNCollaboration;
import behavior.bpmn.activities.Task;
import behavior.bpmn.auxiliary.BPMNCollaborationBuilder;
import behavior.bpmn.events.*;
import behavior.bpmn.gateways.ParallelGateway;
import org.junit.jupiter.api.Test;

import java.io.IOException;

class BPMNToGrooveEventsTest extends BPMNToGrooveTestBase {

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
        final BPMNCollaboration collaboration = new BPMNCollaborationBuilder()
                .name(modelName)
                .processName(modelName)
                .startEvent(start)
                .sequenceFlow(start, p1)
                .sequenceFlow(p1, a1)
                .sequenceFlow(p1, a2)
                .sequenceFlow(a1, end1)
                .sequenceFlow(a2, end2)
                .build();

        this.checkGrooveGeneration(collaboration);
    }

    /**
     * See test case <a href="https://cawemo.com/share/519f49aa-e3ec-4d6d-8425-3933f93f974d">"Link Event"</a> in cawemo.
     */
    @Test
    void testLinkEvent() throws IOException {
        final StartEvent start = new StartEvent("start");
        final ParallelGateway p1 = new ParallelGateway("p1");
        IntermediateThrowEvent throw_link1 = new IntermediateThrowEvent("Link1", IntermediateEventType.LINK);
        IntermediateThrowEvent throw_link2 = new IntermediateThrowEvent("Link2", IntermediateEventType.LINK);
        IntermediateCatchEvent catch_link1 = new IntermediateCatchEvent("Link1", IntermediateEventType.LINK);
        IntermediateCatchEvent catch_link2 = new IntermediateCatchEvent("Link2", IntermediateEventType.LINK);
        final ParallelGateway p2 = new ParallelGateway("p2");
        final EndEvent end = new EndEvent("end");

        final String modelName = "linkEvent";
        final BPMNCollaboration collaboration = new BPMNCollaborationBuilder()
                .name(modelName)
                .processName(modelName)
                .startEvent(start)
                .sequenceFlow(start, p1)
                .sequenceFlow(p1, throw_link1)
                .sequenceFlow(p1, throw_link2)
                .sequenceFlow(catch_link1, p2)
                .sequenceFlow(catch_link2, p2)
                .sequenceFlow(p2, end)
                .build();

        this.checkGrooveGeneration(collaboration);
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
        final BPMNCollaboration collaboration = new BPMNCollaborationBuilder()
                .name(modelName)
                .processName(modelName)
                .startEvent(start)
                .sequenceFlow(start, p1)
                .sequenceFlow(p1, a)
                .sequenceFlow(p1, b)
                .sequenceFlow(a, end)
                .sequenceFlow(b, terminate_end)
                .build();

        this.checkGrooveGeneration(collaboration);
    }

    /**
     * See test case <a href="https://cawemo.com/share/e6a2eb93-b0e7-4c09-baa0-93ff18084d0e">"Message Events"</a> in cawemo.
     */
//    @Test
    void testMessageEvents() throws IOException {
        final StartEvent start_p1 = new StartEvent("start_p1");
        IntermediateCatchEvent catch_p1 = new IntermediateCatchEvent("catch_p1", IntermediateEventType.MESSAGE);
        final EndEvent end_p1 = new EndEvent("end_p1", EndEventType.MESSAGE);

        final StartEvent start_p2 = new StartEvent("start_p2");
        IntermediateCatchEvent throw_p2 = new IntermediateCatchEvent("throw_p2", IntermediateEventType.MESSAGE);
        IntermediateCatchEvent catch_p2 = new IntermediateCatchEvent("catch_p2", IntermediateEventType.MESSAGE);
        final EndEvent end_p2 = new EndEvent("end_p2");

        final String modelName = "messageEvents";

        // TODO: Add message flows!
        final BPMNCollaboration p1Model = new BPMNCollaborationBuilder()
                .name(modelName)
                .processName("p1")
                .startEvent(start_p1)
                .sequenceFlow(start_p1, catch_p2)
                .sequenceFlow(catch_p2, end_p1)
                .buildProcess()
                .processName("p2")
                .startEvent(start_p2)
                .sequenceFlow(start_p2, throw_p2)
                .sequenceFlow(throw_p2, catch_p2)
                .sequenceFlow(catch_p2, end_p2)
                .build();

        this.checkGrooveGeneration(p1Model);
    }
}