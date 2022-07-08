package maude.generation;

import java.util.LinkedHashMap;
import java.util.Map;

public class MaudeObjectBuilder {

    private String oid;
    private String oidType;
    private Map<String, String> attributeValues;

    public MaudeObjectBuilder() {
        this.attributeValues = new LinkedHashMap<>();
    }

    public MaudeObjectBuilder oid(String oid) {
        this.oid = oid;
        return this;
    }

    public MaudeObjectBuilder oidType(String oidType) {
        this.oidType = oidType;
        return this;
    }

    public MaudeObjectBuilder addAttributeValue(String attrType, String attrValue) {
        attributeValues.put(attrType, attrValue);
        return this;
    }

    public MaudeObject build() {
        if (oid == null || oidType == null) {
            throw new MaudeGenerationException("Oid or oidType must not be null!");
        }
        return new MaudeObject(oid, oidType, attributeValues);
    }

    public MaudeObjectBuilder reset() {
        this.oid = null;
        this.oidType = null;
        this.attributeValues = new LinkedHashMap<>();
        return this;
    }
}
