package groove.behaviortransformer.bpmn;

import org.junit.jupiter.api.Test;

import java.io.IOException;

class BPMNToGrooveEventsTest extends BPMNToGrooveTestBase {

    /**
     * See test case <a href="https://cawemo.com/share/b115e18b-0136-41a7-940a-8190c97da07e">"Multiple Start Events"</a> in cawemo.
     */
    @Test
    void testMultipleStartEvents() throws IOException {
        testGrooveGenerationForBPMNResourceFile("multiple-start-events.bpmn");
    }

    /**
     * See test case <a href="https://cawemo.com/share/e6a2eb93-b0e7-4c09-baa0-93ff18084d0e">"Message Events"</a> in cawemo.
     */
    @Test
    void testMessageEvents() throws IOException {
        testGrooveGenerationForBPMNResourceFile("message-events.bpmn");
    }

    /**
     * See test case <a href="https://cawemo.com/share/b4b588fa-3f0f-4c30-95e9-b7f5dd40cd7a">"Message Events without Message Flows"</a> in cawemo.
     */
    @Test
    void testMessageEventsNoMessageFlows() throws IOException {
        testGrooveGenerationForBPMNResourceFile("message-events-without-message-flows.bpmn");
    }

    /**
     * See test case <a href="https://cawemo.com/share/44d74e7b-f940-48cd-8ceb-d23976b4da2b">"Two Incoming Message Flows"</a> in cawemo.
     */
    @Test
    void testTwoIncomingMessageFlows() throws IOException {
        // TODO: Implement the terminating example which supersedes this one.
        testGrooveGenerationForBPMNResourceFile("two-incoming-message-flows-no-termination.bpmn");
    }

    /**
     * See test case <a href="https://cawemo.com/share/e1777355-d0cc-45d0-8f01-87d08ba2b5ef">"Two End Events"</a> in cawemo.
     */
    @Test
    void testTwoEndEvents() throws IOException {
        testGrooveGenerationForBPMNResourceFile("two-end-events.bpmn");
    }

    /**
     * See test case <a href="https://cawemo.com/share/e579995b-65f3-4146-a974-f136f5fd949b">"Terminate End Event"</a> in cawemo.
     */
    @Test
    void testTerminateEndEvent() throws IOException {
        testGrooveGenerationForBPMNResourceFile("terminate-end-event.bpmn");
    }

    /**
     * See test case <a href="https://cawemo.com/share/519f49aa-e3ec-4d6d-8425-3933f93f974d">"Link Event"</a> in cawemo.
     */
    @Test
    void testLinkEvent() throws IOException {
        testGrooveGenerationForBPMNResourceFile("link-event.bpmn");
    }

    /**
     * See test case <a href="https://cawemo.com/share/b2db6ccf-1d3b-49c2-8739-0c53c069fd61">"Signal Start Events"</a> in cawemo.
     */
    @Test
    void testSignalStartEvents() throws IOException {
        testGrooveGenerationForBPMNResourceFile("signal-start-events.bpmn");
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
        testGrooveGenerationForBPMNResourceFile("signal-events-cross-process.bpmn");
    }

    /**
     * See test case <a href="https://cawemo.com/share/15e29a24-e35b-40b8-b09a-b63f3bc91268">"Timer events"</a> in cawemo.
     */
    @Test
    void testTimerEvents() throws IOException {
        testGrooveGenerationForBPMNResourceFile("timer-events.bpmn");
    }

    /**
     * See test case <a href="https://cawemo.com/share/0b3cb831-a6b2-4e7c-b064-0c83e887bf47">"Intermediate Throw Event"</a> in cawemo.
     */
    @Test
    void testIntermediateThrowEvent() throws IOException {
        testGrooveGenerationForBPMNResourceFile("intermediate-throw-event.bpmn");
    }
}