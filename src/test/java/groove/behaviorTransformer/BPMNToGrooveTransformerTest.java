package groove.behaviorTransformer;

import behavior.bpmn.Activity;
import behavior.bpmn.BPMNProcessModel;
import behavior.bpmn.auxiliary.BPMNProcessBuilder;
import behavior.bpmn.events.*;
import behavior.bpmn.gateways.ExclusiveGateway;
import behavior.bpmn.gateways.InclusiveGateway;
import behavior.bpmn.gateways.ParallelGateway;
import org.junit.jupiter.api.Test;

import java.io.IOException;

class BPMNToGrooveTransformerTest extends BehaviorToGrooveTransformerTestHelper {

    private static final String TYPE_GRAPH_FILE_NAME = "type.gty";
    private static final String TERMINATE_RULE_FILE_NAME = "Terminate.gpr";

    @Override
    protected void setUpFurther() {
        // Default is to ignore the type graph and the terminate rule.
        this.setFileNameFilter(x -> x.equals(TYPE_GRAPH_FILE_NAME) || x.equals(TERMINATE_RULE_FILE_NAME));
    }

    /**
     * See test case <a href="https://cawemo.com/share/e9bca9c5-c750-487f-becf-737bbd6ea19b">"Sequential Activities"</a> in cawemo.
     */
    @Test
    void testSequentialActivities() throws IOException {
        final StartEvent start = new StartEvent("start");
        Activity a = new Activity("A");
        Activity b = new Activity("B");
        final EndEvent end = new EndEvent("end");

        final String modelName = "sequentialActivities";
        final BPMNProcessModel processModel = new BPMNProcessBuilder()
                .name(modelName)
                .startEvent(start)
                .sequenceFlow(start, a)
                .sequenceFlow(a, b)
                .sequenceFlow(b, end)
                .endEvent(end)
                .build();
        // TODO: test prefix
        this.setFileNameFilter(x -> false); // Expect type graph here.
        this.checkGrooveGeneration(processModel);
    }

    /**
     * See test case <a href="https://cawemo.com/share/9f739e59-c250-4f84-96cd-191914b07296">"Exclusive Gateway"</a> in cawemo.
     */
    @Test
    void testExclusiveGateway() throws IOException {
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

        final String modelName = "exclusiveGateway";
        final BPMNProcessModel processModel = new BPMNProcessBuilder()
                .name(modelName)
                .startEvent(start)
                .sequenceFlow(start, a0)
                .sequenceFlow(a0, a1)
                .sequenceFlow(a1, e1)
                .sequenceFlow(e1, a2_1)
                .sequenceFlow(e1, a2_2)
                .sequenceFlow(a2_1, e2)
                .sequenceFlow(a2_2, e2)
                .sequenceFlow(e2, a3)
                .sequenceFlow(a3, end)
                .endEvent(end)
                .build();

        // Exclusive Gateways rules could be optimized, by setting the token position to the exlusive gateway not the individual flow incoming.
        // This leads to only one rule for each outgoing flow not also incoming flow and combinations!
        this.checkGrooveGeneration(processModel);
    }

    /**
     * See test case <a href="https://cawemo.com/share/7ac506cd-86f7-4c89-a946-1ab2b3707d92">"Parallel Gateway"</a> in cawemo.
     */
    @Test
    void testParallelGateway() throws IOException {
        final StartEvent start = new StartEvent("start");
        Activity a0 = new Activity("a0");
        Activity a1 = new Activity("a1");
        final ParallelGateway p1 = new ParallelGateway("p1");
        Activity a2_1 = new Activity("a2_1");
        Activity a2_2 = new Activity("a2_2");
        final ParallelGateway p2 = new ParallelGateway("p2");
        Activity a3 = new Activity("a3");
        final EndEvent end = new EndEvent("end");

        final String modelName = "parallelGateway";
        final BPMNProcessModel processModel = new BPMNProcessBuilder()
                .name(modelName)
                .startEvent(start)
                .sequenceFlow(start, a0)
                .sequenceFlow(a0, a1)
                .sequenceFlow(a1, p1)
                .sequenceFlow(p1, a2_1)
                .sequenceFlow(p1, a2_2)
                .sequenceFlow(a2_1, p2)
                .sequenceFlow(a2_2, p2)
                .sequenceFlow(p2, a3)
                .sequenceFlow(a3, end)
                .endEvent(end)
                .build();

        this.checkGrooveGeneration(processModel);
    }

