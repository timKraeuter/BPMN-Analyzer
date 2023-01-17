package groove.behaviortransformer.bpmn;

import java.io.IOException;
import org.junit.jupiter.api.Test;

class BPMNToGrooveEventsTest extends BPMNToGrooveTestBase {

  /**
   * See test case <a href="https://cawemo.com/share/b115e18b-0136-41a7-940a-8190c97da07e">"Multiple
   * Start Events"</a> in cawemo.
   */
  @Test
  void testMultipleStartEvents() throws IOException {
    testGrooveGenerationForBPMNResourceFile("multiple-start-events.bpmn");
  }

  /**
   * See test case <a href="https://cawemo.com/share/e6a2eb93-b0e7-4c09-baa0-93ff18084d0e">"Message
   * Events"</a> in cawemo.
   */
  @Test
  void testMessageEvents() throws IOException {
    testGrooveGenerationForBPMNResourceFile("message-events.bpmn");
  }

  /**
   * See test case <a href="https://cawemo.com/share/b4b588fa-3f0f-4c30-95e9-b7f5dd40cd7a">"Message
   * Events without Message Flows"</a> in cawemo.
   */
  @Test
  void testMessageEventsNoMessageFlows() throws IOException {
    testGrooveGenerationForBPMNResourceFile("message-events-without-message-flows.bpmn");
  }

  /**
   * See test case <a href="https://cawemo.com/share/44d74e7b-f940-48cd-8ceb-d23976b4da2b">"Two
   * Incoming Message Flows"</a> in cawemo.
   */
  @Test
  void testTwoIncomingMessageFlows() throws IOException {
    // TODO: Implement the terminating example which supersedes this one.
    testGrooveGenerationForBPMNResourceFile("two-incoming-message-flows-no-termination.bpmn");
  }

  /**
   * See test case <a href="https://cawemo.com/share/e1777355-d0cc-45d0-8f01-87d08ba2b5ef">"Two End
   * Events"</a> in cawemo.
   */
  @Test
  void testTwoEndEvents() throws IOException {
    testGrooveGenerationForBPMNResourceFile("two-end-events.bpmn");
  }

  /**
   * See test case <a
   * href="https://cawemo.com/share/e579995b-65f3-4146-a974-f136f5fd949b">"Terminate End Event"</a>
   * in cawemo.
   */
  @Test
  void testTerminateEndEvent() throws IOException {
    testGrooveGenerationForBPMNResourceFile("terminate-end-event.bpmn");
  }

  /**
   * See test case <a href="https://cawemo.com/share/519f49aa-e3ec-4d6d-8425-3933f93f974d">"Link
   * Event"</a> in cawemo.
   */
  @Test
  void testLinkEvent() throws IOException {
    testGrooveGenerationForBPMNResourceFile("link-event.bpmn");
  }

  /**
   * See test case <a href="https://cawemo.com/share/e13f777e-dca2-45e9-8018-0b9d0c4b34cc">"Signal
   * Events"</a> in cawemo.
   */
  @Test
  void testSignalEvents() throws IOException {
    testGrooveGenerationForBPMNResourceFile("signal-events.bpmn");
  }

  /**
   * See test case <a href="https://cawemo.com/share/6a29e7e5-bf10-4b3e-bb40-2ff8591f7e0c">"Signal
   * Events - Multi Activation"</a> in cawemo.
   */
  @Test
  void testSignalEventsMultiActivation() throws IOException {
    testGrooveGenerationForBPMNResourceFile("signal-events-multi-activation.bpmn");
  }

  /**
   * See test case <a href="https://cawemo.com/share/813dee70-ddc2-4a71-a965-1b6a2d28c7fa">"Signal
   * events - Multi Activation - Same Process"</a> in cawemo.
   */
  @Test
  void testSignalEventsMultiActivationSameProcess() throws IOException {
    testGrooveGenerationForBPMNResourceFile("signal-events-multi-activation-same-process.bpmn");
  }

  /**
   * See test case <a href="https://cawemo.com/share/15e29a24-e35b-40b8-b09a-b63f3bc91268">"Timer
   * events"</a> in cawemo.
   */
  @Test
  void testTimerEvents() throws IOException {
    testGrooveGenerationForBPMNResourceFile("timer-events.bpmn");
  }

  /**
   * See test case <a
   * href="https://cawemo.com/share/0b3cb831-a6b2-4e7c-b064-0c83e887bf47">"Intermediate Throw
   * Event"</a> in cawemo.
   */
  @Test
  void testIntermediateThrowEvent() throws IOException {
    testGrooveGenerationForBPMNResourceFile("intermediate-throw-event.bpmn");
  }

  /**
   * See test case <a
   * href="https://cawemo.com/share/8854a051-803d-4acf-b5f9-ffd3e7e984e9">"Subprocess - Error"</a>
   * in cawemo.
   */
  @Test
  void testSubprocessError() throws IOException {
    testGrooveGenerationForBPMNResourceFile("subprocess-error.bpmn");
  }

  /**
   * See test case <a href="https://cawemo.com/share/e10dfa71-5df9-40c5-bd0f-82c391b051e5">"Event
   * Subprocess - Error"</a> in cawemo.
   */
  @Test
  void testEventSubprocessError() throws IOException {
    // TODO: Test interruption correctly! See other interruption tests
    testGrooveGenerationForBPMNResourceFile("event-subprocess-error.bpmn");
  }

  /**
   * See test case <a href="https://cawemo.com/share/2a6dc064-a602-4bda-96d1-a788d9a0e363">
   * "Subprocess Error Catch Ordering"</a> in cawemo.
   */
  @Test
  void testSubprocessErrorCatchOrdering() throws IOException {
    testGrooveGenerationForBPMNResourceFile("subprocess-error-catch-ordering.bpmn");
  }
}
