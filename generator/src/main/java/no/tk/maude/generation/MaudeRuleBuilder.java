package no.tk.maude.generation;

import java.util.LinkedHashSet;

public class MaudeRuleBuilder extends MaudeRuleBuilderBase<MaudeRuleBuilder> {

  public MaudeRuleBuilder() {
    super();
    setSelfReference(this);
  }

  @Override
  public MaudeRule buildRule() {
    if (ruleName == null || preObjects.isEmpty() || postObjects.isEmpty()) {
      throw new MaudeGenerationException(
          "A rule should have a name and at least one pre/post object");
    }
    MaudeRule maudeRule = new MaudeRule(ruleName, preObjects, postObjects, condition);
    createdRules.add(maudeRule);
    this.reset();
    return maudeRule;
  }

  @Override
  public void reset() {
    this.ruleName = null;
    this.preObjects = new LinkedHashSet<>();
    this.postObjects = new LinkedHashSet<>();
  }
}
