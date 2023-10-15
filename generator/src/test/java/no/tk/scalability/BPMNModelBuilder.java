package no.tk.scalability;

import java.util.concurrent.atomic.AtomicLong;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.builder.AbstractFlowNodeBuilder;

public class BPMNModelBuilder {

  private AbstractFlowNodeBuilder flowNodeBuilder;
  private final AtomicLong idSequencer = new AtomicLong(0);

  public static BpmnModelInstance createModelWithXBlocks(long numberOfBlocks) {
    BPMNModelBuilder bpmnModelBuilder = new BPMNModelBuilder();
    while (numberOfBlocks > 0) {
      bpmnModelBuilder.block1();
      numberOfBlocks--;
      if (numberOfBlocks <= 0) {
        break;
      }
      bpmnModelBuilder.block2();
      numberOfBlocks--;
      if (numberOfBlocks <= 0) {
        break;
      }
      bpmnModelBuilder.block3();
      numberOfBlocks--;
    }

    return bpmnModelBuilder.build();
  }

  public BPMNModelBuilder() {
    flowNodeBuilder = Bpmn.createProcess().startEvent(getNextId());
  }

  /** Block 1 adds three tasks to the process. */
  public BPMNModelBuilder block1() {
    String task1 = getNextId();
    String task2 = getNextId();
    String task3 = getNextId();
    flowNodeBuilder = flowNodeBuilder.serviceTask(task1).serviceTask(task2).serviceTask(task3);
    return this;
  }

  /** Block 2 adds two exclusive gateways and two tasks as a block to the process. */
  public BPMNModelBuilder block2() {
    String gateway1 = getNextId();
    String task1 = getNextId();
    String task2 = getNextId();
    String gateway2 = getNextId();

    flowNodeBuilder =
        flowNodeBuilder
            .exclusiveGateway(gateway1)
            .serviceTask(task1)
            .moveToLastGateway()
            .serviceTask(task2)
            .moveToNode(task1)
            .exclusiveGateway(gateway2)
            .moveToNode(task2)
            .connectTo(gateway2);
    return this;
  }

  private String getNextId() {
    return String.format("FlowNode_%s", idSequencer.getAndIncrement());
  }

  /** Block 3 adds two parallel gateways and two tasks as a block to the process. */
  public BPMNModelBuilder block3() {
    String gateway1 = getNextId();
    String task1 = getNextId();
    String task2 = getNextId();
    String gateway2 = getNextId();

    flowNodeBuilder =
        flowNodeBuilder
            .parallelGateway(gateway1)
            .scriptTask(task1)
            .moveToLastGateway()
            .scriptTask(task2)
            .moveToNode(task1)
            .parallelGateway(gateway2)
            .moveToNode(task2)
            .connectTo(gateway2);
    return this;
  }

  public BpmnModelInstance build() {
    return this.flowNodeBuilder.endEvent(getNextId()).done();
  }
}
