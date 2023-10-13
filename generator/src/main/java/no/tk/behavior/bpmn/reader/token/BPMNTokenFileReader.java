package no.tk.behavior.bpmn.reader.token;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.function.UnaryOperator;
import no.tk.behavior.bpmn.SequenceFlow;
import no.tk.behavior.bpmn.reader.token.extension.BPMNToken;
import no.tk.behavior.bpmn.reader.token.extension.instance.BTProcessSnapshot;
import no.tk.behavior.bpmn.reader.token.extension.instance.BTToken;
import no.tk.behavior.bpmn.reader.token.model.CollaborationSnapshot;
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

  private final UnaryOperator<String> elementNameTransformer;

  public BPMNTokenFileReader() {
    this(x -> x);
  }

  public BPMNTokenFileReader(UnaryOperator<String> elementNameTransformer) {
    this.elementNameTransformer = elementNameTransformer;
  }

  public CollaborationSnapshot readModelFromString(String name, String xml) {
    // TODO: Should be a better way than wrapping the string.
    return readModelFromStream(
        name, new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
  }

  public CollaborationSnapshot readModelFromFilePath(Path file) throws IOException {
    String modelName = FilenameUtils.removeExtension(file.getFileName().toString());
    return readModelFromStream(modelName, Files.newInputStream(file));
  }

  public CollaborationSnapshot readModelFromStream(String modelName, InputStream stream) {
    BpmnModelInstance bpmnModelInstance = Bpmn.readModelFromStream(stream);
    return readBPMNProcessSnapshot(modelName, bpmnModelInstance);
  }

  private CollaborationSnapshot readBPMNProcessSnapshot(
      String modelName, BpmnModelInstance bpmnModelInstance) {
    CollaborationSnapshot collaborationSnapshot = new CollaborationSnapshot(modelName);

    Collection<ModelElementInstance> snapshots = getAllSnapshots(bpmnModelInstance);

    if (hasNoBPMNCollaboration(bpmnModelInstance)) {
      saveUnconnectedSnapshots(snapshots, collaborationSnapshot);
    }

    Collection<ModelElementInstance> tokens = getAllTokens(bpmnModelInstance);
    Collection<ModelElementInstance> associations = getAllAssociations(bpmnModelInstance);

    for (ModelElementInstance association : associations) {
      followAssociationToSaveTokenOrSnapshot(
          bpmnModelInstance, association, collaborationSnapshot, tokens, snapshots);
    }

    return collaborationSnapshot;
  }

  private void saveUnconnectedSnapshots(
      Collection<ModelElementInstance> snapshots, CollaborationSnapshot collaborationSnapshot) {
    snapshots.forEach(
        snapshot -> saveProcessSnapshot(collaborationSnapshot, (BTProcessSnapshot) snapshot, null));
  }

  private Collection<ModelElementInstance> getAllSnapshots(BpmnModelInstance bpmnModelInstance) {
    // Could be indexed once for faster retrieval later.
    return getAllElementsByClassType(bpmnModelInstance, BTProcessSnapshot.class);
  }

  private Collection<ModelElementInstance> getAllAssociations(BpmnModelInstance bpmnModelInstance) {
    return getAllElementsByClassType(bpmnModelInstance, Association.class);
  }

  private Collection<ModelElementInstance> getAllTokens(BpmnModelInstance bpmnModelInstance) {
    // Could be indexed once for faster retrieval later.
    return getAllElementsByClassType(bpmnModelInstance, BTToken.class);
  }

  private Collection<ModelElementInstance> getAllElementsByClassType(
      BpmnModelInstance bpmnModelInstance, Class<? extends ModelElementInstance> classType) {
    ModelElementType type = bpmnModelInstance.getModel().getType(classType);
    return bpmnModelInstance.getModelElementsByType(type);
  }

  private boolean hasNoBPMNCollaboration(BpmnModelInstance bpmnModelInstance) {
    Collection<ModelElementInstance> collaborations =
        getAllElementsByClassType(bpmnModelInstance, Collaboration.class);
    return collaborations.isEmpty();
  }

  private void followAssociationToSaveTokenOrSnapshot(
      BpmnModelInstance bpmnModelInstance,
      ModelElementInstance association,
      CollaborationSnapshot collaborationSnapshot,
      Collection<ModelElementInstance> tokens,
      Collection<ModelElementInstance> snapshots) {
    String targetRef = association.getAttributeValue("targetRef");
    if (targetRef.startsWith("ProcessSnapshot")) {
      saveProcessSnapshot(
          collaborationSnapshot, bpmnModelInstance, association, snapshots, targetRef);
    }
    if (targetRef.startsWith("Token")) {
      saveToken(bpmnModelInstance, association, collaborationSnapshot, targetRef, tokens);
    }
  }

  private void saveToken(
      BpmnModelInstance bpmnModelInstance,
      ModelElementInstance association,
      CollaborationSnapshot collaborationSnapshot,
      String targetRef,
      Collection<ModelElementInstance> tokens) {
    BTToken token = (BTToken) getTokenOrSnapshotWithID(targetRef, tokens);
    String tokenID = getTokenID(bpmnModelInstance, association);
    collaborationSnapshot.addToken(
        token.processSnapshotID(), new Token(tokenID, token.shouldExist()));
  }

  private String getTokenID(BpmnModelInstance bpmnModelInstance, ModelElementInstance association) {
    ModelElementInstance tokenPosition =
        bpmnModelInstance.getModelElementById(association.getAttributeValue("sourceRef"));
    if (tokenPosition instanceof org.camunda.bpm.model.bpmn.instance.SequenceFlow sfPosition) {
      return getSequenceFlowDescriptiveName(sfPosition);
    }
    return getFlowNodePosition(tokenPosition);
  }

  private String getFlowNodePosition(ModelElementInstance tokenPosition) {
    return String.format(
        "%s (%s)", getNameOrID(tokenPosition), tokenPosition.getAttributeValue("id"));
  }

  private String getSequenceFlowDescriptiveName(
      org.camunda.bpm.model.bpmn.instance.SequenceFlow sequenceFlow) {
    String name = sequenceFlow.getAttributeValue("name");
    if (name == null) {
      name = "";
    }
    return SequenceFlow.getDescriptiveName(
        name,
        sequenceFlow.getId(),
        getNameOrID(sequenceFlow.getSource()),
        getNameOrID(sequenceFlow.getTarget()));
  }

  private String getNameOrID(ModelElementInstance element) {
    String name = element.getAttributeValue("name");
    if (name == null) {
      return element.getAttributeValue("id");
    }
    return elementNameTransformer.apply(name);
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
      CollaborationSnapshot collaborationSnapshot,
      BpmnModelInstance bpmnModelInstance,
      ModelElementInstance association,
      Collection<ModelElementInstance> snapshots,
      String snapshotID) {

    String processName = getProcessNameOrIDForSnapshot(bpmnModelInstance, association);
    BTProcessSnapshot snapshot =
        (BTProcessSnapshot) getTokenOrSnapshotWithID(snapshotID, snapshots);
    saveProcessSnapshot(collaborationSnapshot, snapshot, processName);
  }

  private void saveProcessSnapshot(
      CollaborationSnapshot collaborationSnapshot,
      BTProcessSnapshot snapshot,
      String snapshotName) {
    collaborationSnapshot.addProcessSnapshot(
        new ProcessSnapshot(snapshotName, snapshot.getId(), snapshot.shouldExist()));
  }

  private String getProcessNameOrIDForSnapshot(
      BpmnModelInstance bpmnModelInstance, ModelElementInstance association) {
    String participantID = association.getAttributeValue("sourceRef");
    ModelElementInstance participant = bpmnModelInstance.getModelElementById(participantID);
    return getNameOrID(participant);
  }
}
