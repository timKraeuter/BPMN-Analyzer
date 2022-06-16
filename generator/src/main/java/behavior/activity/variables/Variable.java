package behavior.activity.variables;

import behavior.activity.values.Value;

public abstract class Variable<V extends Value> {
    private final String name;
    private final V initialValue;

    protected Variable(String name, V initialValue) {
        this.name = name;
        this.initialValue = initialValue;
    }

    public String getName() {
        return this.name;
    }

    public V getInitialValue() {
        return this.initialValue;
    }
}
