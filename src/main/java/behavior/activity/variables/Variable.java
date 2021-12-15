package behavior.activity.variables;

import behavior.activity.values.Value;

public abstract class Variable<VALUE extends Value> {
    private final String name;
    private final VALUE initialValue;

    protected Variable(String name, VALUE initialValue) {
        this.name = name;
        this.initialValue = initialValue;
    }

    public String getName() {
        return this.name;
    }

    public VALUE getInitialValue() {
        return this.initialValue;
    }
}
