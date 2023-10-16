package no.tk.scalability;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.builder.AbstractFlowNodeBuilder;
import org.camunda.bpm.model.bpmn.instance.FlowNode;

public class BPMNModelBuilder {

  private AbstractFlowNodeBuilder flowNodeBuilder;
  private final AtomicLong idSequencer = new AtomicLong(0);

  public static void createModelsWithUpToXBlocks(long numberOfBlocks) {
    try (ForkJoinPool forkJoinPool = ForkJoinPool.commonPool()) {
      BPMNModelBuilder bpmnModelBuilder = new BPMNModelBuilder();
      int blocks = 0;
      while (blocks <= numberOfBlocks) {
        bpmnModelBuilder.block1();
        blocks++;
        addEndEventAndSaveInstance(bpmnModelBuilder, blocks, forkJoinPool);
        if (blocks >= numberOfBlocks) {
          break;
        }
        bpmnModelBuilder.block2();
        blocks++;
        addEndEventAndSaveInstance(bpmnModelBuilder, blocks, forkJoinPool);
        if (blocks >= numberOfBlocks) {
          break;
        }
        bpmnModelBuilder.block3();
        blocks++;
        addEndEventAndSaveInstance(bpmnModelBuilder, blocks, forkJoinPool);
      }
      assertTrue(forkJoinPool.awaitQuiescence(24, TimeUnit.HOURS));
    }
  }

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

    return bpmnModelBuilder.buildWithEndEvent();
  }

  private static void addEndEventAndSaveInstance(
      BPMNModelBuilder bpmnModelBuilder, int blocks, ForkJoinPool forkJoinPool) {
    BpmnModelInstance clone = bpmnModelBuilder.build().clone();
    String currentId = bpmnModelBuilder.getCurrentId();
    forkJoinPool.execute(
        () -> {
          FlowNode lastElement = clone.getModelElementById(currentId);
          BpmnModelInstance instanceWithEndEvent = lastElement.builder().endEvent().done();
          File file = new File(String.format("C:\\Source\\scalability/%s.bpmn", blocks));
          Bpmn.writeModelToFile(file, instanceWithEndEvent);
        });
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
            .moveToNode(gateway1)
            .serviceTask(task2)
            .moveToNode(task1)
            .exclusiveGateway(gateway2)
            .moveToNode(task2)
            .connectTo(gateway2);
    return this;
  }

  private String getNextId() {
    return String.format("FlowNode_%s", idSequencer.incrementAndGet());
  }

  private String getCurrentId() {
    return String.format("FlowNode_%s", idSequencer.get());
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
            .moveToNode(gateway1)
            .scriptTask(task2)
            .moveToNode(task1)
            .parallelGateway(gateway2)
            .moveToNode(task2)
            .connectTo(gateway2);
    return this;
  }

  public BpmnModelInstance buildWithEndEvent() {
    return this.flowNodeBuilder.endEvent().done();
  }

  public BpmnModelInstance build() {
    return this.flowNodeBuilder.done();
  }
}
