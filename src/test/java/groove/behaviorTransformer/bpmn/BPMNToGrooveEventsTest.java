package groove.behaviorTransformer.bpmn;

import behavior.bpmn.BPMNCollaboration;
import behavior.bpmn.auxiliary.BPMNCollaborationBuilder;
import behavior.bpmn.events.*;
import org.junit.jupiter.api.Test;

import java.io.IOException;

class BPMNToGrooveEventsTest extends BPMNToGrooveTestBase {

    /**
     * See test case <a href="https://cawemo.com/share/e1777355-d0cc-45d0-8f01-87d08ba2b5ef">"Two End Events"</a> in cawemo.
     */
    @Test
    void testTwoEndEvents() throws IOException {
        testGrooveGenerationForBPMNResourceFile("two-end-events.bpmn");
    }

    /**
     * See test case <a href="https://cawemo.com/share/519f49aa-e3ec-4d6d-8425-3933f93f974d">"Link Event"</a> in cawemo.
     */
    @Test
    void testLinkEvent() throws IOException {
        testGrooveGenerationForBPMNResourceFile("link-event.bpmn");
    }

    /**
     * See test case <a href="https://cawemo.com/share/e579995b-65f3-4146-a974-f136f5fd949b">"Terminate End Event"</a> in cawemo.
     */
    @Test
    void testTerminateEndEvent() throws IOException {
        testGrooveGenerationForBPMNResourceFile("terminate-end-event.bpmn");
    }

    /**
     * See test case <a href="https://cawemo.com/share/e6a2eb93-b0e7-4c09-baa0-93ff18084d0e">"Message Events"</a> in cawemo.
     */
    @Test
    void testMessageEvents() throws IOException {
        testGrooveGenerationForBPMNResourceFile("message-events.bpmn");
    }

    /**
     * See test case <a href="https://cawemo.com/share/13dda53f-c28d-446f-9330-2cf73814c657">"Signal Events"</a> in cawemo.
     */
    @Test
    void testSignalEvents() throws IOException {
        testGrooveGenerationForBPMNResourceFile("signal-events.bpmn");
    }

    /**
     * See test case <a href="https://cawemo.com/share/350bbe89-8c14-4ec2-a059-999a98ce92ea">"Signal events - Cross Process"</a> in cawemo.
     */
    @Test
    void testSignalEventsCrossProcess() throws IOException {
        final EventDefinition s1 = new EventDefinition("s1");
        // p1
        final StartEvent start_p1 = new StartEvent("start_p1");
        IntermediateThrowEvent s1_throw = new IntermediateThrowEvent("S1_Throw", IntermediateThrowEventType.SIGNAL, s1);
        final EndEvent end_p1 = new EndEvent("end_p1");

        // p2
        StartEvent start_p2 = new StartEvent("start_p2");
        IntermediateCatchEvent s1_catch = new IntermediateCatchEvent("S1_Catch", IntermediateCatchEventType.SIGNAL, s1);
        EndEvent end_p2 = new EndEvent("end_p2");

        final String modelName = "signalEventsCrossProcess";

        final BPMNCollaboration signalModel = new BPMNCollaborationBuilder()
                .name(modelName)
                .processName("p1")
                .startEvent(start_p1)
                .sequenceFlow(start_p1, s1_throw)
                .sequenceFlow(s1_throw, end_p1)
                .buildProcess()
                .processName("p2")
                .startEvent(start_p2)
                .sequenceFlow(start_p2, s1_catch)
                .sequenceFlow(s1_catch, end_p2)
                .buildProcess()
                .build();

        this.checkGrooveGeneration(signalModel);
    }

