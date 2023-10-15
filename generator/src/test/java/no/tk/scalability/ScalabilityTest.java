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
}
