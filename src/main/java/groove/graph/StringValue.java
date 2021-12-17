package groove.graph;

class StringValue extends Value<String> {
    public StringValue(String value) {
        super(value);
    }

    @Override
    public String getTypeName() {
        return "string";
    }

    @Override
    public String getValue() {
        return String.format("\"%s\"", this.value);
    }
}
