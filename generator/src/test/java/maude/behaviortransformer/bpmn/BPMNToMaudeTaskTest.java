package maude.behaviortransformer.bpmn;

import java.io.IOException;
import org.junit.jupiter.api.Test;

class BPMNToMaudeTaskTest implements BPMNMaudeTestHelper {

  /**
   * See test case <a
   * href="https://cawemo.com/share/e9bca9c5-c750-487f-becf-737bbd6ea19b">"Sequential Tasks"</a> in
   * cawemo.
   */
  @Test
  void testSequentialTasks() throws IOException {
    testBPMNMaudeGeneration("sequential-tasks");
  }

  /**
   * See test case <a href="https://cawemo.com/share/9fdaa163-2b27-4787-99df-1ecf55971f14">"Implicit
   * Exclusive Gateway"</a> in cawemo.
   */
  @Test
  void testImplicitExclusiveGateway() throws IOException {
    testBPMNMaudeGeneration("implicit-exclusive-gateway");
  }

  /**
   * See test case <a href="https://cawemo.com/share/5e855137-d237-4bf7-bbf4-639c8e6093e0">"Implicit
   * Parallel Gateway"</a> in cawemo.
   */
  @Test
  void testImplicitParallelGateway() throws IOException {
    testBPMNMaudeGeneration("implicit-parallel-gateway");
  }

  /**
   * See test case <a
   * href="https://cawemo.com/share/121dafdb-2ce5-4146-8f4e-315ab9bb0c38">"Send/Receive Message
   * Tasks"</a> in cawemo.
   */
  @Test
  void testSendReceiveTask() throws IOException {
    testBPMNMaudeGeneration("send-receive-message-tasks");
  }

  /**
   * See test case <a href="https://cawemo.com/share/27ac3d00-1a27-4b53-b2d0-2b9a18c2e5d7">"Two
   * Incoming Message flows Receive Task"</a> in cawemo.
   */
  @Test
  void testTwoIncomingFlowsReceiveTask() throws IOException {
    testBPMNMaudeGeneration("two-incoming-message-flows-receive-task");
  }

  /**
   * See test case <a
   * href="https://cawemo.com/share/e76c1763-4842-493c-bebb-cfa41e5abb09">"Instantiate Receive
   * Task"</a> in cawemo.
   */
  @Test
  void testInstantiateReceiveTask() throws IOException {
    testBPMNMaudeGeneration("instantiate-receive-task");
  }
}
