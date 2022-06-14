package groove.behaviortransformer;

import behavior.fsm.FiniteStateMachine;
import behavior.fsm.State;
import behavior.fsm.Transition;
import org.junit.jupiter.api.Test;

import java.io.IOException;

class FSMToGrooveTransformerTest extends BehaviorToGrooveTransformerTestHelper {

    static final String TYPE_GRAPH_FILE_NAME = "fsm_e_model.gty";

    @Override
    protected void setUpFurther() {
        // Default is to ignore the type graph.
        this.setFileNameFilter(x -> x.equals(TYPE_GRAPH_FILE_NAME));
    }

    @Override
    public String getOutputPathSubFolderName() {
        return "fsm";
    }

    @Test
    void testFSMGenerationABC() throws IOException {
        setFileNameFilter(s -> false); // We want to check for the type graph in this testcase.

        State start = new State("start");
        String fsmName = "abc";
        FiniteStateMachine fsm = new FiniteStateMachine(fsmName, start);
        State s1 = new State("s1");
        State s2 = new State("s2");
        State s3 = new State("s3");
        fsm.addTransition(new Transition("a", start, s1));
        fsm.addTransition(new Transition("b", s1, s2));
        fsm.addTransition(new Transition("c", s2, s3));

        this.checkGrooveGeneration(fsm);
    }

    @Test
    void testFSMGenerationResources() throws IOException {
        State start = new State("start");
        String fsmName = "2_Resource_Process";
        FiniteStateMachine fsm = new FiniteStateMachine(fsmName, start);
        State r1 = new State("r1");
        State work = new State("work");
        State r2_released = new State("r2_released");
        State end = new State("end");
        fsm.addTransition(new Transition("acquire_r1", start, r1));
        fsm.addTransition(new Transition("acquire_r2", r1, work));
        fsm.addTransition(new Transition("release_r2", work, r2_released));
        fsm.addTransition(new Transition("release_r1", r2_released, end));

        this.checkGrooveGeneration(fsm);
    }

    @Test
    void testFSMGenerationTrafficLight() throws IOException {
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

        this.checkGrooveGeneration(fsm);
    }
}