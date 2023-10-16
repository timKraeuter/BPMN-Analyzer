package no.tk.scalability;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.common.base.Joiner;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
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
  void print() {
    List<String> models =
        IntStream.rangeClosed(1, 300)
            .mapToObj(i -> String.format("grammars/%03d.gps", i))
            .collect(Collectors.toList());
    System.out.print(Joiner.on(",").join(models));
  }

  //  @Test
  void printStats() throws IOException {
    BPMNStatPrinter.printStats(Path.of("C:/Source/scalability/"));
  }
}
