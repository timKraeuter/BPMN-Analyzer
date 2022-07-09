package maude.behaviortransformer;

import behavior.fsm.FiniteStateMachine;
import behavior.fsm.StateAtomicProposition;
import behavior.fsm.Transition;
import maude.generation.MaudeObject;
import maude.generation.MaudeRule;
import maude.generation.MaudeRuleBuilder;
import org.apache.commons.text.StringSubstitutor;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class FSMToMaudeTransformer {
    private final FiniteStateMachine finiteStateMachine;
    private final Set<StateAtomicProposition> atomicPropositions;
    private static final String MODULE_TEMPLATE = "load model-checker.maude .\n" +
                                                  "\n" +
                                                  "mod FSM-BEHAVIOR is\n" +
                                                  "    pr STRING .\n" +
                                                  "    pr CONFIGURATION .\n" +
                                                  "\n" +
                                                  "    op state :_ : String -> Attribute [ctor].\n" +
                                                  "    op FSM : -> Cid [ctor] .\n" +
                                                  "\n" +
                                                  "    subsort String < Oid .\n" +
                                                  "endm\n" +
                                                  "\n" +
                                                  "mod FSM-BEHAVIOR-${name} is\n" +
                                                  "    pr FSM-BEHAVIOR .\n" +
                                                  "\n" +
                                                  "    var X : String .\n" +
                                                  "\n" +
                                                  "    --- Generated rules\n" +
                                                  "    ${rules}\n" +
                                                  "\n" +
                                                  "    --- Generated initial config representing the start state of " +
                                                  "the FSM.\n" +
                                                  "    op initial : -> Configuration .\n" +
                                                  "    eq initial = < \"${name}\" : FSM | state : \"${startState}\" >" +
                                                  " .\n" +
                                                  "endm\n" +
                                                  "\n" +
                                                  "mod FSM-BEHAVIOR-${name}-PREDS is\n" +
                                                  "    pr FSM-BEHAVIOR-${name} .\n" +
                                                  "    pr SATISFACTION .\n" +
                                                  "    subsort Configuration < State .\n" +
                                                  "\n" +
                                                  "    var X : Oid .\n" +
                                                  "    var C : Configuration .\n" +
                                                  "    var P : Prop .\n" +
                                                  "\n" +
                                                  "    --- Generated atomic propositions\n" +
                                                  "    ${atomicPropositions}\n" +
                                                  "\n" +
                                                  "    eq C |= P = false [owise] .\n" +
                                                  "endm\n" +
                                                  "\n" +
                                                  "mod FSM-CHECK is\n" +
                                                  "    pr FSM-BEHAVIOR-${name}-PREDS .\n" +
                                                  "    pr MODEL-CHECKER .\n" +
                                                  "    pr LTL-SIMPLIFIER .\n" +
                                                  "endm\n" +
                                                  "\n" +
                                                  "red modelCheck(initial, ${ltlQuery}) .\n";

    public FSMToMaudeTransformer(FiniteStateMachine finiteStateMachine,
                                 Set<StateAtomicProposition> atomicPropositions) {
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
        return this.atomicPropositions.stream().map(stateAtomicProposition -> String.format(
                "op %s : Oid -> Prop .\n    eq < X : FSM | state : \"%s\" > C |= %s" + "(X) = true .",
                stateAtomicProposition.getName(),
                stateAtomicProposition.getState().getName(),
                stateAtomicProposition.getName()))
                                               .collect(Collectors.joining("\n    "));
    }

    private String makeRules() {
        MaudeRuleBuilder ruleBuilder = new MaudeRuleBuilder();
        // Create a rule for each transition
        finiteStateMachine.getTransitions().forEach(transition -> this.generateRuleForTransition(transition, ruleBuilder));

        return ruleBuilder.createdRules()
                          .map(MaudeRule::generateRuleString)
                          .collect(Collectors.joining("\n    "));
    }

    private void generateRuleForTransition(Transition transition, MaudeRuleBuilder ruleBuilder) {
        ruleBuilder.ruleName(transition.getName());
        ruleBuilder.addPreObject(createFSMinStateObject(transition.getSource().getName()));
        ruleBuilder.addPostObject(createFSMinStateObject(transition.getTarget().getName()));
        ruleBuilder.build();
    }

    private MaudeObject createFSMinStateObject(String stateName) {
        Map<String, String> attributeValues = new HashMap<>();
        attributeValues.put("state", String.format("\"%s\"", stateName));
        return new MaudeObject("X", "FSM", attributeValues);
    }
}
