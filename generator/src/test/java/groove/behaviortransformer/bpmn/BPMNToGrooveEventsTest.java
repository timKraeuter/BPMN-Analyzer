package groove.behaviortransformer.bpmn;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import behavior.bpmn.auxiliary.exceptions.GrooveGenerationRuntimeException;
import java.io.IOException;
import org.junit.jupiter.api.Assertions;
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
    testGrooveGenerationForBPMNResourceFile("event-subprocess-error.bpmn");
  }

  /**
   * See test case <a
   * href="https://cawemo.com/share/ca354679-b2a9-4deb-93f9-47069658e48f">"Subprocess Error Handling
   * Complex"</a> in cawemo.
   */
  @Test
  void testSubprocessErrorHandlingComplex() throws IOException {
    testGrooveGenerationForBPMNResourceFile("subprocess-error-handling-complex.bpmn");
  }

  /**
   * See test case <a href="https://cawemo.com/share/2a6dc064-a602-4bda-96d1-a788d9a0e363">
   * "Subprocess Error Unclear Catch Event"</a> in cawemo.
   */
  @Test
  void testSubprocessErrorUnclearCatchEvent() {
    GrooveGenerationRuntimeException exception =
        Assertions.assertThrows(
            GrooveGenerationRuntimeException.class,
            () ->
                testGrooveGenerationForBPMNResourceFile(
                    "subprocess-error-unclear-catch-event.bpmn"));
    assertThat(
        exception.getMessage(),
        is(
            "There were multiple matching error catch events "
                + "\"[catch_error_esub (Event_0050cj9), catch_error (Event_1yjmjxx)]\" "
                + "for the error end event \"throw_error (Event_1qghjqc)\"!"));
  }

  /**
   * See test case <a href="https://cawemo.com/share/fc49a2d8-60f3-409a-9225-715b2f682f90">"No Error
   * Catch Event"</a> in cawemo.
   */
  @Test
  void testNoErrorCatchEvent() {
    GrooveGenerationRuntimeException exception =
        Assertions.assertThrows(
            GrooveGenerationRuntimeException.class,
            () -> testGrooveGenerationForBPMNResourceFile("no-error-catch-event.bpmn"));
    assertThat(
        exception.getMessage(),
        is("No matching error catch event found for \"error 1 (Event_1lfavsd)\"!"));
  }

  /**
   * See test case <a
   * href="https://cawemo.com/share/5b367115-13ea-41f5-8c08-5c1453c090fc">"Subprocess - Escalation"</a>
   * in cawemo.
   */
  @Test
  void testSubprocessEscalation() throws IOException {
    testGrooveGenerationForBPMNResourceFile("subprocess-escalation.bpmn");
  }

  /**
   * See test case <a href="https://cawemo.com/share/27f3844c-87a7-46cb-a18f-6183d4e0340f">"Event
   * Subprocess - Escalation"</a> in cawemo.
   */
  @Test
  void testEventSubprocessEscalation() throws IOException {
    testGrooveGenerationForBPMNResourceFile("event-subprocess-escalation.bpmn");
  }

  /**
   * See test case <a
   * href="https://cawemo.com/share/1512a5dd-45f4-4209-9e24-efcfd5dddc8a">"Subprocess Escalation Handling
   * Complex"</a> in cawemo.
   */
  @Test
  void testSubprocessEscalationHandlingComplex() throws IOException {
    testGrooveGenerationForBPMNResourceFile("subprocess-escalation-handling-complex.bpmn");
  }

  /**
   * See test case <a href="https://cawemo.com/share/2f2a2535-bb4a-4290-aef8-3877b3808030">
   * "Subprocess Escalation Unclear Catch Event"</a> in cawemo.
   */
  @Test
  void testSubprocessEscalationUnclearCatchEvent() {
    GrooveGenerationRuntimeException exception =
        Assertions.assertThrows(
            GrooveGenerationRuntimeException.class,
            () ->
                testGrooveGenerationForBPMNResourceFile(
                    "subprocess-escalation-unclear-catch-event.bpmn"));
    assertThat(
        exception.getMessage(),
        is(
            "There were multiple matching escalation catch events "
                + "\"[catch_error_esub (Event_0050cj9), catch_error (Event_1yjmjxx)]\" "
                + "for the error end event \"throw_error (Event_1qghjqc)\"!"));
  }

  /**
   * See test case <a href="https://cawemo.com/share/120ed7b8-70e6-4eb2-8b84-b2f74e4f52dd">"No
   * Escalation Catch Event"</a> in cawemo.
   */
  @Test
  void testNoEscalationCatchEvent() {
    GrooveGenerationRuntimeException exception =
        Assertions.assertThrows(
            GrooveGenerationRuntimeException.class,
            () -> testGrooveGenerationForBPMNResourceFile("no-error-catch-event.bpmn"));
    assertThat(
        exception.getMessage(),
        is("No matching error catch event found for \"error 1 (Event_1lfavsd)\"!"));
  }
}
