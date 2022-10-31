package maude.behaviortransformer;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import behavior.fsm.FiniteStateMachine;
import behavior.fsm.State;
import behavior.fsm.StateAtomicProposition;
import behavior.fsm.Transition;
import com.google.common.collect.ImmutableSet;
import java.io.IOException;
import java.util.Set;
import org.junit.jupiter.api.Test;

class FSMToMaudeTransformerTest implements MaudeTestHelper {

  private static final String MAUDE_MODULE_FOLDER = "/fsm/maude/";

  @Test
  void generateTwoStateFSMTest() throws Exception {
    // Create FSM
    State red = new State("red");
    FiniteStateMachine fsm = new FiniteStateMachine("X", red);
    State green = new State("green");
    fsm.addTransition(new Transition("turnGreen", red, green));
    fsm.addTransition(new Transition("turnRed", green, red));

    Set<StateAtomicProposition> props =
        ImmutableSet.of(new StateAtomicProposition(red), new StateAtomicProposition(green));
    testFSMMaudeGeneration(fsm, props, "<> green(\"X\")");
    // Maude output:
    // reduce in FSM-CHECK : modelCheck(initial, <> green("X")) .
    // rewrites: 10 in 0ms cpu (0ms real) (~ rewrites/second)
    // result Bool: true
  }

  private void testFSMMaudeGeneration(
      FiniteStateMachine fsm, Set<StateAtomicProposition> props, String finalQuery)
      throws IOException {
    String actualMaudeModule = new FSMToMaudeTransformer(fsm, props).generate(finalQuery);

    String expectedMaudeModule = readExpectedMaudeModule(MAUDE_MODULE_FOLDER, fsm.getName());
    if (!actualMaudeModule.equals(expectedMaudeModule)) {
      System.out.println(actualMaudeModule);
    }
    assertThat(actualMaudeModule, is(expectedMaudeModule));
  }

  @Test
  void generateFourStateFSMTest() throws Exception {
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

    Set<StateAtomicProposition> props =
        ImmutableSet.of(new StateAtomicProposition("red", red), new StateAtomicProposition(green));

    testFSMMaudeGeneration(fsm, props, "<> red(\"1\")");
    // Maude output:
    // reduce in FSM-CHECK : modelCheck(initial, <> red("1")) .
    // rewrites: 8 in 0ms cpu (0ms real) (~ rewrites/second)
    // result Bool: true
  }
}
