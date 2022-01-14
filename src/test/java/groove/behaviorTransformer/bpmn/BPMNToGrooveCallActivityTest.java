package groove.behaviorTransformer.bpmn;

import behavior.bpmn.BPMNProcessModel;
import behavior.bpmn.CallActivity;
import behavior.bpmn.Task;
import behavior.bpmn.auxiliary.BPMNProcessBuilder;
import behavior.bpmn.events.EndEvent;
import behavior.bpmn.events.EndEventType;
import behavior.bpmn.events.StartEvent;
import behavior.bpmn.gateways.ExclusiveGateway;
import behavior.bpmn.gateways.ParallelGateway;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class BPMNToGrooveCallActivityTest extends BPMNToGrooveTest {


    /**
     * See test case <a href="https://cawemo.com/share/598c5678-1f50-49a3-8d30-abe22ecedc43">"Call activity"</a> in cawemo.
     */
    @Test
    void testCallActivity() throws IOException {
        final StartEvent start = new StartEvent("start");
        final Task a = new Task("A");
        final CallActivity subprocess = new CallActivity(this.buildSimpleSubProcess());
        Task b = new Task("B");
        final EndEvent end = new EndEvent("end");

        final String modelName = "callActivity";
        final BPMNProcessModel processModel = new BPMNProcessBuilder()
                .name(modelName)
                .startEvent(start)
                .sequenceFlow(start, a)
                .sequenceFlow(a, subprocess)
                .sequenceFlow(subprocess, b)
                .sequenceFlow(b, end)
                .endEvent(end)
                .build();

        this.checkGrooveGeneration(processModel);
    }

    /**
     * See test case <a href="https://cawemo.com/share/598c5678-1f50-49a3-8d30-abe22ecedc43">"Call activity"</a> in cawemo.
     */
    @Test
    void testCallActivityImplicitGateways() throws IOException {
        final StartEvent start = new StartEvent("start");
        final Task a = new Task("A");
        final CallActivity subprocess = new CallActivity(this.buildSimpleSubProcess());
        Task b = new Task("B");
        final EndEvent end = new EndEvent("end");

        final String modelName = "callActivityImplicitGateways";
        final BPMNProcessModel processModel = new BPMNProcessBuilder()
                .name(modelName)
                .startEvent(start)
                .sequenceFlow(start, a)
                .sequenceFlow("a1", a, subprocess)
                .sequenceFlow("a2", a, subprocess)
                .sequenceFlow("a3", subprocess, b)
                .sequenceFlow("a4", subprocess, b)
                .sequenceFlow(b, end)
                .endEvent(end)
                .build();

        this.checkGrooveGeneration(processModel);
    }

    /**
     * See test case <a href="https://cawemo.com/share/598c5678-1f50-49a3-8d30-abe22ecedc43">"Call activity"</a> in cawemo.
     */
    @Test
    void testCallActivityTerminateEvent() throws IOException {
        final StartEvent start = new StartEvent("start");
        ParallelGateway p1 = new ParallelGateway("p1");
        final CallActivity subprocess = new CallActivity(this.buildSimpleSubProcess());
        final EndEvent end = new EndEvent("end");
        final EndEvent terminate_end = new EndEvent("terminate_end", EndEventType.TERMINATION);

        final String modelName = "callActivityTerminateEvent";
        final BPMNProcessModel processModel = new BPMNProcessBuilder()
                .name(modelName)
                .startEvent(start)
                .sequenceFlow(start, p1)
                .sequenceFlow(p1, subprocess)
                .sequenceFlow(p1, terminate_end)
                .sequenceFlow(subprocess, end)
                .endEvent(end)
                .endEvent(terminate_end)
                .build();

        this.checkGrooveGeneration(processModel);
    }

    private BPMNProcessModel buildSimpleSubProcess() {
        // TODO: Possible name crashes in rules!
        final StartEvent start = new StartEvent("start_sub");
        final Task a = new Task("Subactivity");
        final EndEvent end = new EndEvent("end_sub");
        return new BPMNProcessBuilder()
                .name("Subprocess")
                .startEvent(start)
                .sequenceFlow(start, a)
                .sequenceFlow(a, end)
                .endEvent(end)
                .build();
    }

    /**
     * See test case <a href="https://cawemo.com/share/598c5678-1f50-49a3-8d30-abe22ecedc43">"Call activity"</a> in cawemo.
     */
    @Test
    void testCallActivityComplex() throws IOException {
        final StartEvent start = new StartEvent("start");
        final Task a = new Task("A");
        final CallActivity subprocess = new CallActivity(this.buildComplexSubProcess());
        Task b = new Task("B");
        final EndEvent end = new EndEvent("end");

        final String modelName = "callActivityComplex";
        final BPMNProcessModel processModel = new BPMNProcessBuilder()
                .name(modelName)
                .startEvent(start)
                .sequenceFlow(start, a)
                .sequenceFlow(a, subprocess)
                .sequenceFlow(subprocess, b)
                .sequenceFlow(b, end)
                .endEvent(end)
                .build();

        this.checkGrooveGeneration(processModel);
    }

    private BPMNProcessModel buildComplexSubProcess() {
        // TODO: Possible name crashes in rules!
        final StartEvent start = new StartEvent("start_sub");
        ExclusiveGateway e1 = new ExclusiveGateway("e1");
        final Task c = new Task("C");
        final Task d = new Task("D");
        ParallelGateway p1 = new ParallelGateway("p1");
        final Task e = new Task("E");
        final Task f = new Task("F");
        ParallelGateway p2 = new ParallelGateway("p2");
        ExclusiveGateway e2 = new ExclusiveGateway("e2");
        final EndEvent end = new EndEvent("end_sub");
        return new BPMNProcessBuilder()
                .name("ComplexSub")
                .startEvent(start)
                .sequenceFlow(start, e1)
                .sequenceFlow(e1, c)
                .sequenceFlow(c, e2)
                .sequenceFlow(e1, d)
                .sequenceFlow(d, p1)
                .sequenceFlow(p1, e)
                .sequenceFlow(p1, f)
                .sequenceFlow(e, p2)
                .sequenceFlow(f, p2)
                .sequenceFlow(p2, e2)
                .sequenceFlow(e2, end)
                .endEvent(end)
                .build();
    }
}
