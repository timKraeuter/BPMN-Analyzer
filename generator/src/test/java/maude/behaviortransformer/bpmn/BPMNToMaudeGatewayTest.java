package maude.behaviortransformer.bpmn;

import maude.behaviortransformer.BPMNMaudeTestHelper;
import org.junit.jupiter.api.Test;

import java.io.IOException;

class BPMNToMaudeGatewayTest implements BPMNMaudeTestHelper {

    /**
     * See test case <a href="https://cawemo.com/share/9f739e59-c250-4f84-96cd-191914b07296">"Exclusive Gateway"</a>
     * in cawemo.
     */
    @Test
    void testExclusiveGateway() throws IOException {
    }

    /**
     * See test case <a href="https://cawemo.com/share/7ac506cd-86f7-4c89-a946-1ab2b3707d92">"Parallel Gateway"</a>
     * in cawemo.
     */
    @Test
    void testParallelGateway() throws IOException {
        testBPMNMaudeGeneration("parallel-gateway");
    }

    /**
     * See test case
     * <a href="https://cawemo.com/share/d0d0439d-31da-4b34-b508-aa75bc2551c8">"Parallel Gateway - Complex"</a>
     * in cawemo.
     */
    @Test
    void testParallelGatewayComplex() throws IOException {
        testBPMNMaudeGeneration("parallel-gateway-complex");
    }
}
