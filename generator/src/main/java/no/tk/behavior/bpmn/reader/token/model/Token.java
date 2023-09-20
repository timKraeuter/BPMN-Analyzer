package no.tk.behavior.bpmn.reader.token.model;

public class Token {
  private final String elementID;
  private final boolean shouldExist;

  public Token(String elementID) {
    this(elementID, true);
  }

  public Token(String elementID, boolean shouldExist) {
    this.elementID = elementID;
    this.shouldExist = shouldExist;
  }

  public String getElementID() {
    return elementID;
  }

  public boolean isShouldExist() {
    return shouldExist;
  }
}