    /**
     * See test case <a href="https://cawemo.com/share/d0d0439d-31da-4b34-b508-aa75bc2551c8">"Parallel Gateway - Complex"</a> in cawemo.
     */
    @Test
    void testParallelGatewayComplex() throws IOException {
        final StartEvent start = new StartEvent("start");
        Activity a0 = new Activity("a0");
        Activity a1 = new Activity("a1");
        final ParallelGateway p1 = new ParallelGateway("p1");
        Activity a2_1 = new Activity("a2_1");
        Activity a2_2 = new Activity("a2_2");
        final ParallelGateway p2 = new ParallelGateway("p2");
        Activity a3 = new Activity("a3");
        final ParallelGateway p3 = new ParallelGateway("p3");
        final EndEvent end = new EndEvent("end");

        final String modelName = "parallelGateway_complex";
        final BPMNProcessModel processModel = new BPMNProcessBuilder()
                .name(modelName)
                .startEvent(start)
                .sequenceFlow(start, a0)
                .sequenceFlow(a0, a1)
                .sequenceFlow(a1, p1)
                .sequenceFlow(p1, a2_1)
                .sequenceFlow(p1, a2_2)
                .sequenceFlow(a2_2, p2)
                .sequenceFlow(a2_1, p3)
                .sequenceFlow(p2, a3)
                .sequenceFlow(p2, p3)
                .sequenceFlow(a3, p3)
                .sequenceFlow(p3, end)
                .endEvent(end)
                .build();

        this.checkGrooveGeneration(processModel);
    }

    /**
     * See test case <a href="https://cawemo.com/share/9b143426-50ed-4621-83af-b30e29273077">"[CYC]"</a> in cawemo.
     */
    @Test
    void testCyclic() throws IOException {
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
                .sequenceFlow(start, a0)
                .sequenceFlow(a0, e1)
                .sequenceFlow(e1, a1)
                .sequenceFlow(e1, a2)
                .sequenceFlow(a1, e2)
                .sequenceFlow(a2, e3)
                .sequenceFlow(e2, a3)
                .sequenceFlow(a3, e3)
                .sequenceFlow(e3, e4)
                .sequenceFlow(e4, e2)
                .sequenceFlow(e4, end)
                .endEvent(end)
                .build();

        this.checkGrooveGeneration(processModel);
    }

    /**
     * See test case <a href="https://cawemo.com/share/e1777355-d0cc-45d0-8f01-87d08ba2b5ef">"[EXT]"</a> in cawemo.
     */
    @Test
    void testTwoEndEvents() throws IOException {
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
                .sequenceFlow(start, p1)
                .sequenceFlow(p1, a1)
                .sequenceFlow(p1, a2)
                .sequenceFlow(a1, end1)
                .sequenceFlow(a2, end2)
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

        final String modelName = "linkEvent";
        final BPMNProcessModel processModel = new BPMNProcessBuilder()
                .name(modelName)
                .startEvent(start)
                .sequenceFlow(start, p1)
                .sequenceFlow(p1, throw_link1)
                .sequenceFlow(p1, throw_link2)
                .sequenceFlow(catch_link1, p2)
                .sequenceFlow(catch_link2, p2)
                .sequenceFlow(p2, end)
                .endEvent(end)
                .build();

        this.checkGrooveGeneration(processModel);
    }

    /**
     * See test case <a href="https://cawemo.com/share/e579995b-65f3-4146-a974-f136f5fd949b">"Terminate End Event"</a> in cawemo.
     */
    @Test
    void testTerminateEndEvent() throws IOException {
        final StartEvent start = new StartEvent("start");
        final ParallelGateway p1 = new ParallelGateway("p1");
        Activity a = new Activity("A");
        Activity b = new Activity("B");
        final EndEvent end = new EndEvent("end");
        final EndEvent terminate_end = new EndEvent("terminate_end", EndEventType.TERMINATION);

        final String modelName = "terminateEndEvent";
        final BPMNProcessModel processModel = new BPMNProcessBuilder()
                .name(modelName)
                .startEvent(start)
                .sequenceFlow(start, p1)
                .sequenceFlow(p1, a)
                .sequenceFlow(p1, b)
                .sequenceFlow(a, end)
                .sequenceFlow(b, terminate_end)
                .endEvent(end)
                .endEvent(terminate_end)
                .build();

        this.checkGrooveGeneration(processModel);
    }

