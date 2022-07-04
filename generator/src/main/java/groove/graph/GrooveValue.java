package groove.graph;

public abstract class GrooveValue<T> {
    protected final T value;

    protected GrooveValue(T value) {
        this.value = value;
    }

    public abstract String getTypeName();

    public abstract String getValue();
}
