package ecmf;

import behavior.bpmn.BPMNCollaboration;
import behavior.bpmn.auxiliary.BPMNCollaborationBuilder;
import behavior.bpmn.events.EndEvent;
import behavior.bpmn.events.StartEvent;
import groove.behaviorTransformer.bpmn.BPMNToGrooveTestBase;

import java.io.IOException;

public class ECMFA extends BPMNToGrooveTestBase {


    /**
     * See test case <a href="">"Use case"</a> in cawemo.
     */
//    @Test
    void testCallActivity() throws IOException {
        final StartEvent start = new StartEvent("start");
        // TODO: Add ECMF stuff here
        final EndEvent end = new EndEvent("end");

        // TODO: Implement signal events

        final String modelName = "usecase";
        final BPMNCollaboration collaboration = new BPMNCollaborationBuilder()
                .name(modelName)
                .processName(modelName)
                .startEvent(start)
                .sequenceFlow(start, end)
                .build();

        this.checkGrooveGeneration(collaboration);
    }
}