    /**
     * See test case <a href="https://cawemo.com/share/9fdaa163-2b27-4787-99df-1ecf55971f14">"Implicit exclusive gateway"</a> in cawemo.
     */
    @Test
    void testImplicitExclusiveGateway() throws IOException {
        final StartEvent start = new StartEvent("start");
        final ExclusiveGateway e1 = new ExclusiveGateway("e1");
        Activity a = new Activity("A");
        Activity b = new Activity("B");
        final EndEvent end = new EndEvent("end");

        final String modelName = "implicitExclusiveGateway";
        final BPMNProcessModel processModel = new BPMNProcessBuilder()
                .name(modelName)
                .startEvent(start)
                .sequenceFlow(start, e1)
                .sequenceFlow(e1, a)
                .sequenceFlow(e1, b)
                .sequenceFlow(a, b).sequenceFlow(b, end)
                .endEvent(end)
                .build();

        this.checkGrooveGeneration(processModel);
    }

    /**
     * See test case <a href="https://cawemo.com/share/5e855137-d237-4bf7-bbf4-639c8e6093e0">"Implicit parallel gateway"</a> in cawemo.
     */
    @Test
    void testImplicitParallelGateway() throws IOException {
        final StartEvent start = new StartEvent("start");
        final ParallelGateway p1 = new ParallelGateway("p1");
        Activity a = new Activity("A");
        Activity b = new Activity("B");
        Activity c = new Activity("C");
        final EndEvent end = new EndEvent("end");

        final String modelName = "implicitParallelGateway";
        final BPMNProcessModel processModel = new BPMNProcessBuilder()
                .name(modelName)
                .startEvent(start)
                .sequenceFlow(start, a)
                .sequenceFlow(a, b)
                .sequenceFlow(a, c)
                .sequenceFlow(b, p1)
                .sequenceFlow(c, p1)
                .sequenceFlow(p1, end)
                .endEvent(end)
                .build();

        this.checkGrooveGeneration(processModel);
    }

    /**
     * See test case <a href="https://cawemo.com/share/e5ab5920-be7c-435f-8d58-964760455caf">"Inclusive gateway"</a> in cawemo.
     */
    @Test
    void testInclusiveGateway() throws IOException {
        final StartEvent start = new StartEvent("start");
        final InclusiveGateway i1 = new InclusiveGateway("i1");
        Activity a = new Activity("A");
        Activity b = new Activity("B");
        final InclusiveGateway i2 = new InclusiveGateway("i2");
        final EndEvent end = new EndEvent("end");

        final String modelName = "inclusiveGateway";
        final BPMNProcessModel processModel = new BPMNProcessBuilder()
                .name(modelName)
                .startEvent(start)
                .sequenceFlow(start, i1)
                .sequenceFlow(i1, a)
                .sequenceFlow(i1, b)
                .sequenceFlow(a, i2)
                .sequenceFlow(b, i2)
                .sequenceFlow(i2, end)
                .endEvent(end)
                .build();

        this.checkGrooveGeneration(processModel);
    }

    /**
     * See test case <a href="https://cawemo.com/share/4edc1064-1a2f-46ba-b4bd-9bd3fceea7ae">"Inclusive gateway complex"</a> in cawemo.
     */
    @Test
    void testInclusiveGatewayComplex() throws IOException {
        final StartEvent start = new StartEvent("start");
        final InclusiveGateway i1 = new InclusiveGateway("i1");
        Activity a = new Activity("A");
        final ParallelGateway p1 = new ParallelGateway("p1");
        Activity b = new Activity("B");
        Activity c = new Activity("C");
        final ParallelGateway p2 = new ParallelGateway("p2");
        final InclusiveGateway i2 = new InclusiveGateway("i2");
        final EndEvent end = new EndEvent("end");

        final String modelName = "inclusiveGatewayComplex";
        final BPMNProcessModel processModel = new BPMNProcessBuilder()
                .name(modelName)
                .startEvent(start)
                .sequenceFlow(start, i1)
                .sequenceFlow(i1, a)
                .sequenceFlow(i1, p1)
                .sequenceFlow(a, i2)
                .sequenceFlow(p1, b)
                .sequenceFlow(p1, c)
                .sequenceFlow(b, p2)
                .sequenceFlow(c, p2)
                .sequenceFlow(p2, i2)
                .sequenceFlow(i2, end)
                .endEvent(end)
                .build();

        this.checkGrooveGeneration(processModel);
    }

    @Override
    public String getOutputPathSubFolderName() {
        return "bpmn";
    }
}