package behavior.activity.values;

public class IntegerValue implements Value {
    private final int value;

    public IntegerValue(int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

    @Override
    public <RETURN> RETURN accept(ValueVisitor<RETURN> visitor) {
        return visitor.handle(this);
    }
}
