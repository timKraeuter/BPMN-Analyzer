package no.tk.groove.graph;

/** Represent an integer value for a groove graph. */
class IntGrooveValue extends GrooveValue<Integer> {
  public IntGrooveValue(Integer value) {
    super(value);
  }

  @Override
  public String getTypeName() {
    return "int";
  }

  @Override
  public String getValue() {
    return this.value.toString();
  }
}
