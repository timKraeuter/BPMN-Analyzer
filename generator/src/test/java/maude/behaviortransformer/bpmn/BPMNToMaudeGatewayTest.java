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
        testBPMNMaudeGeneration("exclusive-gateway");
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

    /**
     * See test case
     * <a href="https://cawemo.com/share/c16c4923-dfa0-4a15-ade3-b47acb40ad66">"Exclusive Event Based Gateway"</a>
     * in cawemo.
     */
    @Test
    void testExclusiveEventBasedGateway() throws IOException {
        // Does not terminate if the EV-Gateway is too late for the signal event.
        testBPMNMaudeGeneration("exclusive-event-based-gateway", CAN_TERMINATE_QUERY);
    }

    /**
     * See test case
     * <a href="https://cawemo.com/share/6db8059f-911b-4d2b-a8b3-83efb99ceed2">"Exclusive Event Based Gateway - Instantiate"</a>
     * in cawemo.
     */
    @Test
    void testExclusiveEventBasedGatewayInstantiate() throws IOException {
        testBPMNMaudeGeneration("exclusive-event-based-gateway-instantiate");
    }
}
