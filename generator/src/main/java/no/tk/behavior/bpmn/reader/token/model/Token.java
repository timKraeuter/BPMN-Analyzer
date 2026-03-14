package no.tk.behavior.bpmn.reader.token.model;

public record Token(String elementID, boolean shouldExist) {
  public Token(String elementID) {
    this(elementID, true);
  }
}
