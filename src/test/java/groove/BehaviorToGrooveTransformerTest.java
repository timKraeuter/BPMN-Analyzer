package groove;

import behavior.bpmn.*;
import behavior.bpmn.auxiliary.BPMNProcessBuilder;
import behavior.fsm.FiniteStateMachine;
import behavior.fsm.State;
import behavior.fsm.Transition;
import behavior.petriNet.PetriNet;
import behavior.petriNet.Place;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import util.FileTestHelper;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

class BehaviorToGrooveTransformerTest {
    //    private static final String outputPath = "C:/Source/groove/bin";
//        private static final String outputPath = "B:/Research/groove/bin";
    private static final String outputPath = FileUtils.getTempDirectoryPath();

    @BeforeEach
    void setUp() {
        GrooveNode.idCounter.set(-1);
    }

    @Test
    void testFSMGenerationABC() throws IOException {
        State start = new State("start");
        FiniteStateMachine fsm = new FiniteStateMachine("abc", start);
        State s1 = new State("s1");
        State s2 = new State("s2");
        State s3 = new State("s3");
        fsm.addTransition(new Transition("a", start, s1));
        fsm.addTransition(new Transition("b", s1, s2));
        fsm.addTransition(new Transition("c", s2, s3));

        BehaviorToGrooveTransformer transformer = new BehaviorToGrooveTransformer();
        File outputDir = new File(outputPath);
        transformer.generateGrooveGrammar(fsm, outputDir);

        // assert
        File expectedDir = new File(this.getClass().getResource("/abc.gps").getFile());
        FileTestHelper.testDirEquals(
                expectedDir,
                new File(outputDir + "/abc.gps"),
                fileName -> fileName.equals("system.properties")); // Ignore the system.properties file because it contains a timestamp and a dir.

        File propertiesFile = new File(this.getClass().getResource("/abc.gps/system.properties").getFile());
        checkPropertiesFile(propertiesFile);
    }

    @Test
    void testFSMGenerationResources() throws IOException {
        State start = new State("start");
        FiniteStateMachine fsm = new FiniteStateMachine("2_Resource_Process", start);
        State r1 = new State("r1");
        State work = new State("work");
        State r2_released = new State("r2_released");
        State end = new State("end");
        fsm.addTransition(new Transition("acquire_r1", start, r1));
        fsm.addTransition(new Transition("acquire_r2", r1, work));
        fsm.addTransition(new Transition("release_r2", work, r2_released));
        fsm.addTransition(new Transition("release_r1", r2_released, end));

        BehaviorToGrooveTransformer transformer = new BehaviorToGrooveTransformer();
        File outputDir = new File(outputPath);
        transformer.generateGrooveGrammar(fsm, outputDir);

        // assert
        File expectedDir = new File(this.getClass().getResource("/2_Resource_Process.gps").getFile());
        FileTestHelper.testDirEquals(
                expectedDir,
                new File(outputDir + "/2_Resource_Process.gps"),
                fileName -> fileName.equals("system.properties")); // Ignore the system.properties file because it contains a timestamp and a dir.

        File propertiesFile = new File(this.getClass().getResource("/2_Resource_Process.gps/system.properties").getFile());
        checkPropertiesFile(propertiesFile);
    }

    @Test
    void testPNGenerationResources() throws IOException {
        PetriNet pn = new PetriNet("pn");
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
        BehaviorToGrooveTransformer transformer = new BehaviorToGrooveTransformer();
        File outputDir = new File(outputPath);
        transformer.generateGrooveGrammar(pn, outputDir);

        // assert
        File expectedDir = new File(this.getClass().getResource("/pn.gps").getFile());
        FileTestHelper.testDirEquals(
                expectedDir,
                new File(outputDir + "/pn.gps"),
                fileName -> fileName.equals("system.properties")); // Ignore the system.properties file because it contains a timestamp and a dir.

        File propertiesFile = new File(this.getClass().getResource("/pn.gps/system.properties").getFile());
        checkPropertiesFile(propertiesFile);
    }

    @Test
    void testBPMNGenerationResources() throws IOException {
        // Build the process model from the NWPT example.
        final StartEvent start = new StartEvent("start");
        final EndEvent end = new EndEvent("end");
        Activity a0 = new Activity("a0");
        Activity a1 = new Activity("a1");
        final AlternativeGateway split = new AlternativeGateway("split");
        Activity a2_1 = new Activity("a2_1");
        Activity a2_2 = new Activity("a2_2");
        final AlternativeGateway merge = new AlternativeGateway("merge");
        Activity a3 = new Activity("a3");

        final BPMNProcessModel processModel = new BPMNProcessBuilder()
                .name("subwork.gps")
                .startEvent(start)
                .sequenceFlow(start, a0)
                .sequenceFlow(a0, a1)
                .sequenceFlow(a1, split)
                .sequenceFlow(split, a2_1)
                .sequenceFlow(split, a2_2)
                .sequenceFlow(a2_1, merge)
                .sequenceFlow(a2_2, merge)
                .sequenceFlow(merge, a3)
                .sequenceFlow(merge, end)
                .endEvent(end)
                .build();

        BehaviorToGrooveTransformer transformer = new BehaviorToGrooveTransformer();
        File outputDir = new File(outputPath);
        transformer.generateGrooveGrammar(processModel, outputDir);

        // assert
        File expectedDir = new File(this.getClass().getResource("/subwork.gps").getFile());
        FileTestHelper.testDirEquals(
                expectedDir,
                new File(outputDir + "/subwork.gps"),
                fileName -> fileName.equals("system.properties")); // Ignore the system.properties file because it contains a timestamp and a dir.

        File propertiesFile = new File(this.getClass().getResource("/subwork.gps/system.properties").getFile());
        checkPropertiesFile(propertiesFile);
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