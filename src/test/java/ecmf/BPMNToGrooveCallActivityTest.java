package ecmf;

import behavior.bpmn.BPMNCollaboration;
import behavior.bpmn.Process;
import behavior.bpmn.activities.CallActivity;
import behavior.bpmn.activities.tasks.Task;
import behavior.bpmn.auxiliary.BPMNCollaborationBuilder;
import behavior.bpmn.events.EndEvent;
import behavior.bpmn.events.EndEventType;
import behavior.bpmn.events.StartEvent;
import behavior.bpmn.gateways.ExclusiveGateway;
import behavior.bpmn.gateways.ParallelGateway;
import groove.behaviorTransformer.bpmn.BPMNToGrooveTestBase;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class BPMNToGrooveCallActivityTest extends BPMNToGrooveTestBase {


    /**
     * See test case <a href="https://cawemo.com/share/598c5678-1f50-49a3-8d30-abe22ecedc43">"Call activity"</a> in cawemo.
     */
    @Test
    void testCallActivity() throws IOException {
        final StartEvent start = new StartEvent("start");
        final EndEvent end = new EndEvent("end");

        // TODO: Add ECMF stuff here
        // TODO: Implement signal events

        final String modelName = "callActivity";
        final BPMNCollaboration collaboration = new BPMNCollaborationBuilder()
                .name(modelName)
                .processName(modelName)
                .startEvent(start)
                .sequenceFlow(start, end)
                .build();

        this.checkGrooveGeneration(collaboration);
    }
}
