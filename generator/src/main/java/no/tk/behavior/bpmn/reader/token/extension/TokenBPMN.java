package no.tk.behavior.bpmn.reader.token.extension;

import no.tk.behavior.bpmn.reader.token.extension.instance.ProcessSnapshotImpl;
import no.tk.behavior.bpmn.reader.token.extension.instance.TokenImpl;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.xml.ModelBuilder;

public class TokenBPMN extends Bpmn {

  @Override
  protected void doRegisterTypes(ModelBuilder bpmnModelBuilder) {
    super.doRegisterTypes(bpmnModelBuilder);

    /* Token BPMN extensions */
    ProcessSnapshotImpl.registerType(bpmnModelBuilder);
    TokenImpl.registerType(bpmnModelBuilder);
  }
}
