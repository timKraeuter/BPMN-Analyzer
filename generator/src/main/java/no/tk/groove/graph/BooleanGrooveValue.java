package no.tk.groove.graph;

class BooleanGrooveValue extends GrooveValue<Boolean> {
  public BooleanGrooveValue(Boolean value) {
    super(value);
  }

  @Override
  public String getTypeName() {
    return "bool";
  }

  @Override
  public String getValue() {
    return this.value.toString();
  }
}
