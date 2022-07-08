package maude.behaviortransformer;

import behavior.fsm.FiniteStateMachine;
import behavior.fsm.Transition;
import maude.generation.MaudeObject;
import maude.generation.MaudeRule;
import maude.generation.MaudeRuleBuilder;
import org.apache.commons.text.StringSubstitutor;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class FSMToMaudeTransformer {
    private final FiniteStateMachine finiteStateMachine;
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
                                                  " " +
                                                  ".\n" +
                                                  "endm\n" +
                                                  "\n" +
                                                  "rew [10] in FSM-BEHAVIOR-${name} : initial .\n";

    public FSMToMaudeTransformer(FiniteStateMachine finiteStateMachine) {
        this.finiteStateMachine = finiteStateMachine;
    }

    public String generate() {
        Map<String, String> substitutionValues = new HashMap<>();
        substitutionValues.put("name", finiteStateMachine.getName());
        substitutionValues.put("startState", finiteStateMachine.getStartState().getName());
        substitutionValues.put("rules", this.makeRules());
        return new StringSubstitutor(substitutionValues).replace(MODULE_TEMPLATE);
    }

    private String makeRules() {
        Set<MaudeRule> createdRules = new LinkedHashSet<>();
        // Create rules
        MaudeRuleBuilder ruleBuilder = new MaudeRuleBuilder();
        finiteStateMachine.getTransitions().forEach(transition -> {
            createdRules.add(this.generateRuleForTransition(transition, ruleBuilder));
            ruleBuilder.reset();
        });

        return createdRules.stream()
                           .map(MaudeRule::generateRule)
                           .collect(Collectors.joining("\n    "));
    }

    private MaudeRule generateRuleForTransition(Transition transition,
                                                MaudeRuleBuilder ruleBuilder) {
        ruleBuilder.ruleName(transition.getName());
        ruleBuilder.addPreObject(createFSMinStateObject(transition.getSource().getName()));
        ruleBuilder.addPostObject(createFSMinStateObject(transition.getTarget().getName()));
        return ruleBuilder.build();
    }

    private MaudeObject createFSMinStateObject(String stateName) {
        Map<String, String> attributeValues = new HashMap<>();
        attributeValues.put("state", String.format("\"%s\"", stateName));
        return new MaudeObject("X", "FSM", attributeValues);
    }
}
