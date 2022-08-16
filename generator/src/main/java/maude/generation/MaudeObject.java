package maude.generation;

import org.apache.commons.text.StringSubstitutor;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class MaudeObject {
    private static final String OBJECT_TEMPLATE = "< ${oid} : ${oidType} | ${attributeValues} >";

    private final String oid;
    private final String oidType;
    private final Map<String, String> attributeValues;

    public MaudeObject(String oid, String oidType, Map<String, String> attributeValues) {
        this.oid = oid;
        this.oidType = oidType;
        this.attributeValues = attributeValues;
    }

    public String generateObjectString() {
        Map<String, String> substitutionValues = new HashMap<>();
        substitutionValues.put("oid", oid);
        substitutionValues.put("oidType", oidType);
        substitutionValues.put("attributeValues", this.makeAttributes());
        return new StringSubstitutor(substitutionValues).replace(OBJECT_TEMPLATE);
    }

    private String makeAttributes() {
        return attributeValues.entrySet().stream()
                              .map(attributeValue -> String.format("%s : %s", attributeValue.getKey(), attributeValue.getValue()))
                              .collect(Collectors.joining(", "));
    }
}
