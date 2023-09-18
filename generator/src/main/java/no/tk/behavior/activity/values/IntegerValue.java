package no.tk.behavior.activity.values;

public class IntegerValue implements Value {
  private final int value;

  public IntegerValue(int value) {
    this.value = value;
  }

  public int getValue() {
    return this.value;
  }

  @Override
  public <R> R accept(ValueVisitor<R> visitor) {
    return visitor.handle(this);
  }
}
