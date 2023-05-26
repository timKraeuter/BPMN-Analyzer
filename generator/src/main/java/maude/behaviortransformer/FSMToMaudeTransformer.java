package maude.behaviortransformer;

import behavior.fsm.FiniteStateMachine;
import behavior.fsm.FSMStateAtomicProposition;
import behavior.fsm.Transition;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import maude.generation.MaudeObject;
import maude.generation.MaudeRule;
import maude.generation.MaudeRuleBuilder;
import org.apache.commons.text.StringSubstitutor;

public class FSMToMaudeTransformer {
  public static final String ENQUOTE = "\"%s\"";
  private final FiniteStateMachine finiteStateMachine;
  private final Set<FSMStateAtomicProposition> atomicPropositions;

  private static final String MODULE_TEMPLATE =
      """
load model-checker.maude .\r
\r
mod FSM-BEHAVIOR is\r
    pr STRING .\r
    pr CONFIGURATION .\r
\r
    op state :_ : String -> Attribute [ctor].\r
    op name :_ : String -> Attribute [ctor].\r
    op FSM : -> Cid [ctor] .\r
\r
    subsort String < Oid .\r
endm\r
\r
mod FSM-BEHAVIOR-${name} is\r
    pr FSM-BEHAVIOR .\r
\r
    var X : String .\r
\r
    --- Generated rules\r
    ${rules}\r
\r
    --- Generated initial config representing the start state of the FSM.\r
    op initial : -> Configuration .\r
    eq initial = < "1" : FSM | name : "${name}", state : "${startState}" > .\r
endm\r
\r
mod FSM-BEHAVIOR-${name}-PREDS is\r
    pr FSM-BEHAVIOR-${name} .\r
    pr SATISFACTION .\r
    subsort Configuration < State .\r
\r
    var X : Oid .\r
    var C : Configuration .\r
    var P : Prop .\r
\r
    --- Generated atomic propositions\r
    ${atomicPropositions}\r
\r
    eq C |= P = false [owise] .\r
endm\r
\r
mod FSM-CHECK is\r
    pr FSM-BEHAVIOR-${name}-PREDS .\r
    pr MODEL-CHECKER .\r
    pr LTL-SIMPLIFIER .\r
endm\r
\r
red modelCheck(initial, ${ltlQuery}) .\r
""";

  public FSMToMaudeTransformer(
      FiniteStateMachine finiteStateMachine, Set<FSMStateAtomicProposition> atomicPropositions) {
    this.finiteStateMachine = finiteStateMachine;
    this.atomicPropositions = atomicPropositions;
  }

  public String generate(String ltlQuery) {
    Map<String, String> substitutionValues = new HashMap<>();
    substitutionValues.put("name", finiteStateMachine.getName());
    substitutionValues.put("startState", finiteStateMachine.getStartState().getName());
    substitutionValues.put("rules", this.makeRules());
    substitutionValues.put("atomicPropositions", this.makeAtomicPropositions());
    substitutionValues.put("ltlQuery", ltlQuery);
    return new StringSubstitutor(substitutionValues).replace(MODULE_TEMPLATE);
  }

  private String makeAtomicPropositions() {
    return this.atomicPropositions.stream()
        .map(
            proposition ->
                String.format(
                    "op %s : Oid -> Prop .\r\n    eq < X : FSM | name : \"%s\", state : \"%s\" > C |= %s"
                        + "(X) = true .",
                    proposition.getName(),
                    finiteStateMachine.getName(),
                    proposition.getState().getName(),
                    proposition.getName()))
        .collect(Collectors.joining("\r\n    "));
  }

  private String makeRules() {
    MaudeRuleBuilder ruleBuilder = new MaudeRuleBuilder();
    // Create a rule for each transition
    finiteStateMachine
        .getTransitions()
        .forEach(transition -> this.generateRuleForTransition(transition, ruleBuilder));

    return ruleBuilder
        .createdRules()
        .map(MaudeRule::generateRuleString)
        .collect(Collectors.joining("\r\n    "));
  }

  private void generateRuleForTransition(Transition transition, MaudeRuleBuilder ruleBuilder) {
    ruleBuilder.startRule(transition.getName());
    ruleBuilder.addPreObject(createFSMinStateObject(transition.getSource().getName()));
    ruleBuilder.addPostObject(createFSMinStateObject(transition.getTarget().getName()));
    ruleBuilder.buildRule();
  }

  private MaudeObject createFSMinStateObject(String stateName) {
    Map<String, String> attributeValues = new HashMap<>();
    attributeValues.put("name", String.format(ENQUOTE, finiteStateMachine.getName()));
    attributeValues.put("state", String.format(ENQUOTE, stateName));
    return new MaudeObject("X", "FSM", attributeValues);
  }
}
