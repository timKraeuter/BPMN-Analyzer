package no.tk.behavior.bpmn.reader.token;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import no.tk.behavior.bpmn.reader.token.extension.TokenBPMN;
import no.tk.behavior.bpmn.reader.token.extension.instance.BTToken;
import no.tk.behavior.bpmn.reader.token.model.BPMNProcessSnapshot;
import no.tk.behavior.bpmn.reader.token.model.ProcessSnapshot;
import no.tk.behavior.bpmn.reader.token.model.Token;
import org.apache.commons.io.FilenameUtils;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.*;
import org.camunda.bpm.model.xml.instance.ModelElementInstance;
import org.camunda.bpm.model.xml.type.ModelElementType;

public class TokenBPMNFileReader {
  static {
    Bpmn.INSTANCE = new TokenBPMN();
  }

  public BPMNProcessSnapshot readModelFromFilePath(Path file) throws IOException {
    String modelName = FilenameUtils.removeExtension(file.getFileName().toString());
    return readModelFromStream(modelName, Files.newInputStream(file));
  }

  public BPMNProcessSnapshot readModelFromStream(String modelName, InputStream stream) {
    BpmnModelInstance bpmnModelInstance = Bpmn.readModelFromStream(stream);
    return readBPMNProcessSnapshot(modelName, bpmnModelInstance);
  }

  private BPMNProcessSnapshot readBPMNProcessSnapshot(
      String name, BpmnModelInstance bpmnModelInstance) {
    BPMNProcessSnapshot bpmnProcessSnapshot = new BPMNProcessSnapshot(name);

    Collection<ModelElementInstance> tokens = getAllTokens(bpmnModelInstance);

    Collection<ModelElementInstance> associations = getAllAssociations(bpmnModelInstance);

    for (ModelElementInstance association : associations) {
      followAssociationToSaveTokenOrSnapshot(
          bpmnModelInstance, association, bpmnProcessSnapshot, tokens);
    }

    return bpmnProcessSnapshot;
  }

  private Collection<ModelElementInstance> getAllAssociations(BpmnModelInstance bpmnModelInstance) {
    ModelElementType associationType = bpmnModelInstance.getModel().getType(Association.class);
    return bpmnModelInstance.getModelElementsByType(associationType);
  }

  private Collection<ModelElementInstance> getAllTokens(BpmnModelInstance bpmnModelInstance) {
    ModelElementType tokenType = bpmnModelInstance.getModel().getType(BTToken.class);
    // Could be indexed once for faster retrieval later.
    return bpmnModelInstance.getModelElementsByType(tokenType);
  }

  private void followAssociationToSaveTokenOrSnapshot(
      BpmnModelInstance bpmnModelInstance,
      ModelElementInstance association,
      BPMNProcessSnapshot bpmnProcessSnapshot,
      Collection<ModelElementInstance> tokens) {
    String targetRef = association.getAttributeValue("targetRef");
    if (targetRef.startsWith("ProcessSnapshot")) {
      saveProcessSnapshot(bpmnModelInstance, association, bpmnProcessSnapshot, targetRef);
    }
    if (targetRef.startsWith("Token")) {
      saveToken(association, bpmnProcessSnapshot, targetRef, tokens);
    }
  }

  private void saveToken(
      ModelElementInstance association,
      BPMNProcessSnapshot bpmnProcessSnapshot,
      String targetRef,
      Collection<ModelElementInstance> tokens) {
    BTToken token = getTokenForTargetRef(targetRef, tokens);
    bpmnProcessSnapshot.addToken(
        token.processSnapshotID(),
        new Token(association.getAttributeValue("sourceRef"), token.shouldExist()));
  }

  private BTToken getTokenForTargetRef(String targetRef, Collection<ModelElementInstance> tokens) {
    // A matching token should always exist.
    return (BTToken)
        tokens.stream()
            .filter(t -> t.getAttributeValue("id").equals(targetRef))
            .findFirst()
            .orElseThrow();
  }

  private void saveProcessSnapshot(
      BpmnModelInstance bpmnModelInstance,
      ModelElementInstance association,
      BPMNProcessSnapshot bpmnProcessSnapshot,
      String snapshotID) {
    String participantID = association.getAttributeValue("sourceRef");
    ModelElementInstance participant = bpmnModelInstance.getModelElementById(participantID);
    String processID = participant.getAttributeValue("processRef");
    bpmnProcessSnapshot.addProcessSnapshot(new ProcessSnapshot(processID, snapshotID));
  }
}
