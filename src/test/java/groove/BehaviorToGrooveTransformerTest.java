package groove;

import behavior.Behavior;
import behavior.bpmn.*;
import behavior.bpmn.auxiliary.BPMNProcessBuilder;
import behavior.fsm.FiniteStateMachine;
import behavior.fsm.State;
import behavior.fsm.Transition;
import behavior.petriNet.PetriNet;
import behavior.petriNet.Place;
import groove.graph.GrooveNode;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import util.FileTestHelper;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

class BehaviorToGrooveTransformerTest {
        private static final String outputPath = "C:/Source/groove/bin";
//    private static final String outputPath = "B:/Source/groove/bin";
//    private static final String outputPath = FileUtils.getTempDirectoryPath();

    @BeforeEach
    void setUp() {
        GrooveNode.idCounter.set(-1);
    }

    @Test
    void testFSMGenerationABC() throws IOException {
        State start = new State("start");
        String fsmName = "abc";
        FiniteStateMachine fsm = new FiniteStateMachine(fsmName, start);
        State s1 = new State("s1");
        State s2 = new State("s2");
        State s3 = new State("s3");
        fsm.addTransition(new Transition("a", start, s1));
        fsm.addTransition(new Transition("b", s1, s2));
        fsm.addTransition(new Transition("c", s2, s3));

        this.checkGrooveGeneration(fsmName, fsm);
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

        this.checkGrooveGeneration(fsmName, fsm);
    }

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
        this.checkGrooveGeneration(petriNetName, pn);
    }

    /**
     * See model [SEQ] in bpmn_models/models.png
     */
    @Test
    void testBPMNTwoActivityGenerationResources() throws IOException {
        // Build the process model from the NWPT example.
        final StartEvent start = new StartEvent("start");
        Activity a0 = new Activity("a0");
        Activity a1 = new Activity("a1");
        final EndEvent end = new EndEvent("end");

        final String modelName = "twoActivity";
        final BPMNProcessModel processModel = new BPMNProcessBuilder()
                .name(modelName)
                .startEvent(start)
                .sequenceFlow("start", start, a0)
                .sequenceFlow("a0", a0, a1)
                .sequenceFlow("a1", a1, end)
                .endEvent(end)
                .build();

        this.checkGrooveGeneration(modelName, processModel);
    }

    /**
     * See model in bpmn_models/exclusive_parallel_BPMN.pdf
     */
    @Test
    void testBPMNExclusiveGatewayGenerationResources() throws IOException {
        // Build the process model from the NWPT example.
        final StartEvent start = new StartEvent("start");
        final EndEvent end = new EndEvent("end");
        Activity a0 = new Activity("a0");
        Activity a1 = new Activity("a1");
        final ExclusiveGateway e1 = new ExclusiveGateway("e1");
        Activity a2_1 = new Activity("a2_1");
        Activity a2_2 = new Activity("a2_2");
        final ExclusiveGateway e2 = new ExclusiveGateway("e2");
        Activity a3 = new Activity("a3");

        final String modelName = "exclusive";
        final BPMNProcessModel processModel = new BPMNProcessBuilder()
                .name(modelName)
                .startEvent(start)
                .sequenceFlow("start", start, a0)
                .sequenceFlow("a0", a0, a1)
                .sequenceFlow("a1", a1, e1)
                .sequenceFlow("e1_a2_1", e1, a2_1)
                .sequenceFlow("e1_a2_2", e1, a2_2)
                .sequenceFlow("a2_1", a2_1, e2)
                .sequenceFlow("a2_2", a2_2, e2)
                .sequenceFlow("e2", e2, a3)
                .sequenceFlow("a3", a3, end)
                .endEvent(end)
                .build();

        this.checkGrooveGeneration(modelName, processModel);
    }

    /**
     * See model in bpmn_models/exclusive_parallel_BPMN.pdf
     */
    @Test
    void testBPMNParallelGatewayGenerationResources() throws IOException {
        final StartEvent start = new StartEvent("start");
        final EndEvent end = new EndEvent("end");
        Activity a0 = new Activity("a0");
        Activity a1 = new Activity("a1");
        final ParallelGateway p1 = new ParallelGateway("p1");
        Activity a2_1 = new Activity("a2_1");
        Activity a2_2 = new Activity("a2_2");
        final ParallelGateway p2 = new ParallelGateway("p2");
        Activity a3 = new Activity("a3");

        final String modelName = "parallel";
        final BPMNProcessModel processModel = new BPMNProcessBuilder()
                .name(modelName)
                .startEvent(start)
                .sequenceFlow("start", start, a0)
                .sequenceFlow("a0", a0, a1)
                .sequenceFlow("a1", a1, p1)
                .sequenceFlow("p1_a2_1", p1, a2_1)
                .sequenceFlow("p1_a2_2", p1, a2_2)
                .sequenceFlow("a2_1", a2_1, p2)
                .sequenceFlow("a2_2", a2_2, p2)
                .sequenceFlow("p2", p2, a3)
                .sequenceFlow("a3", a3, end)
                .endEvent(end)
                .build();

        this.checkGrooveGeneration(modelName, processModel);
    }

    /**
     * See model [CYC] in bpmn_models/models.png (without data).
     */
    @Test
    void testBPMNCyclicGenerationResources() throws IOException {
        final StartEvent start = new StartEvent("start");
        final EndEvent end = new EndEvent("end");
        Activity a0 = new Activity("a0");
        final ExclusiveGateway e1 = new ExclusiveGateway("e1");
        Activity a1 = new Activity("a1");
        Activity a2 = new Activity("a2");
        Activity a3 = new Activity("a3");
        final ExclusiveGateway e2 = new ExclusiveGateway("e2");
        final ExclusiveGateway e3 = new ExclusiveGateway("e3");
        final ExclusiveGateway e4 = new ExclusiveGateway("e4");

        final String modelName = "cyclic";
        final BPMNProcessModel processModel = new BPMNProcessBuilder()
                .name(modelName)
                .startEvent(start)
                .sequenceFlow("start", start, a0)
                .sequenceFlow("a0", a0, e1)
                .sequenceFlow("e1_a1", e1, a1)
                .sequenceFlow("e1_a2", e1, a2)
                .sequenceFlow("a1", a1, e2)
                .sequenceFlow("a2", a2, e3)
                .sequenceFlow("e2", e2, a3)
                .sequenceFlow("a3", a3, e3)
                .sequenceFlow("e3", e3, e4)
                .sequenceFlow("e4_e2", e4, e2)
                .sequenceFlow("e4_end", e4, end)
                .endEvent(end)
                .build();

        this.checkGrooveGeneration(modelName, processModel);
    }

    /**
     * See model [EXT] in bpmn_models/models.png.
     */
    @Test
    void testBPMNTwoEndEventsGenerationResources() throws IOException {
        final StartEvent start = new StartEvent("start");
        final ParallelGateway p1 = new ParallelGateway("p1");
        Activity a1 = new Activity("a1");
        Activity a2 = new Activity("a2");
        final EndEvent end1 = new EndEvent("end1");
        final EndEvent end2 = new EndEvent("end2");

        final String modelName = "twoEndEvents";
        final BPMNProcessModel processModel = new BPMNProcessBuilder()
                .name(modelName)
                .startEvent(start)
                .sequenceFlow("start", start, p1)
                .sequenceFlow("p1", p1, a1)
                .sequenceFlow("p1", p1, a2)
                .sequenceFlow("a1", a1, end1)
                .sequenceFlow("a2", a2, end2)
                .endEvent(end1)
                .endEvent(end2)
                .build();

        this.checkGrooveGeneration(modelName, processModel);
    }

    @SuppressWarnings("ConstantConditions")
    private void checkGrooveGeneration(String modelName, Behavior behavior) throws IOException {
        BehaviorToGrooveTransformer transformer = new BehaviorToGrooveTransformer();
        File outputDir = new File(outputPath);
        transformer.generateGrooveGrammar(behavior, outputDir);

        // assert
        File expectedDir = new File(this.getClass().getResource("/" + modelName + ".gps").getFile());
        FileTestHelper.testDirEquals(
                expectedDir,
                new File(outputDir + "/" + modelName + ".gps"),
                fileName -> fileName.equals("system.properties")); // Ignore the system.properties file because it contains a timestamp and a dir.

        File propertiesFile = new File(this.getClass().getResource("/" + modelName + ".gps/system.properties").getFile());
        this.checkPropertiesFile(propertiesFile);
    }

    private void checkPropertiesFile(File propertiesFile) throws IOException {
        Assertions.assertTrue(
                FileUtils.readFileToString(propertiesFile, StandardCharsets.UTF_8)
                         .replaceAll("\r?\n", "\r\n") // force identical line separators
                         .endsWith(
                                 "startGraph=start\r\n" +
                                         "grooveVersion=5.8.1\r\n" +
                                         "grammarVersion=3.7"));
    }
}