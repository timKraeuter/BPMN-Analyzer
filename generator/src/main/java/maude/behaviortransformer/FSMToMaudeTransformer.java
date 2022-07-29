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
    private static final String MODULE_TEMPLATE = "load model-checker.maude .\r\n" +
                                                  "\r\n" +
                                                  "mod FSM-BEHAVIOR is\r\n" +
                                                  "    pr STRING .\r\n" +
                                                  "    pr CONFIGURATION .\r\n" +
                                                  "\r\n" +
                                                  "    op state :_ : String -> Attribute [ctor].\r\n" +
                                                  "    op FSM : -> Cid [ctor] .\r\n" +
                                                  "\r\n" +
                                                  "    subsort String < Oid .\r\n" +
                                                  "endm\r\n" +
                                                  "\r\n" +
                                                  "mod FSM-BEHAVIOR-${name} is\r\n" +
                                                  "    pr FSM-BEHAVIOR .\r\n" +
                                                  "\r\n" +
                                                  "    var X : String .\r\n" +
                                                  "\r\n" +
                                                  "    --- Generated rules\r\n" +
                                                  "    ${rules}\r\n" +
                                                  "\r\n" +
                                                  "    --- Generated initial config representing the start state of " +
                                                  "the FSM.\r\n" +
                                                  "    op initial : -> Configuration .\r\n" +
                                                  "    eq initial = < \"${name}\" : FSM | state : \"${startState}\" >" +
                                                  " .\r\n" +
                                                  "endm\r\n" +
                                                  "\r\n" +
                                                  "mod FSM-BEHAVIOR-${name}-PREDS is\r\n" +
                                                  "    pr FSM-BEHAVIOR-${name} .\r\n" +
                                                  "    pr SATISFACTION .\r\n" +
                                                  "    subsort Configuration < State .\r\n" +
                                                  "\r\n" +
                                                  "    var X : Oid .\r\n" +
                                                  "    var C : Configuration .\r\n" +
                                                  "    var P : Prop .\r\n" +
                                                  "\r\n" +
                                                  "    --- Generated atomic propositions\r\n" +
                                                  "    ${atomicPropositions}\r\n" +
                                                  "\r\n" +
                                                  "    eq C |= P = false [owise] .\r\n" +
                                                  "endm\r\n" +
                                                  "\r\n" +
                                                  "mod FSM-CHECK is\r\n" +
                                                  "    pr FSM-BEHAVIOR-${name}-PREDS .\r\n" +
                                                  "    pr MODEL-CHECKER .\r\n" +
                                                  "    pr LTL-SIMPLIFIER .\r\n" +
                                                  "endm\r\n" +
                                                  "\r\n" +
                                                  "red modelCheck(initial, ${ltlQuery}) .\r\n";

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
                "op %s : Oid -> Prop .\r\n    eq < X : FSM | state : \"%s\" > C |= %s" + "(X) = true .",
                stateAtomicProposition.getName(),
                stateAtomicProposition.getState().getName(),
                stateAtomicProposition.getName()))
                                               .collect(Collectors.joining("\r\n    "));
    }

    private String makeRules() {
        MaudeRuleBuilder ruleBuilder = new MaudeRuleBuilder();
        // Create a rule for each transition
        finiteStateMachine.getTransitions().forEach(transition -> this.generateRuleForTransition(transition, ruleBuilder));

        return ruleBuilder.createdRules()
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
        attributeValues.put("state", String.format("\"%s\"", stateName));
        return new MaudeObject("X", "FSM", attributeValues);
    }
}