    /**
     * See test case <a href="https://cawemo.com/share/b2db6ccf-1d3b-49c2-8739-0c53c069fd61">"Signal Start Events"</a> in cawemo.
     */
    @Test
    void testSignalStartEvents() throws IOException {
        final EventDefinition s1 = new EventDefinition("s1");
        // p1
        final StartEvent start = new StartEvent("start");
        final EndEvent s1_throw = new EndEvent("S1_Throw", EndEventType.SIGNAL, s1);
        StartEvent p1_s1_catch = new StartEvent("p1_S1_Catch", StartEventType.SIGNAL, s1);
        EndEvent end = new EndEvent("end");

        // p2
        StartEvent p2_s1_catch = new StartEvent("p2_S1_Catch", StartEventType.SIGNAL, s1);
        EndEvent end_p2 = new EndEvent("end_p2");

        final String modelName = "signalStartEvents";

        final BPMNCollaboration signalModel = new BPMNCollaborationBuilder()
                .name(modelName)
                .processName("p1")
                .startEvent(start)
                .sequenceFlow(start, s1_throw)
                .sequenceFlow(p1_s1_catch, end)
                .buildProcess()
                .processName("p2")
                .startEvent(p2_s1_catch)
                .sequenceFlow(p2_s1_catch, end_p2)
                .buildProcess()
                .build();

        this.checkGrooveGeneration(signalModel);
    }

    /**
     * See test case <a href="https://cawemo.com/share/15e29a24-e35b-40b8-b09a-b63f3bc91268">"Timer events"</a> in cawemo.
     */
    @Test
    void testTimerEvents() throws IOException {
        final StartEvent start = new StartEvent("start");
        final IntermediateCatchEvent timer = new IntermediateCatchEvent("timer", IntermediateCatchEventType.TIMER);
        EndEvent end = new EndEvent("end");

        final String modelName = "timerEvents";

        final BPMNCollaboration timerModel = new BPMNCollaborationBuilder()
                .name(modelName)
                .processName(modelName)
                .startEvent(start)
                .sequenceFlow(start, timer)
                .sequenceFlow(timer, end)
                .build();

        this.checkGrooveGeneration(timerModel);
    }

    /**
     * See test case <a href="https://cawemo.com/share/44d74e7b-f940-48cd-8ceb-d23976b4da2b">"Two Incoming Message Flows"</a> in cawemo.
     */
    @Test
    void testTwoIncomingMessageFlows() throws IOException {
        final StartEvent start_p1 = new StartEvent("start_p1");
        final EndEvent end_p1 = new EndEvent("end_p1", EndEventType.MESSAGE);

        final StartEvent start_p2 = new StartEvent("start_p2");
        final IntermediateCatchEvent catch_2 = new IntermediateCatchEvent("Catch_2", IntermediateCatchEventType.MESSAGE);
        EndEvent end_p2 = new EndEvent("end_p2");

        final StartEvent start_p3 = new StartEvent("start_p3");
        final EndEvent end_p3 = new EndEvent("end_p3", EndEventType.MESSAGE);

        final String modelName = "twoIncomingMessageFlows";

        final BPMNCollaboration messageModel = new BPMNCollaborationBuilder()
                .name(modelName)
                .messageFlow(end_p1, catch_2)
                .messageFlow(end_p3, catch_2)
                .processName("p1")
                .startEvent(start_p1)
                .sequenceFlow(start_p1, end_p1)
                .buildProcess()
                .processName("p2")
                .startEvent(start_p2)
                .sequenceFlow(start_p2, catch_2)
                .sequenceFlow(catch_2, end_p2)
                .buildProcess()
                .processName("p3")
                .startEvent(start_p3)
                .sequenceFlow(start_p3, end_p3)
                .build();

        this.checkGrooveGeneration(messageModel);
    }

    /**
     * See test case <a href="https://cawemo.com/share/0b3cb831-a6b2-4e7c-b064-0c83e887bf47">"Intermediate Throw Event"</a> in cawemo.
     */
    @Test
    void testIntermediateThrowEvent() throws IOException {
        final StartEvent start = new StartEvent("start");
        IntermediateThrowEvent intermediate = new IntermediateThrowEvent("intermediate", IntermediateThrowEventType.NONE);
        final EndEvent end = new EndEvent("end", EndEventType.MESSAGE);

        final String modelName = "intermediateThrowEvent";

        final BPMNCollaboration model = new BPMNCollaborationBuilder()
                .name(modelName)
                .processName("p1")
                .startEvent(start)
                .sequenceFlow(start, intermediate)
                .sequenceFlow(intermediate, end)
                .build();

        this.checkGrooveGeneration(model);
    }
}