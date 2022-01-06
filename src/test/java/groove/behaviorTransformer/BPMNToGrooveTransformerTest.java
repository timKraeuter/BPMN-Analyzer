package groove.behaviorTransformer;

import behavior.bpmn.Activity;
import behavior.bpmn.BPMNProcessModel;
import behavior.bpmn.auxiliary.BPMNProcessBuilder;
import behavior.bpmn.events.EndEvent;
import behavior.bpmn.events.LinkEvent;
import behavior.bpmn.events.LinkEventType;
import behavior.bpmn.events.StartEvent;
import behavior.bpmn.gateways.ExclusiveGateway;
import behavior.bpmn.gateways.ParallelGateway;
import org.junit.jupiter.api.Test;

import java.io.IOException;

class BPMNToGrooveTransformerTest extends BehaviorToGrooveTransformerTestHelper {

    private static final String TYPE_GRAPH_FILE_NAME = "type.gty";

    @Override
    protected void setUpFurther() {
        // Default is to ignore the type graph.
        this.setFileNameFilter(x -> x.equals(TYPE_GRAPH_FILE_NAME));
    }

    /**
     * See model [SEQ] in bpmn_models/models.png
     */
    @Test
    void testBPMNTwoActivityGenerationResources() throws IOException {
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
        // TODO: test prefix
        this.setFileNameFilter(x -> false); // Expect type graph here.
        this.checkGrooveGeneration(processModel);
    }

    /**
     * See model in bpmn_models/exclusive_parallel_BPMN.pdf
     */
    @Test
    void testBPMNExclusiveGateway() throws IOException {
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

        this.checkGrooveGeneration(processModel);
    }

    /**
     * See model in bpmn_models/exclusive_parallel_BPMN.pdf
     */
    @Test
    void testBPMNParallelGateway() throws IOException {
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

        this.checkGrooveGeneration(processModel);
    }

    /**
     * See model in bpmn_models/exclusive_parallel_BPMN.pdf
     */
    @Test
    void testBPMNParallelGatewayComplex() throws IOException {
        final StartEvent start = new StartEvent("start");
        final EndEvent end = new EndEvent("end");
        Activity a0 = new Activity("a0");
        Activity a1 = new Activity("a1");
        final ParallelGateway p1 = new ParallelGateway("p1");
        Activity a2_1 = new Activity("a2_1");
        Activity a2_2 = new Activity("a2_2");
        final ParallelGateway p2 = new ParallelGateway("p2");
        Activity a3 = new Activity("a3");
        final ParallelGateway p3 = new ParallelGateway("p3");

        final String modelName = "parallel_complex";
        final BPMNProcessModel processModel = new BPMNProcessBuilder()
                .name(modelName)
                .startEvent(start)
                .sequenceFlow("start", start, a0)
                .sequenceFlow("a0", a0, a1)
                .sequenceFlow("a1", a1, p1)
                .sequenceFlow("p1_a2_1", p1, a2_1)
                .sequenceFlow("p1_a2_2", p1, a2_2)
                .sequenceFlow("a2_2", a2_2, p2)
                .sequenceFlow("a2_1", a2_1, p3)
                .sequenceFlow("p2", p2, a3)
                .sequenceFlow("p2", p2, p3)
                .sequenceFlow("a3", a3, p3)
                .sequenceFlow("p3", p3, end)
                .endEvent(end)
                .build();

        this.checkGrooveGeneration(processModel);
    }

    /**
     * See model [CYC] in bpmn_models/models.png (without data).
     */
    @Test
    void testBPMNCyclic() throws IOException {
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

        this.checkGrooveGeneration(processModel);
    }

    /**
     * See model [EXT] in bpmn_models/models.png.
     */
    @Test
    void testBPMNTwoEndEvents() throws IOException {
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

        this.checkGrooveGeneration(processModel);
    }

    /**
     * See test case <a href="https://cawemo.com/share/519f49aa-e3ec-4d6d-8425-3933f93f974d">"Link Event"</a> in cawemo.
     */
    @Test
    void testLinkEvent() throws IOException {
        final StartEvent start = new StartEvent("start");
        final ParallelGateway p1 = new ParallelGateway("p1");
        LinkEvent throw_link1 = new LinkEvent("Link1", LinkEventType.THROW);
        LinkEvent throw_link2 = new LinkEvent("Link2", LinkEventType.THROW);
        LinkEvent catch_link1 = new LinkEvent("Link1", LinkEventType.CATCH);
        LinkEvent catch_link2 = new LinkEvent("Link2", LinkEventType.CATCH);
        final ParallelGateway p2 = new ParallelGateway("p2");
        final EndEvent end = new EndEvent("end");

        final String modelName = "LinkEvent";
        final BPMNProcessModel processModel = new BPMNProcessBuilder()
                .name(modelName)
                .startEvent(start)
                .sequenceFlow("start", start, p1)
                .sequenceFlow("p1_link1", p1, throw_link1)
                .sequenceFlow("p1_link2", p1, throw_link2)
                .sequenceFlow("catch_link1", catch_link1, p2)
                .sequenceFlow("catch_link2", catch_link2, p2)
                .sequenceFlow("p2", p2, end)
                .endEvent(end)
                .build();

        this.checkGrooveGeneration(processModel);
    }

    @Override
    public String getOutputPathSubFolderName() {
        return "bpmn";
    }
}