package maude.behaviortransformer.bpmn;

import maude.behaviortransformer.BPMNMaudeTestHelper;
import maude.behaviortransformer.bpmn.settings.MaudeBPMNGenerationSettings;
import maude.behaviortransformer.bpmn.settings.MessagePersistence;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.UnaryOperator;

class BPMNToMaudeComplexDiagramsTest implements BPMNMaudeTestHelper {

    /**
     * See test case <a href="https://cawemo.com/share/9b143426-50ed-4621-83af-b30e29273077">"Cyclic"</a> in cawemo.
     */
    @Test
    void testCyclic() throws IOException {
        testBPMNMaudeGeneration("cyclic", CAN_TERMINATE_QUERY);
    }

    /**
     * Use case test.
     * See <a href="https://cawemo.com/share/a19e3ee0-b230-44ef-bfd7-1f3e599c4a59">Use-case-execution</a>.
     */
    @Test
    void testUseCase() throws IOException {
        // Persistent message lead to many possible final states!.
        testBPMNMaudeGeneration(
                "use-case-execution",
                CAN_TERMINATE_QUERY,
                createNameTransformer());
    }

    /**
     * Use case test one bus.
     * See <a href="https://cawemo.com/share/4f36c8b0-346b-43d2-9d38-63966c42b0e3">Use-case-execution - One Bus</a>.
     */
    @Test
    void testUseCaseOneBus() throws IOException {
        testBPMNMaudeGeneration(
                "use-case-execution-one-bus",
                CAN_TERMINATE_QUERY,
                createNameTransformer(),
                new MaudeBPMNGenerationSettings(MessagePersistence.NON_PERSISTENT));
    }

    private UnaryOperator<String> createNameTransformer() {
        Map<String, String> replace = new HashMap<>();
        // Remove line separators.
        replace.put("\r?\n", "_");
        replace.put(" ", "_");
        replace.put("\\(", "");
        replace.put("\\)", "");
        replace.put(",", "");

        return name -> {
            String result = name;
            for (Map.Entry<String, String> replaceEntries : replace.entrySet()) {
                result = result.replaceAll(replaceEntries.getKey(), replaceEntries.getValue());
            }
            return result;
        };
    }
}
