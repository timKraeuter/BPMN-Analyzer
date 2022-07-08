package maude.behaviortransformer;

import behavior.fsm.FiniteStateMachine;
import behavior.fsm.State;
import behavior.fsm.StateAtomicProposition;
import behavior.fsm.Transition;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class FSMToMaudeTransformerTest {

    @Test
    void generateTwoStateFSMTest() {
        // Create FSM
        State red = new State("red");
        FiniteStateMachine fsm = new FiniteStateMachine("X", red);
        State green = new State("green");
        fsm.addTransition(new Transition("turnGreen", red, green));
        fsm.addTransition(new Transition("turnRed", green, red));

        FSMToMaudeTransformer transformer = new FSMToMaudeTransformer(fsm,
                                                                      ImmutableSet.of(new StateAtomicProposition(red),
                                                                                      new StateAtomicProposition(green)));
        // Transform FSM
        assertThat(transformer.generate("<> green(\"X\")"), is(EXPECTED_TWO_STATE_FSM_MODULE));
        // Result runs in maude with the following output:
        // reduce in FSM-CHECK : modelCheck(initial, <> green("X")) .
        // rewrites: 10 in 0ms cpu (0ms real) (~ rewrites/second)
        // result Bool: true
    }

    private static final String EXPECTED_TWO_STATE_FSM_MODULE = "load model-checker.maude .\n" +
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
                                                                "mod FSM-BEHAVIOR-X is\n" +
                                                                "    pr FSM-BEHAVIOR .\n" +
                                                                "\n" +
                                                                "    var X : String .\n" +
                                                                "\n" +
                                                                "    --- Generated rules\n" +
                                                                "    rl [turnGreen] :  < X : FSM | state : \"red\" > " +
                                                                "=> < X : FSM | state : \"green\" > .\n" +
                                                                "    rl [turnRed] :  < X : FSM | state : \"green\" > " +
                                                                "=> < X : FSM | state : \"red\" > .\n" +
                                                                "\n" +
                                                                "    --- Generated initial config representing the " +
                                                                "start state of the FSM.\n" +
                                                                "    op initial : -> Configuration .\n" +
                                                                "    eq initial = < \"X\" : FSM | state : \"red\" > " +
                                                                ".\n" +
                                                                "endm\n" +
                                                                "\n" +
                                                                "mod FSM-BEHAVIOR-X-PREDS is\n" +
                                                                "    pr FSM-BEHAVIOR-X .\n" +
                                                                "    pr SATISFACTION .\n" +
                                                                "    subsort Configuration < State .\n" +
                                                                "\n" +
                                                                "    var X : Oid .\n" +
                                                                "    var C : Configuration .\n" +
                                                                "    var P : Prop .\n" +
                                                                "\n" +
                                                                "    --- Generated atomic propositions\n" +
                                                                "    op red : Oid -> Prop .\n" +
                                                                "    eq < X : FSM | state : \"red\" > C |= red(X) = " +
                                                                "true .\n" +
                                                                "    op green : Oid -> Prop .\n" +
                                                                "    eq < X : FSM | state : \"green\" > C |= green(X)" +
                                                                " = true .\n" +
                                                                "\n" +
                                                                "    eq C |= P = false [owise] .\n" +
                                                                "endm\n" +
                                                                "\n" +
                                                                "mod FSM-CHECK is\n" +
                                                                "    pr FSM-BEHAVIOR-X-PREDS .\n" +
                                                                "    pr MODEL-CHECKER .\n" +
                                                                "    pr LTL-SIMPLIFIER .\n" +
                                                                "endm\n" +
                                                                "\n" +
                                                                "red modelCheck(initial, <> green(\"X\")) .\n";


    @Test
    void generateFourStateFSMTest() {
        // Create FSM
        State red = new State("red");
        String fsmName = "trafficLight";
        FiniteStateMachine fsm = new FiniteStateMachine(fsmName, red);
        State red_amber = new State("red-amber");
        State green = new State("green");
        State amber = new State("amber");
        fsm.addTransition(new Transition("turn_red_amber", red, red_amber));
        fsm.addTransition(new Transition("turn_green", red_amber, green));
        fsm.addTransition(new Transition("turn_amber", green, amber));
        fsm.addTransition(new Transition("turn_red", amber, red));

        FSMToMaudeTransformer transformer = new FSMToMaudeTransformer(fsm,
                                                                      ImmutableSet.of(new StateAtomicProposition("redish", red),
                                                                                      new StateAtomicProposition(green)));
        // Transform FSM
        assertThat(transformer.generate("<> redish(\"trafficLight\")"), is(EXPECTED_FOUR_STATE_FSM_MODULE));
        // Maude output
        // reduce in FSM-CHECK : modelCheck(initial, <> redish("trafficLight")) .
        // rewrites: 8 in 0ms cpu (0ms real) (~ rewrites/second)
        // result Bool: true
    }

    private static final String EXPECTED_FOUR_STATE_FSM_MODULE = "load model-checker.maude .\n" +
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
                                                                 "mod FSM-BEHAVIOR-trafficLight is\n" +
                                                                 "    pr FSM-BEHAVIOR .\n" +
                                                                 "\n" +
                                                                 "    var X : String .\n" +
                                                                 "\n" +
                                                                 "    --- Generated rules\n" +
                                                                 "    rl [turn_red_amber] :  < X : FSM | state : " +
                                                                 "\"red\" > => < X : FSM | state : \"red-amber\" > " +
                                                                 ".\n" +
                                                                 "    rl [turn_green] :  < X : FSM | state : " +
                                                                 "\"red-amber\" > => < X : FSM | state : \"green\" > " +
                                                                 ".\n" +
                                                                 "    rl [turn_amber] :  < X : FSM | state : " +
                                                                 "\"green\" > => < X : FSM | state : \"amber\" > .\n" +
                                                                 "    rl [turn_red] :  < X : FSM | state : \"amber\" " +
                                                                 "> => < X : FSM | state : \"red\" > .\n" +
                                                                 "\n" +
                                                                 "    --- Generated initial config representing the " +
                                                                 "start state of the FSM.\n" +
                                                                 "    op initial : -> Configuration .\n" +
                                                                 "    eq initial = < \"trafficLight\" : FSM | state :" +
                                                                 " \"red\" > .\n" +
                                                                 "endm\n" +
                                                                 "\n" +
                                                                 "mod FSM-BEHAVIOR-trafficLight-PREDS is\n" +
                                                                 "    pr FSM-BEHAVIOR-trafficLight .\n" +
                                                                 "    pr SATISFACTION .\n" +
                                                                 "    subsort Configuration < State .\n" +
                                                                 "\n" +
                                                                 "    var X : Oid .\n" +
                                                                 "    var C : Configuration .\n" +
                                                                 "    var P : Prop .\n" +
                                                                 "\n" +
                                                                 "    --- Generated atomic propositions\n" +
                                                                 "    op redish : Oid -> Prop .\n" +
                                                                 "    eq < X : FSM | state : \"red\" > C |= redish(X)" +
                                                                 " = true .\n" +
                                                                 "    op green : Oid -> Prop .\n" +
                                                                 "    eq < X : FSM | state : \"green\" > C |= green" +
                                                                 "(X) = true .\n" +
                                                                 "\n" +
                                                                 "    eq C |= P = false [owise] .\n" +
                                                                 "endm\n" +
                                                                 "\n" +
                                                                 "mod FSM-CHECK is\n" +
                                                                 "    pr FSM-BEHAVIOR-trafficLight-PREDS .\n" +
                                                                 "    pr MODEL-CHECKER .\n" +
                                                                 "    pr LTL-SIMPLIFIER .\n" +
                                                                 "endm\n" +
                                                                 "\n" +
                                                                 "red modelCheck(initial, <> redish(\"trafficLight\")" +
                                                                 ") .\n";
}