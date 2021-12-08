package groove.behaviorTransformer;

import behavior.fsm.FiniteStateMachine;
import behavior.fsm.State;
import behavior.fsm.Transition;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Optional;
import java.util.stream.Stream;

public class GenerateGrammarForMultipleBehaviorsTest {
    private static final String outputPath = "C:/Source/groove/bin";
    //    private static final String outputPath = "B:/Source/groove/bin";
    //    String outputPath = FileUtils.getTempDirectoryPath();

    @Test
    void tlTest() {
        FiniteStateMachine tl_a = this.createTrafficLight("A", "red");
        FiniteStateMachine tl_b = this.createTrafficLight("B", "red");
        FiniteStateMachine tl_c = this.createTrafficLight("C", "green");

        State phase1 = new State("phase1");
        State phase2 = new State("phase2");
        FiniteStateMachine phases = new FiniteStateMachine("phases", phase1);
        phases.addTransition(new Transition("switch_to_phase2", phase1, phase2));
        phases.addTransition(new Transition("switch_to_phase1", phase2, phase1));
        // Expect a folder with prefixed rules and start states etc.

        BehaviorToGrooveTransformer transformer = new BehaviorToGrooveTransformer();
        File outputDir = new File(outputPath);
        transformer.generateGrooveGrammar(outputDir, "trafficLightsTest", tl_a, tl_b, tl_c, phases);
    }

    private FiniteStateMachine createTrafficLight(String fsmName, String startStateName) {
        State red = new State("red");
        State red_amber = new State("red-amber");
        State green = new State("green");
        State amber = new State("amber");

        Optional<State> startState = Stream.of(red, red_amber, green, amber)
                                           .filter(state -> state.getName().equals(startStateName))
                                           .findFirst();

        if (startState.isEmpty()) {
            throw new RuntimeException();
        }
        FiniteStateMachine fsm = new FiniteStateMachine(fsmName, startState.get());
        fsm.addTransition(new Transition("turn_red_amber", red, red_amber));
        fsm.addTransition(new Transition("turn_green", red_amber, green));
        fsm.addTransition(new Transition("turn_amber", green, amber));
        fsm.addTransition(new Transition("turn_red", amber, red));
        return fsm;
    }
}
