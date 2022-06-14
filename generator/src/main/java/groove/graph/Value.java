package groove.graph;

public abstract class Value<TYPE> {
    protected final TYPE value;

    protected Value(TYPE value) {
        this.value = value;
    }

    public abstract String getTypeName();

    public abstract String getValue();
}
