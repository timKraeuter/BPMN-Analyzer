package maude.generation;

import com.google.common.collect.Sets;
import org.apache.commons.lang3.tuple.Pair;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

public class MaudeRuleBuilder {

    // Variables needed here?.
    private final Set<MaudeRule> createdRules;
    private String ruleName;
    private Set<MaudeObject> preObjects;
    private Set<MaudeObject> postObjects;
    // Variable group to variable type and variables.
    private final Map<String, Pair<String, Set<String>>> vargroupToTypeAndVars;

    public MaudeRuleBuilder() {
        this.createdRules = new LinkedHashSet<>();
        this.preObjects = new LinkedHashSet<>();
        this.postObjects = new LinkedHashSet<>();
        this.vargroupToTypeAndVars = new LinkedHashMap<>();
    }

    public MaudeRuleBuilder startRule(String ruleName) {
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

    public MaudeRule buildRule() {
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

    public MaudeRuleBuilder addVar(String varGroupName, String type, String varname) {
        Pair<String, Set<String>> varGroup = this.vargroupToTypeAndVars.get(varGroupName);
        if (varGroup == null) {
            vargroupToTypeAndVars.put(varGroupName, Pair.of(type, Sets.newHashSet(varname)));
        } else {
            if (!varGroup.getLeft().equals(type)) {
                throw new MaudeGenerationException("Variable group type does not match!");
            }
            varGroup.getRight().add(varname);
        }
        return this;
    }

    public String getVarsForGroup(String varGroupName) {
        Pair<String, Set<String>> vars = this.vargroupToTypeAndVars.get(varGroupName);
        if (vars == null) {
            return "";
        }
        return String.format("vars %s : %s . --- %s", String.join(" ", vars.getRight()), vars.getLeft(), varGroupName);
    }
}
