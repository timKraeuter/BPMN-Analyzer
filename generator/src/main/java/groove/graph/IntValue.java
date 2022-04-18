package groove.graph;

/**
 * Represent an integer value for a groove graph.
 */
class IntValue extends Value<Integer> {
    public IntValue(Integer value) {
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
