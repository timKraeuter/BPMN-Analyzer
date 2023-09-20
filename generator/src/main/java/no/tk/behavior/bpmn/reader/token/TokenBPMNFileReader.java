package no.tk.behavior.bpmn.reader.token;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import no.tk.behavior.bpmn.reader.token.extension.TokenBPMN;
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

  private Function<String, String> elementNameTransformer;

  public TokenBPMNFileReader() {}

  public TokenBPMNFileReader(UnaryOperator<String> elementNameTransformer) {
    this.elementNameTransformer = elementNameTransformer;
  }

  public BPMNProcessSnapshot readModelFromFilePath(Path file) throws IOException {
    String modelName = FilenameUtils.removeExtension(file.getFileName().toString());
    return readModelFromStream(modelName, Files.newInputStream(file));
  }

  public BPMNProcessSnapshot readModelFromStream(InputStream stream) {
    return readModelFromStream("model", stream);
  }

  public BPMNProcessSnapshot readModelFromStream(String modelName, InputStream stream) {
    BpmnModelInstance bpmnModelInstance = Bpmn.readModelFromStream(stream);
    return readBPMNProcessSnapshot(modelName, bpmnModelInstance);
  }

  private BPMNProcessSnapshot readBPMNProcessSnapshot(
      String name, BpmnModelInstance bpmnModelInstance) {
    BPMNProcessSnapshot bpmnProcessSnapshot = new BPMNProcessSnapshot(name);
    ModelElementType tokenType =
        bpmnModelInstance
            .getModel()
            .getType(no.tk.behavior.bpmn.reader.token.extension.instance.Token.class);
    // Could be indexed once but not needed for this scale.
    Collection<ModelElementInstance> tokens = bpmnModelInstance.getModelElementsByType(tokenType);

    ModelElementType associationType = bpmnModelInstance.getModel().getType(Association.class);
    Collection<ModelElementInstance> associations =
        bpmnModelInstance.getModelElementsByType(associationType);

    for (ModelElementInstance association : associations) {
      String targetRef = association.getAttributeValue("targetRef");
      if (targetRef.startsWith("ProcessSnapshot")) {
        saveProcessSnapshot(bpmnModelInstance, association, bpmnProcessSnapshot, targetRef);
      }
      if (targetRef.startsWith("Token")) {
        saveToken(association, bpmnProcessSnapshot, targetRef, tokens);
      }
    }

    return bpmnProcessSnapshot;
  }

  private void saveToken(
      ModelElementInstance association,
      BPMNProcessSnapshot bpmnProcessSnapshot,
      String targetRef,
      Collection<ModelElementInstance> tokens) {
    ModelElementInstance token = getTokenForTargetRef(targetRef, tokens);
    String pSnapshotID = token.getAttributeValue("processSnapshot");
    bpmnProcessSnapshot.addToken(
        pSnapshotID, new Token(association.getAttributeValue("sourceRef"), readShouldExist(token)));
  }

  private boolean readShouldExist(ModelElementInstance token) {
    no.tk.behavior.bpmn.reader.token.extension.instance.Token t =
        (no.tk.behavior.bpmn.reader.token.extension.instance.Token) token;
    return t.shouldExist();
  }

  private static ModelElementInstance getTokenForTargetRef(
      String targetRef, Collection<ModelElementInstance> tokens) {
    // A matching token should always exist.
    return tokens.stream()
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
