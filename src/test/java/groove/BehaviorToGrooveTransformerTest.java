package groove;

import behavior.fsm.FiniteStateMachine;
import behavior.fsm.State;
import behavior.fsm.Transition;
import org.junit.jupiter.api.Test;

import java.io.File;

class BehaviorToGrooveTransformerTest {
    private static final String groovePath = "C:/Source/groove/bin";

    @Test
    void testFSMGeneration() {
        State start = new State("start");
        FiniteStateMachine fsm = new FiniteStateMachine("abc", start);
        State s1 = new State("s1");
        State s2 = new State("s2");
        State s3 = new State("s3");
        fsm.addTransition(new Transition("a", start, s1));
        fsm.addTransition(new Transition("b", s1, s2));
        fsm.addTransition(new Transition("c", s2, s3));

        BehaviorToGrooveTransformer transformer = new BehaviorToGrooveTransformer();
        transformer.generateGrooveGrammar(fsm, new File(groovePath));
    }
}