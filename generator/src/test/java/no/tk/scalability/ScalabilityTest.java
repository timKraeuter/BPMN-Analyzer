package no.tk.scalability;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Path;
import java.util.stream.IntStream;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.FlowElement;
import org.junit.jupiter.api.Test;

class ScalabilityTest {

  @Test
  void block1Test() {
    BpmnModelInstance modelInstance = new BPMNModelBuilder().block1().buildWithEndEvent();

    assertThat(modelInstance.getModelElementsByType(FlowElement.class).size(), is(9));
  }

  @Test
  void block2Test() {
    BpmnModelInstance modelInstance = new BPMNModelBuilder().block2().buildWithEndEvent();

    assertThat(modelInstance.getModelElementsByType(FlowElement.class).size(), is(12));
  }

  @Test
  void block3Test() {
    BpmnModelInstance modelInstance = new BPMNModelBuilder().block3().buildWithEndEvent();

    assertThat(modelInstance.getModelElementsByType(FlowElement.class).size(), is(12));
  }

  @Test
  void blockChainingTest() {
    BpmnModelInstance modelInstance =
        new BPMNModelBuilder().block1().block2().block3().buildWithEndEvent();

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
  void upToNumberOfBlocksTest() {
    BPMNModelBuilder.createModelsWithUpToXBlocks(300);
    assertTrue(true);
  }

  //  @Test
  void printStats() throws IOException {
    BPMNStatPrinter.printStats(Path.of("C:/Source/scalability/"));
  }

  @Test
  void parallelismDegreesTest() {
    ParallelismDegreeGenerator parallelismDegreeGenerator = new ParallelismDegreeGenerator();
    IntStream.rangeClosed(1, 30).forEach(parallelismDegreeGenerator::generateParallelModel);
  }
}
