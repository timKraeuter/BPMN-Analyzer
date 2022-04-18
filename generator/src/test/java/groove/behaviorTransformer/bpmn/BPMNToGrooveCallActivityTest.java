package groove.behaviorTransformer.bpmn;

import behavior.bpmn.BPMNCollaboration;
import behavior.bpmn.Process;
import behavior.bpmn.activities.CallActivity;
import behavior.bpmn.activities.tasks.Task;
import behavior.bpmn.auxiliary.BPMNCollaborationBuilder;
import behavior.bpmn.events.BoundaryEvent;
import behavior.bpmn.events.BoundaryEventType;
import behavior.bpmn.events.EndEvent;
import behavior.bpmn.events.StartEvent;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class BPMNToGrooveCallActivityTest extends BPMNToGrooveTestBase {


    /**
     * See test case <a href="https://cawemo.com/share/be4afe81-4adf-4b45-a933-5745316ee533">"Call activity - Simple"</a> in cawemo.
     */
    @Test
    void testCallActivity() throws IOException {
        testGrooveGenerationForBPMNResourceFile("call-activity-simple.bpmn");
    }

    /**
     * See test case <a href="https://cawemo.com/share/321db543-e301-4bfd-ac04-e6618346433a">"Call activity - Implicit exclusive and parallel gateway"</a> in cawemo.
     */
    @Test
    void testCallActivityImplicitGateways() throws IOException {
        testGrooveGenerationForBPMNResourceFile("call-activity-implicit-exclusive-and-parallel-gateway.bpmn");
    }

    /**
     * See test case <a href="https://cawemo.com/share/63333b2a-8298-4e9c-8e1e-14453ed1f063">"Call activity - Terminate end event"</a> in cawemo.
     */
    @Test
    void testCallActivityTerminateEvent() throws IOException {
        testGrooveGenerationForBPMNResourceFile("call-activity-terminate-end-event.bpmn");
    }

    /**
     * See test case <a href="https://cawemo.com/share/37764ed1-03e7-43c7-8218-34467a12d104">"Call activity - No start event"</a> in cawemo.
     */
    @Test
    void testCallActivityNoStartEvent() throws IOException {
        testGrooveGenerationForBPMNResourceFile("call-activity-no-start-event.bpmn");
    }

    /**
     * See test case <a href="https://cawemo.com/share/3b573dc4-3592-421d-9b13-dbb4e45ff410">"Call activity - Complex"</a> in cawemo.
     */
    @Test
    void testCallActivityComplex() throws IOException {
        testGrooveGenerationForBPMNResourceFile("call-activity-complex.bpmn");
    }

    /**
     * See test case <a href="https://cawemo.com/share/3b55577a-e7ed-4729-a046-4d79fd11c941">"Interrupting Timer Boundary Events"</a> in cawemo.
     */
    @Test
    void testInterruptingTimerBoundaryEvents() throws IOException {
        final StartEvent start = new StartEvent("start");
        final CallActivity subprocess = new CallActivity(this.buildSimpleSubProcess());
        BoundaryEvent timerBoundaryEvent = new BoundaryEvent("timer", BoundaryEventType.TIMER, true);
        subprocess.attachBoundaryEvent(timerBoundaryEvent);
        final EndEvent end1 = new EndEvent("end1");
        final EndEvent end2 = new EndEvent("end2");

        final String modelName = "timerBoundaryEvent";
        final BPMNCollaboration collaboration = new BPMNCollaborationBuilder()
                .name(modelName)
                .processName(modelName)
                .startEvent(start)
                .sequenceFlow(start, subprocess)
                .sequenceFlow(subprocess, end1)
                .sequenceFlow(timerBoundaryEvent, end2)
                .build();

        this.checkGrooveGeneration(collaboration);
    }

    private Process buildSimpleSubProcess() {
        // TODO: Possible name crashes in rules!
        final StartEvent start = new StartEvent("start_sub");
        final Task a = new Task("Subactivity");
        final EndEvent end = new EndEvent("end_sub");
        return new BPMNCollaborationBuilder()
                .processName("Subprocess")
                .startEvent(start)
                .sequenceFlow(start, a)
                .sequenceFlow(a, end)
                .build()
                .getParticipants()
                .iterator()
                .next();
    }
}
