package maude.behaviortransformer.bpmn;

import maude.behaviortransformer.BPMNMaudeTestHelper;
import org.junit.jupiter.api.Test;

import java.io.IOException;

class BPMNToMaudeTaskTest implements BPMNMaudeTestHelper {

    /**
     * See test case <a href="https://cawemo.com/share/e9bca9c5-c750-487f-becf-737bbd6ea19b">"Sequential Tasks"</a>
     * in cawemo.
     */
    @Test
    void testSequentialTasks() throws IOException {
        testBPMNMaudeGeneration("sequential-activities");
    }

    /**
     * See test case <a href="https://cawemo.com/share/9fdaa163-2b27-4787-99df-1ecf55971f14">"Implicit Exclusive Gateway"</a> in cawemo.
     */
    @Test
    void testImplicitExclusiveGateway() throws IOException {
        testBPMNMaudeGeneration("implicit-exclusive-gateway");
    }

    /**
     * See test case <a href="https://cawemo.com/share/5e855137-d237-4bf7-bbf4-639c8e6093e0">"Implicit Parallel Gateway"</a> in cawemo.
     */
    @Test
    void testImplicitParallelGateway() throws IOException {
        testBPMNMaudeGeneration("implicit-parallel-gateway");
    }
}
