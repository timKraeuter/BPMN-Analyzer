package no.tk.maude.behaviortransformer.fsm;

import com.google.common.collect.ImmutableSet;
import java.util.Set;
import no.tk.behavior.fsm.FSMStateAtomicProposition;
import no.tk.behavior.fsm.FiniteStateMachine;
import no.tk.behavior.fsm.State;
import no.tk.behavior.fsm.Transition;
import no.tk.maude.behaviortransformer.MaudeTestHelper;
import org.junit.jupiter.api.Test;

class FSMToMaudeTransformerTest implements MaudeTestHelper {

  @Test
  void generatePedestrianTrafficLightFSMTest() throws Exception {
    // Create FSM
    State red = new State("red");
    FiniteStateMachine fsm = new FiniteStateMachine("pedestrianTrafficLight", red);
    State green = new State("green");
    fsm.addTransition(new Transition("turnGreen", red, green));
    fsm.addTransition(new Transition("turnRed", green, red));

    Set<FSMStateAtomicProposition> props =
        ImmutableSet.of(new FSMStateAtomicProposition(red), new FSMStateAtomicProposition(green));
    testFSMMaudeGeneration(fsm, props, "<> green(\"1\")");
    // Maude output:
    // reduce in FSM-CHECK : modelCheck(initial, <> green("X")) .
    // rewrites: 10 in 0ms cpu (0ms real) (~ rewrites/second)
    // result Bool: true
  }

  @Test
  void generateCarTrafficLightFSMTest() throws Exception {
    // Create FSM
    State red = new State("red");
    String fsmName = "carTrafficLight";
    FiniteStateMachine fsm = new FiniteStateMachine(fsmName, red);
    State red_amber = new State("red-amber");
    State green = new State("green");
    State amber = new State("amber");
    fsm.addTransition(new Transition("turn_red_amber", red, red_amber));
    fsm.addTransition(new Transition("turn_green", red_amber, green));
    fsm.addTransition(new Transition("turn_amber", green, amber));
    fsm.addTransition(new Transition("turn_red", amber, red));

    Set<FSMStateAtomicProposition> props =
        ImmutableSet.of(
            new FSMStateAtomicProposition("red", red), new FSMStateAtomicProposition(green));

    testFSMMaudeGeneration(fsm, props, "<> red(\"1\")");
    // Maude output:
    // reduce in FSM-CHECK : modelCheck(initial, <> red("1")) .
    // rewrites: 8 in 0ms cpu (0ms real) (~ rewrites/second)
    // result Bool: true
  }
}
