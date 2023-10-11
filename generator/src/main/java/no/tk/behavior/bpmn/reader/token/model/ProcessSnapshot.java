package no.tk.behavior.bpmn.reader.token.model;

import java.util.LinkedHashSet;
import java.util.Set;

public class ProcessSnapshot {
  private final String snapshotID;

  /**
   * The snapshot name is derived from the participant name in BPMN. Might be null if the snapshot
   * is not attached to a participant.
   */
  private final String snapshotName;

  private final Set<Token> tokens = new LinkedHashSet<>();
  private final boolean shouldExist;

  public ProcessSnapshot(String snapshotName, String snapshotID, boolean shouldExist) {
    this.snapshotName = snapshotName;
    this.snapshotID = snapshotID;
    this.shouldExist = shouldExist;
  }

  public void addToken(Token token) {
    tokens.add(token);
  }

  public Set<Token> getTokens() {
    return tokens;
  }

  public boolean isShouldExist() {
    return shouldExist;
  }

  public String getSnapshotNameIfExists() {
    return snapshotName;
  }

  public String getSnapshotID() {
    return snapshotID;
  }
}
