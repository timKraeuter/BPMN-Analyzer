package groove;

import behavior.fsm.FiniteStateMachine;
import behavior.fsm.State;
import behavior.fsm.Transition;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;

import java.io.File;

class BehaviorToGrooveTransformerTest {
    @Test
    void testFSMGeneration() {
        State start = new State("start");
        FiniteStateMachine fsm = new FiniteStateMachine(start);
        State s1 = new State("s1");
        State s2 = new State("s2");
        State s3 = new State("s3");
        fsm.addTransition(new Transition("a", start, s1));
        fsm.addTransition(new Transition("b", s1, s2));
        fsm.addTransition(new Transition("c", s2, s3));

        File tempDir = new File(FileUtils.getTempDirectory().getPath() + "/fsm/");
        tempDir.mkdir();
        BehaviorToGrooveTransformer transformer = new BehaviorToGrooveTransformer();
        transformer.generateGrooveGrammar(fsm, tempDir);
    }
}