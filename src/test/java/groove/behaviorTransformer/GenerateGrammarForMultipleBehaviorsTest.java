package groove.behaviorTransformer;

import behavior.fsm.FiniteStateMachine;
import behavior.fsm.State;
import behavior.fsm.Transition;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.*;
import java.util.stream.Stream;

public class GenerateGrammarForMultipleBehaviorsTest {
    private static final String outputPath = "C:/Source/groove/bin";
    public static final String SW_TO_PHASE_2 = "switch_to_phase2";
    public static final String SW_TO_PHASE_1 = "switch_to_phase1";
    public static final String TURN_RED = "turn_red";
    public static final String TURN_GREEN = "turn_green";
    //    private static final String outputPath = "B:/Source/groove/bin";
    //    String outputPath = FileUtils.getTempDirectoryPath();

    @Test
    void tlTest() {
        FiniteStateMachine tl_a = this.createTrafficLight("A", "green");
        FiniteStateMachine tl_b = this.createTrafficLight("B", "red");
        FiniteStateMachine tl_c = this.createTrafficLight("C", "green");

        State phase1 = new State("phase1");
        State phase2 = new State("phase2");
        FiniteStateMachine phases = new FiniteStateMachine("phases", phase1);
        phases.addTransition(new Transition(SW_TO_PHASE_2, phase1, phase2));
        phases.addTransition(new Transition(SW_TO_PHASE_1, phase2, phase1));

        BehaviorToGrooveTransformer transformer = new BehaviorToGrooveTransformer();
        File outputDir = new File(outputPath);

        Map<String, Set<String>> nameToToBeSynchedRules = new LinkedHashMap<>();

        Set<String> toBeSynched1 = new LinkedHashSet<>(); // Fixed iteration order needed for the testcase.
        toBeSynched1.add(String.format("%s_%s", phases.getName(), SW_TO_PHASE_1));
        toBeSynched1.add(String.format("%s_%s", tl_a.getName(), TURN_GREEN));
        toBeSynched1.add(String.format("%s_%s", tl_b.getName(), TURN_RED));
        toBeSynched1.add(String.format("%s_%s", tl_c.getName(), TURN_GREEN));

        nameToToBeSynchedRules.put(SW_TO_PHASE_1, toBeSynched1);

        Set<String> toBeSynched2 = new LinkedHashSet<>(); // Fixed iteration order needed for the testcase.
        toBeSynched2.add(String.format("%s_%s", phases.getName(), SW_TO_PHASE_2));
        toBeSynched2.add(String.format("%s_%s", tl_a.getName(), TURN_RED));
        toBeSynched2.add(String.format("%s_%s", tl_b.getName(), TURN_GREEN));
        toBeSynched2.add(String.format("%s_%s", tl_c.getName(), TURN_RED));

        nameToToBeSynchedRules.put(SW_TO_PHASE_2, toBeSynched2);

        transformer.generateGrooveGrammar(
                outputDir,
                "trafficLightsTest",
                nameToToBeSynchedRules,
                tl_a,
                tl_b,
                tl_c,
                phases);
        // Expect a folder with prefixed rules and start states etc.
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
        fsm.addTransition(new Transition(TURN_GREEN, red_amber, green));
        fsm.addTransition(new Transition("turn_amber", green, amber));
        fsm.addTransition(new Transition(TURN_RED, amber, red));
        return fsm;
    }
}
