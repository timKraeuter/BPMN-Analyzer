package maude.generation;

import org.apache.commons.text.StringSubstitutor;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class MaudeRule {
    private static final String RULE_TEMPLATE = "rl [${name}] :  ${preObjects} => ${postObjects} .";

    private final String name;
    private final Set<MaudeObject> preObjects;
    private final Set<MaudeObject> postObjects;

    public MaudeRule(String name, Set<MaudeObject> preObjects, Set<MaudeObject> postObjects) {
        this.name = name;
        this.preObjects = preObjects;
        this.postObjects = postObjects;
    }

    public String generateRuleString() {
        Map<String, String> substitutionValues = new HashMap<>();
        substitutionValues.put("name", name);
        substitutionValues.put("preObjects", this.makePreObjects());
        substitutionValues.put("postObjects", this.makePostObjects());
        return new StringSubstitutor(substitutionValues).replace(RULE_TEMPLATE);
    }

    private String makePostObjects() {
        return makeObjects(postObjects);
    }

    private String makePreObjects() {
        return makeObjects(preObjects);
    }

    private String makeObjects(Set<MaudeObject> postObjects) {
        return postObjects.stream().map(MaudeObject::generateObjectString).collect(Collectors.joining(" "));
    }
}
