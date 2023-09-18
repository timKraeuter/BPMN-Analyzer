package no.tk.maude.generation;

import static no.tk.maude.behaviortransformer.bpmn.BPMNToMaudeTransformerConstants.NEW_LINE;

import com.google.common.collect.Sets;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.tuple.Pair;

public abstract class MaudeRuleBuilderBase<T extends MaudeRuleBuilderBase<T>> {

  protected final Set<MaudeRule> createdRules;
  protected String ruleName;
  protected Set<MaudeObject> preObjects;
  protected Set<MaudeObject> postObjects;
  // Variable group to variable type and variables.
  protected final Map<String, Pair<String, Set<String>>> vargroupToTypeAndVars;
  protected String condition;

  private T selfReference;

  protected MaudeRuleBuilderBase() {
    this.createdRules = new LinkedHashSet<>();
    this.preObjects = new LinkedHashSet<>();
    this.postObjects = new LinkedHashSet<>();
    this.vargroupToTypeAndVars = new LinkedHashMap<>();
    condition = "";
  }

  public T startRule(String ruleName) {
    this.ruleName = ruleName;
    return selfReference;
  }

  public T addPreObject(MaudeObject preObject) {
    this.preObjects.add(preObject);
    return selfReference;
  }

  public T addPostObject(MaudeObject postObject) {
    this.postObjects.add(postObject);
    return selfReference;
  }

  public T setCondition(String condition) {
    this.condition = condition;
    return selfReference;
  }

  public abstract MaudeRule buildRule();

  public Stream<MaudeRule> createdRules() {
    return createdRules.stream();
  }

  abstract void reset();

  public T addVar(String varGroupName, String type, String varname) {
    Pair<String, Set<String>> varGroup = this.vargroupToTypeAndVars.get(varGroupName);
    if (varGroup == null) {
      vargroupToTypeAndVars.put(varGroupName, Pair.of(type, Sets.newHashSet(varname)));
    } else {
      if (!varGroup.getLeft().equals(type)) {
        throw new MaudeGenerationException("Variable group type does not match!");
      }
      varGroup.getRight().add(varname);
    }
    return selfReference;
  }

  public String getVars() {
    return vargroupToTypeAndVars.keySet().stream()
        .map(this::getVarsForGroup)
        .collect(Collectors.joining(NEW_LINE));
  }

  private String getVarsForGroup(String varGroupName) {
    Pair<String, Set<String>> vars = this.vargroupToTypeAndVars.get(varGroupName);
    if (vars == null) {
      return "";
    }
    return String.format(
        "vars %s : %s . --- %s", String.join(" ", vars.getRight()), vars.getLeft(), varGroupName);
  }

  public void setSelfReference(T selfReference) {
    this.selfReference = selfReference;
  }
}
