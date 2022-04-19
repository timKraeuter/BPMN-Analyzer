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
    public <RETURN> RETURN accept(ValueVisitor<RETURN> visitor) {
        return visitor.handle(this);
    }
}
