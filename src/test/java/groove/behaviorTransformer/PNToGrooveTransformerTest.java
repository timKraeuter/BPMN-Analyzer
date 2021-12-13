package groove.behaviorTransformer;

import behavior.petriNet.PetriNet;
import behavior.petriNet.Place;
import org.junit.jupiter.api.Test;

import java.io.IOException;

class PNToGrooveTransformerTest implements BehaviorToGrooveTransformerTestHelper {

    @Test
    void testPNGenerationResources() throws IOException {
        String petriNetName = "pn";
        PetriNet pn = new PetriNet(petriNetName);
        // Places
        Place start = new Place("start", 3);
        Place r1_preWork = new Place("r1_preWork");
        Place r2_preWork = new Place("r2_preWork");
        Place r1_postWork = new Place("r1_postWork");
        Place r2_postWork = new Place("r2_postWork");
        Place end = new Place("end");

        // Transitions
        behavior.petriNet.Transition acquire_r1 = new behavior.petriNet.Transition("acquire_r1");
        acquire_r1.addIncomingEdge(start);
        acquire_r1.addOutgoingEdge(r1_preWork);
        behavior.petriNet.Transition acquire_r2 = new behavior.petriNet.Transition("acquire_r2");
        acquire_r2.addIncomingEdge(start);
        acquire_r2.addOutgoingEdge(r2_preWork);
        behavior.petriNet.Transition work_r1 = new behavior.petriNet.Transition("work_r1");
        work_r1.addIncomingEdge(r1_preWork);
        work_r1.addOutgoingEdge(r1_postWork);
        behavior.petriNet.Transition work_r2 = new behavior.petriNet.Transition("work_r2");
        work_r2.addIncomingEdge(r2_preWork);
        work_r2.addOutgoingEdge(r2_postWork);
        behavior.petriNet.Transition release_r1 = new behavior.petriNet.Transition("release_r1");
        release_r1.addIncomingEdge(r1_postWork);
        release_r1.addOutgoingEdge(end);
        behavior.petriNet.Transition release_r2 = new behavior.petriNet.Transition("release_r2");
        release_r2.addIncomingEdge(r2_postWork);
        release_r2.addOutgoingEdge(end);

        pn.addTransition(acquire_r1);
        pn.addTransition(acquire_r2);
        pn.addTransition(work_r1);
        pn.addTransition(work_r2);
        pn.addTransition(release_r1);
        pn.addTransition(release_r2);

        // TODO weird object things in the visual debugger when explorings pairs. (depth 2)
        this.checkGrooveGeneration(pn);
    }
}