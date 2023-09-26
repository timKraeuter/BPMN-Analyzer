package no.tk.behavior.bpmn.reader.token.model;

import java.util.LinkedHashSet;
import java.util.Set;

public class ProcessSnapshot {
  private final String processID;
  private final String snapshotID;
  private final Set<Token> tokens = new LinkedHashSet<>();
  private final boolean shouldExist;

  public ProcessSnapshot(String processID, String snapshotID, boolean shouldExist) {
    this.processID = processID;
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

  public String getProcessName() {
    return processID;
  }

  public String getSnapshotID() {
    return snapshotID;
  }
}
