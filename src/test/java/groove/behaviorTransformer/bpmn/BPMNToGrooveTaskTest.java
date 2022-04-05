package groove.behaviorTransformer.bpmn;

import behavior.bpmn.BPMNCollaboration;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class BPMNToGrooveTaskTest extends BPMNToGrooveTestBase {

    /**
     * See test case <a href="https://cawemo.com/share/e9bca9c5-c750-487f-becf-737bbd6ea19b">"Sequential Activities"</a> in cawemo.
     */
    @Test
    void testSequentialActivities() throws IOException {
        String resourcePath = BPMN_BPMN_MODELS_SEMANTICS_TEST_FOLDER + "sequential-activities.bpmn";
        BPMNCollaboration collaboration = readModelFromResource(resourcePath);
        // TODO: test prefix
        this.setFileNameFilter(x -> false); // Expect type graph here.
        this.checkGrooveGeneration(collaboration);
    }

    /**
     * See test case <a href="https://cawemo.com/share/9fdaa163-2b27-4787-99df-1ecf55971f14">"Implicit exclusive gateway"</a> in cawemo.
     */
    @Test
    void testImplicitExclusiveGateway() throws IOException {
        String resourcePath = BPMN_BPMN_MODELS_SEMANTICS_TEST_FOLDER + "implicit-exclusive-gateway.bpmn";
        BPMNCollaboration collaboration = readModelFromResource(resourcePath);

        this.checkGrooveGeneration(collaboration);
    }

    /**
     * See test case <a href="https://cawemo.com/share/5e855137-d237-4bf7-bbf4-639c8e6093e0">"Implicit parallel gateway"</a> in cawemo.
     */
    @Test
    void testImplicitParallelGateway() throws IOException {
        String resourcePath = BPMN_BPMN_MODELS_SEMANTICS_TEST_FOLDER + "implicit-parallel-gateway.bpmn";
        BPMNCollaboration collaboration = readModelFromResource(resourcePath);

        this.checkGrooveGeneration(collaboration);
    }

    /**
     * See test case <a href="https://cawemo.com/share/121dafdb-2ce5-4146-8f4e-315ab9bb0c38">"Send/Receive Message Tasks"</a> in cawemo.
     */
    @Test
    void testSendReceiveTask() throws IOException {
        String resourcePath = BPMN_BPMN_MODELS_SEMANTICS_TEST_FOLDER + "send-receive-message-tasks.bpmn";
        BPMNCollaboration collaboration = readModelFromResource(resourcePath);

        this.checkGrooveGeneration(collaboration);
    }

    /**
     * See test case <a href="https://cawemo.com/share/e76c1763-4842-493c-bebb-cfa41e5abb09">"Instantiate Receive Task"</a> in cawemo.
     */
    @Test
    void testInstantiateReceiveTask() throws IOException {
        String resourcePath = BPMN_BPMN_MODELS_SEMANTICS_TEST_FOLDER + "instantiate-receive-task.bpmn";
        BPMNCollaboration collaboration = readModelFromResource(resourcePath);

        this.checkGrooveGeneration(collaboration);
    }
}
