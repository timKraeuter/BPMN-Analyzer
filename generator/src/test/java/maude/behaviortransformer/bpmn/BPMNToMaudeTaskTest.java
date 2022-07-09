package maude.behaviortransformer.bpmn;

import behavior.bpmn.BPMNCollaboration;
import behavior.bpmn.reader.BPMNFileReaderTestHelper;
import org.junit.jupiter.api.Test;

import static groove.behaviortransformer.bpmn.BPMNToGrooveTestBase.BPMN_BPMN_MODELS_SEMANTICS_TEST_FOLDER;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class BPMNToMaudeTaskTest implements BPMNFileReaderTestHelper {

    /**
     * See test case <a href="https://cawemo.com/share/e9bca9c5-c750-487f-becf-737bbd6ea19b">"Sequential Tasks"</a>
     * in cawemo.
     */
    @Test
    void testSequentialTasks() {
        BPMNCollaboration collaboration = readModelFromResourceFolder("sequential-activities.bpmn");

        BPMNToMaudeTransformer transformer = new BPMNToMaudeTransformer(collaboration);

        String maudeModule = transformer.generate("<> True");

        assertThat(maudeModule, is(SEQUENTIAL_TASK_MODULE));
    }

    private BPMNCollaboration readModelFromResourceFolder(String resourceFileName) {
        String resourcePath = BPMN_BPMN_MODELS_SEMANTICS_TEST_FOLDER + resourceFileName;
        return readModelFromResource(resourcePath);
    }

    public static final String SEQUENTIAL_TASK_MODULE = "load model-checker.maude .\n" +
                                                        "\n" +
                                                        "--- Multiset implementation could be extracted as well.\n" +
                                                        "fmod MSET is pr\n" +
                                                        "    STRING .\n" +
                                                        "    sorts NeMSet MSet .\n" +
                                                        "    subsort String < NeMSet < MSet .\n" +
                                                        "\n" +
                                                        "    op none : -> MSet [ctor] .\n" +
                                                        "    op __ : MSet MSet -> MSet [ctor assoc comm id: none] .\n" +
                                                        "    op __ : NeMSet MSet -> NeMSet [ctor ditto] .\n" +
                                                        "    op __ : MSet NeMSet -> NeMSet [ctor ditto] .\n" +
                                                        "endfm\n" +
                                                        "\n" +
                                                        "mod BPMN-EXECUTION is\n" +
                                                        "    pr MSET .\n" +
                                                        "    pr STRING .\n" +
                                                        "    pr CONFIGURATION .\n" +
                                                        "\n" +
                                                        "    sort ProcessState .\n" +
                                                        "\n" +
                                                        "    ops Running, Terminated : -> ProcessState [ctor] .\n" +
                                                        "    op tokens :_ : MSet -> Attribute [ctor].\n" +
                                                        "    op subprocesses :_ : Configuration -> Attribute [ctor]" +
                                                        ".\n" +
                                                        "    op state :_ : ProcessState -> Attribute [ctor].\n" +
                                                        "    op ProcessSnapshot : -> Cid [ctor] .\n" +
                                                        "    subsort String < Oid .\n" +
                                                        "\n" +
                                                        "    var P : String .\n" +
                                                        "\n" +
                                                        "    rl [terminateProcess] :\n" +
                                                        "    < P : ProcessSnapshot | tokens : none, subprocesses : " +
                                                        "none, state : Running >\n" +
                                                        "                            =>\n" +
                                                        "    < P : ProcessSnapshot | tokens : none, subprocesses : " +
                                                        "none, state : Terminated > .\n" +
                                                        "endm\n" +
                                                        "\n" +
                                                        "mod BPMN-EXECUTION-sequential-activities is\n" +
                                                        "    pr BPMN-EXECUTION .\n" +
                                                        "\n" +
                                                        "    var T : MSet .\n" +
                                                        "    var S : Configuration .\n" +
                                                        "\n" +
                                                        "    --- Generated rules\n" +
                                                        "    rl [start_StartEvent_1] :  < \"sequential-activities\" :" +
                                                        " ProcessSnapshot | tokens : (\"start (StartEvent_1)\" T), " +
                                                        "subprocesses : S, state : Running > => < " +
                                                        "\"sequential-activities\" : ProcessSnapshot | tokens : " +
                                                        "(\"start_A (Flow_1u1u0cf)\" T), subprocesses : S, state : " +
                                                        "Running > .\n" +
                                                        "    rl [A_Activity_1hogwa8_start] :  < " +
                                                        "\"sequential-activities\" : ProcessSnapshot | tokens : " +
                                                        "(\"start_A (Flow_1u1u0cf)\" T), subprocesses : S, state : " +
                                                        "Running > => < \"sequential-activities\" : ProcessSnapshot |" +
                                                        " tokens : (\"A (Activity_1hogwa8)\" T), subprocesses : S, " +
                                                        "state : Running > .\n" +
                                                        "    rl [A_Activity_1hogwa8_end] :  < " +
                                                        "\"sequential-activities\" : ProcessSnapshot | tokens : (\"A " +
                                                        "(Activity_1hogwa8)\" T), subprocesses : S, state : Running >" +
                                                        " => < \"sequential-activities\" : ProcessSnapshot | tokens :" +
                                                        " (\"A_B (Flow_1b2a4a3)\" T), subprocesses : S, state : " +
                                                        "Running > .\n" +
                                                        "    rl [B_Activity_02rpmxb_start] :  < " +
                                                        "\"sequential-activities\" : ProcessSnapshot | tokens : " +
                                                        "(\"A_B (Flow_1b2a4a3)\" T), subprocesses : S, state : " +
                                                        "Running > => < \"sequential-activities\" : ProcessSnapshot |" +
                                                        " tokens : (\"B (Activity_02rpmxb)\" T), subprocesses : S, " +
                                                        "state : Running > .\n" +
                                                        "    rl [B_Activity_02rpmxb_end] :  < " +
                                                        "\"sequential-activities\" : ProcessSnapshot | tokens : (\"B " +
                                                        "(Activity_02rpmxb)\" T), subprocesses : S, state : Running >" +
                                                        " => < \"sequential-activities\" : ProcessSnapshot | tokens :" +
                                                        " (\"B_end (Flow_1tj96s9)\" T), subprocesses : S, state : " +
                                                        "Running > .\n" +
                                                        "    rl [end_Event_1xnuloc] :  < \"sequential-activities\" : " +
                                                        "ProcessSnapshot | tokens : (\"B_end (Flow_1tj96s9)\" T), " +
                                                        "subprocesses : S, state : Running > => < " +
                                                        "\"sequential-activities\" : ProcessSnapshot | tokens : (T), " +
                                                        "subprocesses : S, state : Running > .\n" +
                                                        "\n" +
                                                        "    --- Start configuration which would be generated\n" +
                                                        "    op init : -> Configuration .\n" +
                                                        "    eq init = < \"sequential-activities\" : ProcessSnapshot " +
                                                        "| tokens : (\"start (StartEvent_1)\"), subprocesses : none, " +
                                                        "state : Running > .\n" +
                                                        "endm\n" +
                                                        "\n" +
                                                        "rew [10] init .\n";
}
