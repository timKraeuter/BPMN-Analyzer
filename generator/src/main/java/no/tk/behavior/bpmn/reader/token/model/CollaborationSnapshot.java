package no.tk.behavior.bpmn.reader.token.model;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A collaboration snapshot represents a set of process snapshots containing tokens for one BPMN
 * collaboration.
 */
public class CollaborationSnapshot {

  private final String name;
  private final Map<String, ProcessSnapshot> idToProcessSnapshot = new LinkedHashMap<>();

  public CollaborationSnapshot(String name) {
    this.name = name;
  }

  public void addProcessSnapshot(ProcessSnapshot processSnapshot) {
    idToProcessSnapshot.put(processSnapshot.getSnapshotID(), processSnapshot);
  }

  public Collection<ProcessSnapshot> getProcessSnapshots() {
    return idToProcessSnapshot.values();
  }

  public void addToken(String pSnapshotID, Token token) {
    ProcessSnapshot processSnapshot = idToProcessSnapshot.get(pSnapshotID);
    if (processSnapshot == null) {
      throw new CollaborationSnapshotRuntimeException(
          String.format("Process snapshot with ID %s not found", pSnapshotID));
    }
    processSnapshot.addToken(token);
  }

  public String getName() {
    return name;
  }
}
