package maude.generation;

import java.util.LinkedHashSet;
import java.util.Set;

public class MaudeRuleBuilder {

    // Variables needed here?.
    // Set of rules needed here?.
    private String ruleName;
    private Set<MaudeObject> preObjects;
    private Set<MaudeObject> postObjects;

    public MaudeRuleBuilder() {
        this.preObjects = new LinkedHashSet<>();
        this.postObjects = new LinkedHashSet<>();
    }

    public MaudeRuleBuilder ruleName(String ruleName) {
        this.ruleName = ruleName;
        return this;
    }

    public MaudeRuleBuilder addPreObject(MaudeObject preObject) {
        this.preObjects.add(preObject);
        return this;
    }

    public MaudeRuleBuilder addPostObject(MaudeObject postObject) {
        this.postObjects.add(postObject);
        return this;
    }

    public MaudeRule build() {
        if (ruleName == null || preObjects.isEmpty()) {
            throw new MaudeGenerationException("A rule should have a name and at least one pre object");
        }
        return new MaudeRule(ruleName, preObjects, postObjects);
    }

    public MaudeRuleBuilder reset() {
        this.ruleName = null;
        this.preObjects = new LinkedHashSet<>();
        this.postObjects = new LinkedHashSet<>();
        return this;
    }
}
