package no.tk.behavior.bpmn.reader.token;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import no.tk.behavior.bpmn.reader.token.extension.BPMNToken;
import no.tk.behavior.bpmn.reader.token.extension.instance.BTProcessSnapshot;
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

/** Reader for BPMN token files, which silently ignores unconnected snapshots and tokens. */
public class BPMNTokenFileReader {
  static {
    Bpmn.INSTANCE = new BPMNToken();
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
    Collection<ModelElementInstance> snapshots = getAllSnapshots(bpmnModelInstance);

    Collection<ModelElementInstance> associations = getAllAssociations(bpmnModelInstance);

    for (ModelElementInstance association : associations) {
      followAssociationToSaveTokenOrSnapshot(
          bpmnModelInstance, association, bpmnProcessSnapshot, tokens, snapshots);
    }

    return bpmnProcessSnapshot;
  }

  private Collection<ModelElementInstance> getAllSnapshots(BpmnModelInstance bpmnModelInstance) {
    ModelElementType snapshotType = bpmnModelInstance.getModel().getType(BTProcessSnapshot.class);
    // Could be indexed once for faster retrieval later.
    return bpmnModelInstance.getModelElementsByType(snapshotType);
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
      Collection<ModelElementInstance> tokens,
      Collection<ModelElementInstance> snapshots) {
    String targetRef = association.getAttributeValue("targetRef");
    if (targetRef.startsWith("ProcessSnapshot")) {
      saveProcessSnapshot(
          bpmnModelInstance, association, bpmnProcessSnapshot, targetRef, snapshots);
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
    BTToken token = (BTToken) getTokenOrSnapshotWithID(targetRef, tokens);
    bpmnProcessSnapshot.addToken(
        token.processSnapshotID(),
        new Token(association.getAttributeValue("sourceRef"), token.shouldExist()));
  }

  private ModelElementInstance getTokenOrSnapshotWithID(
      String id, Collection<ModelElementInstance> tokensOrSnapshots) {
    // A matching token should always exist.
    return tokensOrSnapshots.stream()
        .filter(t -> t.getAttributeValue("id").equals(id))
        .findFirst()
        .orElseThrow();
  }

  private void saveProcessSnapshot(
      BpmnModelInstance bpmnModelInstance,
      ModelElementInstance association,
      BPMNProcessSnapshot bpmnProcessSnapshot,
      String snapshotID,
      Collection<ModelElementInstance> snapshots) {
    BTProcessSnapshot snapshot =
        (BTProcessSnapshot) getTokenOrSnapshotWithID(snapshotID, snapshots);

    String processID = getProcessIDForSnapshot(bpmnModelInstance, association);
    bpmnProcessSnapshot.addProcessSnapshot(
        new ProcessSnapshot(processID, snapshotID, snapshot.shouldExist()));
  }

  private String getProcessIDForSnapshot(
      BpmnModelInstance bpmnModelInstance, ModelElementInstance association) {
    String participantID = association.getAttributeValue("sourceRef");
    ModelElementInstance participant = bpmnModelInstance.getModelElementById(participantID);
    return participant.getAttributeValue("processRef");
  }
}
