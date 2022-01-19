package groove.behaviorTransformer.bpmn;

import behavior.bpmn.BPMNCollaboration;
import behavior.bpmn.activities.tasks.ReceiveTask;
import behavior.bpmn.activities.tasks.SendTask;
import behavior.bpmn.activities.tasks.Task;
import behavior.bpmn.auxiliary.BPMNCollaborationBuilder;
import behavior.bpmn.events.*;
import behavior.bpmn.gateways.ExclusiveGateway;
import behavior.bpmn.gateways.ParallelGateway;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class BPMNToGrooveTaskTest extends BPMNToGrooveTestBase {

    /**
     * See test case <a href="https://cawemo.com/share/e9bca9c5-c750-487f-becf-737bbd6ea19b">"Sequential Activities"</a> in cawemo.
     */
    @Test
    void testSequentialActivities() throws IOException {
        final StartEvent start = new StartEvent("start");
        Task a = new Task("A");
        Task b = new Task("B");
        final EndEvent end = new EndEvent("end");

        final String modelName = "sequentialActivities";
        final BPMNCollaboration collaboration = new BPMNCollaborationBuilder()
                .name(modelName)
                .processName(modelName)
                .startEvent(start)
                .sequenceFlow(start, a)
                .sequenceFlow(a, b)
                .sequenceFlow(b, end)
                .build();
        // TODO: test prefix
        this.setFileNameFilter(x -> false); // Expect type graph here.
        this.checkGrooveGeneration(collaboration);
    }

    /**
     * See test case <a href="https://cawemo.com/share/9fdaa163-2b27-4787-99df-1ecf55971f14">"Implicit exclusive gateway"</a> in cawemo.
     */
    @Test
    void testImplicitExclusiveGateway() throws IOException {
        final StartEvent start = new StartEvent("start");
        final ExclusiveGateway e1 = new ExclusiveGateway("e1");
        Task a = new Task("A");
        Task b = new Task("B");
        final EndEvent end = new EndEvent("end");

        final String modelName = "implicitExclusiveGateway";
        final BPMNCollaboration collaboration = new BPMNCollaborationBuilder()
                .name(modelName)
                .processName(modelName)
                .startEvent(start)
                .sequenceFlow(start, e1)
                .sequenceFlow(e1, a)
                .sequenceFlow(e1, b)
                .sequenceFlow(a, b).sequenceFlow(b, end)
                .build();

        this.checkGrooveGeneration(collaboration);
    }

    /**
     * See test case <a href="https://cawemo.com/share/5e855137-d237-4bf7-bbf4-639c8e6093e0">"Implicit parallel gateway"</a> in cawemo.
     */
    @Test
    void testImplicitParallelGateway() throws IOException {
        final StartEvent start = new StartEvent("start");
        final ParallelGateway p1 = new ParallelGateway("p1");
        Task a = new Task("A");
        Task b = new Task("B");
        Task c = new Task("C");
        final EndEvent end = new EndEvent("end");

        final String modelName = "implicitParallelGateway";
        final BPMNCollaboration collaboration = new BPMNCollaborationBuilder()
                .name(modelName)
                .processName(modelName)
                .startEvent(start)
                .sequenceFlow(start, a)
                .sequenceFlow(a, b)
                .sequenceFlow(a, c)
                .sequenceFlow(b, p1)
                .sequenceFlow(c, p1)
                .sequenceFlow(p1, end)
                .build();

        this.checkGrooveGeneration(collaboration);
    }

    /**
     * See test case <a href="https://cawemo.com/share/121dafdb-2ce5-4146-8f4e-315ab9bb0c38">"Send/Receive Message Tasks"</a> in cawemo.
     */
    @Test
    void testSendReceiveTask() throws IOException {
        final StartEvent start_p1 = new StartEvent("start_p1");
        SendTask tSend_1 = new SendTask("TSend_1");
        SendTask tSend_2 = new SendTask("TSend_2");
        ReceiveTask tReceive_1 = new ReceiveTask("TReceive_1");
        final EndEvent end_p1 = new EndEvent("end_p1");

        final StartEvent start_p2 = new StartEvent("start_p2");
        ReceiveTask tReceive_2 = new ReceiveTask("TReceive_2");
        IntermediateCatchEvent eReceive_1 = new IntermediateCatchEvent("EReceive_1", IntermediateEventType.MESSAGE);
        final EndEvent end_p2 = new EndEvent("end_p2", EndEventType.MESSAGE);


        final String modelName = "sendReceiveTask";
        final BPMNCollaboration collaboration = new BPMNCollaborationBuilder()
                .name(modelName)
                .messageFlow(tSend_1, tReceive_2)
                .messageFlow(tSend_2, eReceive_1)
                .messageFlow(end_p2, tReceive_1)
                .processName("p1")
                .startEvent(start_p1)
                .sequenceFlow(start_p1, tSend_1)
                .sequenceFlow(tSend_1, tSend_2)
                .sequenceFlow(tSend_2, tReceive_1)
                .sequenceFlow(tReceive_1, end_p1)
                .buildProcess()
                .processName("p2")
                .startEvent(start_p2)
                .sequenceFlow(start_p2, tReceive_2)
                .sequenceFlow(tReceive_2, eReceive_1)
                .sequenceFlow(eReceive_1, end_p2)
                .build();

        this.checkGrooveGeneration(collaboration);
    }
}
