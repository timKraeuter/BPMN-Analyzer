package groove.behaviortransformer.bpmn;

import org.junit.jupiter.api.Test;

class BPMNToGrooveGatewayTest extends BPMNToGrooveTestBase {

  /**
   * See test case <a
   * href="https://cawemo.com/share/9f739e59-c250-4f84-96cd-191914b07296">"Exclusive Gateway"</a> in
   * cawemo.
   */
  @Test
  void testExclusiveGateway() throws Exception {
    // Exclusive Gateways rules could be optimized, by setting the token position to the exclusive
    // gateway not
    // the individual flow incoming.
    // This leads to only one rule for each outgoing flow not also incoming flow and combinations
    // but two rule
    // activations for exclusive gateways.
    // Maybe we can do something with nested rules?
    testGrooveGenerationForBPMNResourceFile("exclusive-gateway.bpmn");
  }

  /**
   * See test case <a href="https://cawemo.com/share/7ac506cd-86f7-4c89-a946-1ab2b3707d92">"Parallel
   * Gateway"</a> in cawemo.
   */
  @Test
  void testParallelGateway() throws Exception {
    testGrooveGenerationForBPMNResourceFile("parallel-gateway.bpmn");
  }

  /**
   * See test case <a href="https://cawemo.com/share/d0d0439d-31da-4b34-b508-aa75bc2551c8">"Parallel
   * Gateway - Complex"</a> in cawemo.
   */
  @Test
  void testParallelGatewayComplex() throws Exception {
    testGrooveGenerationForBPMNResourceFile("parallel-gateway-complex.bpmn");
  }

  /**
   * See test case <a
   * href="https://cawemo.com/share/e5ab5920-be7c-435f-8d58-964760455caf">"Inclusive Gateway"</a> in
   * cawemo.
   */
  @Test
  void testInclusiveGateway() throws Exception {
    testGrooveGenerationForBPMNResourceFile("inclusive-gateway.bpmn");
  }

  /**
   * See test case <a
   * href="https://cawemo.com/share/4edc1064-1a2f-46ba-b4bd-9bd3fceea7ae">"Inclusive Gateway -
   * Complex"</a> in cawemo.
   */
  @Test
  void testInclusiveGatewayComplex() throws Exception {
    testGrooveGenerationForBPMNResourceFile("inclusive-gateway-complex.bpmn");
  }

  /**
   * See test case <a
   * href="https://cawemo.com/share/c16c4923-dfa0-4a15-ade3-b47acb40ad66">"Exclusive Event Based
   * Gateway"</a> in cawemo.
   */
  @Test
  void testExclusiveEventBasedGateway() throws Exception {
    testGrooveGenerationForBPMNResourceFile("exclusive-event-based-gateway.bpmn");
  }

  /**
   * See test case <a
   * href="https://cawemo.com/share/6db8059f-911b-4d2b-a8b3-83efb99ceed2">"Exclusive Event Based
   * Gateway - Instantiate"</a> in cawemo.
   */
  @Test
  void testExclusiveEventBasedGatewayInstantiate() throws Exception {
    testGrooveGenerationForBPMNResourceFile("exclusive-event-based-gateway-instantiate.bpmn");
  }
}
