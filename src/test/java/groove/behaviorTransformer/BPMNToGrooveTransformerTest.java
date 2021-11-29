package groove.behaviorTransformer;

import behavior.bpmn.*;
import behavior.bpmn.auxiliary.BPMNProcessBuilder;
import groove.graph.GrooveNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

class BPMNToGrooveTransformerTest implements BehaviorToGrooveTransformerTestHelper {
    
    @BeforeEach
    void setUp() {
        GrooveNode.idCounter.set(-1);
    }

    /**
     * See model [SEQ] in bpmn_models/models.png
     */
    @Test
    void testBPMNTwoActivityGenerationResources() throws IOException {
        // Build the process model from the NWPT example.
        final StartEvent start = new StartEvent("start");
        Activity a0 = new Activity("a0");
        Activity a1 = new Activity("a1");
        final EndEvent end = new EndEvent("end");

        final String modelName = "twoActivity";
        final BPMNProcessModel processModel = new BPMNProcessBuilder()
                .name(modelName)
                .startEvent(start)
                .sequenceFlow("start", start, a0)
                .sequenceFlow("a0", a0, a1)
                .sequenceFlow("a1", a1, end)
                .endEvent(end)
                .build();

        this.checkGrooveGeneration(modelName, processModel);
    }

    /**
     * See model in bpmn_models/exclusive_parallel_BPMN.pdf
     */
    @Test
    void testBPMNExclusiveGatewayGenerationResources() throws IOException {
        // Build the process model from the NWPT example.
        final StartEvent start = new StartEvent("start");
        final EndEvent end = new EndEvent("end");
        Activity a0 = new Activity("a0");
        Activity a1 = new Activity("a1");
        final ExclusiveGateway e1 = new ExclusiveGateway("e1");
        Activity a2_1 = new Activity("a2_1");
        Activity a2_2 = new Activity("a2_2");
        final ExclusiveGateway e2 = new ExclusiveGateway("e2");
        Activity a3 = new Activity("a3");

        final String modelName = "exclusive";
        final BPMNProcessModel processModel = new BPMNProcessBuilder()
                .name(modelName)
                .startEvent(start)
                .sequenceFlow("start", start, a0)
                .sequenceFlow("a0", a0, a1)
                .sequenceFlow("a1", a1, e1)
                .sequenceFlow("e1_a2_1", e1, a2_1)
                .sequenceFlow("e1_a2_2", e1, a2_2)
                .sequenceFlow("a2_1", a2_1, e2)
                .sequenceFlow("a2_2", a2_2, e2)
                .sequenceFlow("e2", e2, a3)
                .sequenceFlow("a3", a3, end)
                .endEvent(end)
                .build();

        this.checkGrooveGeneration(modelName, processModel);
    }

    /**
     * See model in bpmn_models/exclusive_parallel_BPMN.pdf
     */
    @Test
    void testBPMNParallelGatewayGenerationResources() throws IOException {
        final StartEvent start = new StartEvent("start");
        final EndEvent end = new EndEvent("end");
        Activity a0 = new Activity("a0");
        Activity a1 = new Activity("a1");
        final ParallelGateway p1 = new ParallelGateway("p1");
        Activity a2_1 = new Activity("a2_1");
        Activity a2_2 = new Activity("a2_2");
        final ParallelGateway p2 = new ParallelGateway("p2");
        Activity a3 = new Activity("a3");

        final String modelName = "parallel";
        final BPMNProcessModel processModel = new BPMNProcessBuilder()
                .name(modelName)
                .startEvent(start)
                .sequenceFlow("start", start, a0)
                .sequenceFlow("a0", a0, a1)
                .sequenceFlow("a1", a1, p1)
                .sequenceFlow("p1_a2_1", p1, a2_1)
                .sequenceFlow("p1_a2_2", p1, a2_2)
                .sequenceFlow("a2_1", a2_1, p2)
                .sequenceFlow("a2_2", a2_2, p2)
                .sequenceFlow("p2", p2, a3)
                .sequenceFlow("a3", a3, end)
                .endEvent(end)
                .build();

        this.checkGrooveGeneration(modelName, processModel);
    }

    /**
     * See model [CYC] in bpmn_models/models.png (without data).
     */
    @Test
    void testBPMNCyclicGenerationResources() throws IOException {
        final StartEvent start = new StartEvent("start");
        final EndEvent end = new EndEvent("end");
        Activity a0 = new Activity("a0");
        final ExclusiveGateway e1 = new ExclusiveGateway("e1");
        Activity a1 = new Activity("a1");
        Activity a2 = new Activity("a2");
        Activity a3 = new Activity("a3");
        final ExclusiveGateway e2 = new ExclusiveGateway("e2");
        final ExclusiveGateway e3 = new ExclusiveGateway("e3");
        final ExclusiveGateway e4 = new ExclusiveGateway("e4");

        final String modelName = "cyclic";
        final BPMNProcessModel processModel = new BPMNProcessBuilder()
                .name(modelName)
                .startEvent(start)
                .sequenceFlow("start", start, a0)
                .sequenceFlow("a0", a0, e1)
                .sequenceFlow("e1_a1", e1, a1)
                .sequenceFlow("e1_a2", e1, a2)
                .sequenceFlow("a1", a1, e2)
                .sequenceFlow("a2", a2, e3)
                .sequenceFlow("e2", e2, a3)
                .sequenceFlow("a3", a3, e3)
                .sequenceFlow("e3", e3, e4)
                .sequenceFlow("e4_e2", e4, e2)
                .sequenceFlow("e4_end", e4, end)
                .endEvent(end)
                .build();

        this.checkGrooveGeneration(modelName, processModel);
    }

    /**
     * See model [EXT] in bpmn_models/models.png.
     */
    @Test
    void testBPMNTwoEndEventsGenerationResources() throws IOException {
        final StartEvent start = new StartEvent("start");
        final ParallelGateway p1 = new ParallelGateway("p1");
        Activity a1 = new Activity("a1");
        Activity a2 = new Activity("a2");
        final EndEvent end1 = new EndEvent("end1");
        final EndEvent end2 = new EndEvent("end2");

        final String modelName = "twoEndEvents";
        final BPMNProcessModel processModel = new BPMNProcessBuilder()
                .name(modelName)
                .startEvent(start)
                .sequenceFlow("start", start, p1)
                .sequenceFlow("p1", p1, a1)
                .sequenceFlow("p1", p1, a2)
                .sequenceFlow("a1", a1, end1)
                .sequenceFlow("a2", a2, end2)
                .endEvent(end1)
                .endEvent(end2)
                .build();

        this.checkGrooveGeneration(modelName, processModel);
    }
}