package maude.generation;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Stream;

public class MaudeRuleBuilder {

    // Variables needed here?.
    private final Set<MaudeRule> createdRules;
    private String ruleName;
    private Set<MaudeObject> preObjects;
    private Set<MaudeObject> postObjects;

    public MaudeRuleBuilder() {
        this.createdRules = new LinkedHashSet<>();
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
        MaudeRule maudeRule = new MaudeRule(ruleName, preObjects, postObjects);
        createdRules.add(maudeRule);
        this.reset();
        return maudeRule;
    }

    public Stream<MaudeRule> createdRules() {
        return createdRules.stream();
    }

    private void reset() {
        this.ruleName = null;
        this.preObjects = new LinkedHashSet<>();
        this.postObjects = new LinkedHashSet<>();
    }
}
