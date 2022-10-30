package behavior.activity.values;

public class BooleanValue implements Value {
  private final boolean value;

  public BooleanValue(boolean value) {
    this.value = value;
  }

  public boolean getValue() {
    return this.value;
  }

  @Override
  public <R> R accept(ValueVisitor<R> visitor) {
    return visitor.handle(this);
  }
}
