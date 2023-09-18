package no.tk.maude.generation;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.text.StringSubstitutor;

public class MaudeRule {
  private static final String RULE_TEMPLATE = "rl [${name}] :  ${preObjects} => ${postObjects} .";
  private static final String CRULE_TEMPLATE =
      "crl [${name}] :  ${preObjects} => ${postObjects} if ${condition} .";

  private final String name;
  private final Set<MaudeObject> preObjects;
  private final Set<MaudeObject> postObjects;
  private final String condition;

  public MaudeRule(
      String name, Set<MaudeObject> preObjects, Set<MaudeObject> postObjects, String condition) {
    this.name = name;
    this.preObjects = preObjects;
    this.postObjects = postObjects;
    this.condition = condition;
  }

  public String generateRuleString() {
    Map<String, String> substitutionValues = new HashMap<>();
    substitutionValues.put("name", name);
    substitutionValues.put("preObjects", this.makePreObjects());
    substitutionValues.put("postObjects", this.makePostObjects());
    if (condition.isBlank()) {

      return new StringSubstitutor(substitutionValues).replace(RULE_TEMPLATE);
    }
    substitutionValues.put("condition", this.condition);
    return new StringSubstitutor(substitutionValues).replace(CRULE_TEMPLATE);
  }

  private String makePostObjects() {
    return makeObjects(postObjects);
  }

  private String makePreObjects() {
    return makeObjects(preObjects);
  }

  private String makeObjects(Set<MaudeObject> postObjects) {
    return postObjects.stream()
        .map(MaudeObject::generateObjectString)
        .collect(Collectors.joining(" "));
  }
}
