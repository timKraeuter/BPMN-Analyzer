package no.tk.behavior.bpmn.reader.token.extension;

import no.tk.behavior.bpmn.reader.token.extension.instance.BTProcessSnapshotImpl;
import no.tk.behavior.bpmn.reader.token.extension.instance.BTTokenImpl;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.xml.ModelBuilder;

public class TokenBPMN extends Bpmn {

  @Override
  protected void doRegisterTypes(ModelBuilder bpmnModelBuilder) {
    super.doRegisterTypes(bpmnModelBuilder);

    /* Token BPMN extensions */
    BTProcessSnapshotImpl.registerType(bpmnModelBuilder);
    BTTokenImpl.registerType(bpmnModelBuilder);
  }
}
