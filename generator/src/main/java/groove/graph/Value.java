package groove.graph;

public abstract class Value<T> {
    protected final T value;

    protected Value(T value) {
        this.value = value;
    }

    public abstract String getTypeName();

    public abstract String getValue();
}
