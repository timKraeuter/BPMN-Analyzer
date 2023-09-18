package no.tk.groove.graph;

class StringGrooveValue extends GrooveValue<String> {
  public StringGrooveValue(String value) {
    super(value);
  }

  @Override
  public String getTypeName() {
    return "string";
  }

  @Override
  public String getValue() {
    return String.format("\"%s\"", this.value);
  }
}
