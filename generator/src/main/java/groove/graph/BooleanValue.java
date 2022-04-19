package groove.graph;

class BooleanValue extends Value<Boolean> {
    public BooleanValue(Boolean value) {
        super(value);
    }

    @Override
    public String getTypeName() {
        return "bool";
    }

    @Override
    public String getValue() {
        return this.value.toString();
    }
}
