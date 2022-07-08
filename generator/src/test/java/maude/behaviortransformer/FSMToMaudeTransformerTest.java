package maude.behaviortransformer;

import behavior.fsm.FiniteStateMachine;
import behavior.fsm.State;
import behavior.fsm.Transition;
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

        FSMToMaudeTransformer transformer = new FSMToMaudeTransformer(fsm);

        // Transform FSM
        assertThat(transformer.generate(), is(EXPECTED_TWO_STATE_FSM_MODULE));
        // Result runs in maude with the following output:
        // rewrite [10] in FSM-BEHAVIOR-X : initial .
        // rewrites: 11 in 0ms cpu (0ms real) (~ rewrites/second)
        // result Object: < "X" : FSM | state : "red" >
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
                                                                "=> < X : " +
                                                                "FSM | state : \"green\" > .\n" +
                                                                "    rl [turnRed] :  < X : FSM | state : \"green\" > " +
                                                                "=> < X : " +
                                                                "FSM | state : \"red\" > .\n" +
                                                                "\n" +
                                                                "    --- Generated initial config representing the " +
                                                                "start state " +
                                                                "of the FSM.\n" +
                                                                "    op initial : -> Configuration .\n" +
                                                                "    eq initial = < \"X\" : FSM | state : \"red\" > " +
                                                                ".\n" +
                                                                "endm\n" +
                                                                "\n" +
                                                                "rew [10] in FSM-BEHAVIOR-X : initial .\n";


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

        FSMToMaudeTransformer transformer = new FSMToMaudeTransformer(fsm);

        // Transform FSM
        assertThat(transformer.generate(), is(EXPECTED_FOUR_STATE_FSM_MODULE));
        // Maude output
        // rewrite [10] in FSM-BEHAVIOR-trafficLight : initial .
        // rewrites: 11 in 0ms cpu (0ms real) (~ rewrites/second)
        // result Object: < "trafficLight" : FSM | state : "green" >
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
                                                                 "rew [10] in FSM-BEHAVIOR-trafficLight : initial .\n";
}