package no.tk.scalability;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.apache.commons.lang3.ObjectUtils.Null;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.FlowElement;
import org.junit.jupiter.api.Test;

class ScalabilityTest {

  @Test
  void block1Test() {
    BpmnModelInstance modelInstance = new BPMNModelBuilder().block1().build();

    assertThat(modelInstance.getModelElementsByType(FlowElement.class).size(), is(9));
  }

  @Test
  void block2Test() {
    BpmnModelInstance modelInstance = new BPMNModelBuilder().block2().build();

    assertThat(modelInstance.getModelElementsByType(FlowElement.class).size(), is(12));
  }

  @Test
  void block3Test() {
    BpmnModelInstance modelInstance = new BPMNModelBuilder().block3().build();

    assertThat(modelInstance.getModelElementsByType(FlowElement.class).size(), is(12));
  }

  @Test
  void blockChainingTest() {
    BpmnModelInstance modelInstance = new BPMNModelBuilder().block1().block2().block3().build();

    assertThat(modelInstance.getModelElementsByType(FlowElement.class).size(), is(27));
  }

  @Test
  void numberOfBlocksTest() {
    BpmnModelInstance modelInstance1 = BPMNModelBuilder.createModelWithXBlocks(3);
    assertThat(modelInstance1.getModelElementsByType(FlowElement.class).size(), is(27));

    BpmnModelInstance modelInstance2 = BPMNModelBuilder.createModelWithXBlocks(10);
    assertThat(modelInstance2.getModelElementsByType(FlowElement.class).size(), is(81));
  }

  //  @Test
  void generateScalabilityInputModels() throws Exception {
    try (ForkJoinPool forkJoinPool = ForkJoinPool.commonPool()) {
      Set<Callable<Null>> tasks =
          IntStream.rangeClosed(0, 10)
              .mapToObj(
                  i ->
                      (Callable<Null>)
                          () -> {
                            BpmnModelInstance instance = BPMNModelBuilder.createModelWithXBlocks(i);
                            File file =
                                new File(String.format("C:\\Source\\scalability/%s.bpmn", i));
                            Bpmn.writeModelToFile(file, instance);
                            return null;
                          })
              .collect(Collectors.toSet());
      forkJoinPool.invokeAll(tasks);
      assertTrue(forkJoinPool.awaitQuiescence(24, TimeUnit.HOURS));
    }
  }
}
