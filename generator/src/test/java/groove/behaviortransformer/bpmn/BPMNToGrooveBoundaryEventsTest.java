package groove.behaviortransformer.bpmn;

import org.junit.jupiter.api.Test;

class BPMNToGrooveBoundaryEventsTest extends BPMNToGrooveTestBase {

  /**
   * See test case <a
   * href="https://cawemo.com/share/3b55577a-e7ed-4729-a046-4d79fd11c941">"Subprocess - Interrupting
   * Boundary Events"</a> in cawemo.
   */
  @Test
  void testSubProcessInterruptingBoundaryEvents() throws Exception {
    testGrooveGenerationForBPMNResourceFile("subprocess-interrupting-boundary-events.bpmn");
  }

  /**
   * See test case <a
   * href="https://cawemo.com/share/656b7c63-34e3-404f-9399-bbed9e22a8b7">"Subprocess -
   * Non-Interrupting Boundary Events"</a> in cawemo.
   */
  @Test
  void testSubProcessNonInterruptingBoundaryEvents() throws Exception {
    testGrooveGenerationForBPMNResourceFile("subprocess-non-interrupting-boundary-events.bpmn");
  }

  /**
   * See test case <a href="https://cawemo.com/share/9acd9a23-65d7-46cd-bad5-b5b874333567">"Task -
   * Interrupting Boundary Events"</a> in cawemo.
   */
  @Test
  void testTaskInterruptingBoundaryEvents() throws Exception {
    testGrooveGenerationForBPMNResourceFile("task-interrupting-boundary-events.bpmn");
  }

  /**
   * See test case <a href="https://cawemo.com/share/8a322a8a-95ff-4773-aeb7-fba9635eefbb">"Task -
   * Non-Interrupting Boundary Events"</a> in cawemo.
   */
  @Test
  void testTaskNonInterruptingBoundaryEvents() throws Exception {
    testGrooveGenerationForBPMNResourceFile("task-non-interrupting-boundary-events.bpmn");
  }
}
