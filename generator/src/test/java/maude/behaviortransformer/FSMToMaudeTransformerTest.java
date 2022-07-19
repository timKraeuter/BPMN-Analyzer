package maude.behaviortransformer;

import behavior.fsm.FiniteStateMachine;
import behavior.fsm.State;
import behavior.fsm.StateAtomicProposition;
import behavior.fsm.Transition;
import com.google.common.collect.ImmutableSet;
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

    private static final String EXPECTED_TWO_STATE_FSM_MODULE = "load model-checker.maude .\r\n" +
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
                                                                "mod FSM-BEHAVIOR-X is\r\n" +
                                                                "    pr FSM-BEHAVIOR .\r\n" +
                                                                "\r\n" +
                                                                "    var X : String .\r\n" +
                                                                "\r\n" +
                                                                "    --- Generated rules\r\n" +
                                                                "    rl [turnGreen] :  < X : FSM | state : \"red\" > " +
                                                                "=> < X : FSM | state : \"green\" > .\r\n" +
                                                                "    rl [turnRed] :  < X : FSM | state : \"green\" > " +
                                                                "=> < X : FSM | state : \"red\" > .\r\n" +
                                                                "\r\n" +
                                                                "    --- Generated initial config representing the " +
                                                                "start state of the FSM.\r\n" +
                                                                "    op initial : -> Configuration .\r\n" +
                                                                "    eq initial = < \"X\" : FSM | state : \"red\" > " +
                                                                ".\r\n" +
                                                                "endm\r\n" +
                                                                "\r\n" +
                                                                "mod FSM-BEHAVIOR-X-PREDS is\r\n" +
                                                                "    pr FSM-BEHAVIOR-X .\r\n" +
                                                                "    pr SATISFACTION .\r\n" +
                                                                "    subsort Configuration < State .\r\n" +
                                                                "\r\n" +
                                                                "    var X : Oid .\r\n" +
                                                                "    var C : Configuration .\r\n" +
                                                                "    var P : Prop .\r\n" +
                                                                "\r\n" +
                                                                "    --- Generated atomic propositions\r\n" +
                                                                "    op red : Oid -> Prop .\r\n" +
                                                                "    eq < X : FSM | state : \"red\" > C |= red(X) = " +
                                                                "true .\r\n" +
                                                                "    op green : Oid -> Prop .\r\n" +
                                                                "    eq < X : FSM | state : \"green\" > C |= green(X)" +
                                                                " = true .\r\n" +
                                                                "\r\n" +
                                                                "    eq C |= P = false [owise] .\r\n" +
                                                                "endm\r\n" +
                                                                "\r\n" +
                                                                "mod FSM-CHECK is\r\n" +
                                                                "    pr FSM-BEHAVIOR-X-PREDS .\r\n" +
                                                                "    pr MODEL-CHECKER .\r\n" +
                                                                "    pr LTL-SIMPLIFIER .\r\n" +
                                                                "endm\r\n" +
                                                                "\r\n" +
                                                                "red modelCheck(initial, <> green(\"X\")) .\r\n";


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

    private static final String EXPECTED_FOUR_STATE_FSM_MODULE = "load model-checker.maude .\r\n" +
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
                                                                 "mod FSM-BEHAVIOR-trafficLight is\r\n" +
                                                                 "    pr FSM-BEHAVIOR .\r\n" +
                                                                 "\r\n" +
                                                                 "    var X : String .\r\n" +
                                                                 "\r\n" +
                                                                 "    --- Generated rules\r\n" +
                                                                 "    rl [turn_red_amber] :  < X : FSM | state : " +
                                                                 "\"red\" > => < X : FSM | state : \"red-amber\" > " +
                                                                 ".\r\n" +
                                                                 "    rl [turn_green] :  < X : FSM | state : " +
                                                                 "\"red-amber\" > => < X : FSM | state : \"green\" > " +
                                                                 ".\r\n" +
                                                                 "    rl [turn_amber] :  < X : FSM | state : " +
                                                                 "\"green\" > => < X : FSM | state : \"amber\" > .\r\n" +
                                                                 "    rl [turn_red] :  < X : FSM | state : \"amber\" " +
                                                                 "> => < X : FSM | state : \"red\" > .\r\n" +
                                                                 "\r\n" +
                                                                 "    --- Generated initial config representing the " +
                                                                 "start state of the FSM.\r\n" +
                                                                 "    op initial : -> Configuration .\r\n" +
                                                                 "    eq initial = < \"trafficLight\" : FSM | state :" +
                                                                 " \"red\" > .\r\n" +
                                                                 "endm\r\n" +
                                                                 "\r\n" +
                                                                 "mod FSM-BEHAVIOR-trafficLight-PREDS is\r\n" +
                                                                 "    pr FSM-BEHAVIOR-trafficLight .\r\n" +
                                                                 "    pr SATISFACTION .\r\n" +
                                                                 "    subsort Configuration < State .\r\n" +
                                                                 "\r\n" +
                                                                 "    var X : Oid .\r\n" +
                                                                 "    var C : Configuration .\r\n" +
                                                                 "    var P : Prop .\r\n" +
                                                                 "\r\n" +
                                                                 "    --- Generated atomic propositions\r\n" +
                                                                 "    op redish : Oid -> Prop .\r\n" +
                                                                 "    eq < X : FSM | state : \"red\" > C |= redish(X)" +
                                                                 " = true .\r\n" +
                                                                 "    op green : Oid -> Prop .\r\n" +
                                                                 "    eq < X : FSM | state : \"green\" > C |= green" +
                                                                 "(X) = true .\r\n" +
                                                                 "\r\n" +
                                                                 "    eq C |= P = false [owise] .\r\n" +
                                                                 "endm\r\n" +
                                                                 "\r\n" +
                                                                 "mod FSM-CHECK is\r\n" +
                                                                 "    pr FSM-BEHAVIOR-trafficLight-PREDS .\r\n" +
                                                                 "    pr MODEL-CHECKER .\r\n" +
                                                                 "    pr LTL-SIMPLIFIER .\r\n" +
                                                                 "endm\r\n" +
                                                                 "\r\n" +
                                                                 "red modelCheck(initial, <> redish(\"trafficLight\")" +
                                                                 ") .\r\n";
}