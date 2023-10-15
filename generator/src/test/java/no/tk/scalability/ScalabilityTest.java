package no.tk.scalability;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.File;
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
    Bpmn.writeModelToFile(new File("C:/Source/block2.bpmn"), modelInstance);
  }

  @Test
  void block3Test() {
    BpmnModelInstance modelInstance = new BPMNModelBuilder().block3().build();

    assertThat(modelInstance.getModelElementsByType(FlowElement.class).size(), is(12));
  }

  @Test
  void blockChainingTest() {
    BpmnModelInstance modelInstance =
        new BPMNModelBuilder()
            .block1()
            .block2()
            .block3()
            .build();

    assertThat(modelInstance.getModelElementsByType(FlowElement.class).size(), is(27));
  }

  @Test
  void numberOfBlocksTest() {
    BpmnModelInstance modelInstance1 = BPMNModelBuilder.createModelWithXBlocks(3);
    assertThat(modelInstance1.getModelElementsByType(FlowElement.class).size(), is(27));

    BpmnModelInstance modelInstance2 = BPMNModelBuilder.createModelWithXBlocks(10);
    assertThat(modelInstance2.getModelElementsByType(FlowElement.class).size(), is(81));
  }

  @Test
  void generateScalabilityInputModels() {
    for(int i = 0; i <= 300; i++) {
      BpmnModelInstance instance = BPMNModelBuilder.createModelWithXBlocks(i);
      File file = new File(String.format("C:\\Source\\scalability/%s.bpmn", i));
      Bpmn.writeModelToFile(file, instance);
    }
    assertThat(true,is(true));
  }
}
