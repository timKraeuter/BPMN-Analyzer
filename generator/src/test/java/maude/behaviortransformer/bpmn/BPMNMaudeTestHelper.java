package maude.behaviortransformer.bpmn;

import behavior.bpmn.reader.BPMNFileReaderTestHelper;
import maude.behaviortransformer.MaudeTestHelper;
import maude.behaviortransformer.bpmn.settings.MaudeBPMNGenerationSettings;
import maude.behaviortransformer.bpmn.settings.MessagePersistence;

import java.io.IOException;
import java.util.function.UnaryOperator;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public interface BPMNMaudeTestHelper extends BPMNFileReaderTestHelper, MaudeTestHelper {

    String MAUDE_MODULE_FOLDER = "/bpmn/maude/";
    boolean REPLACE_EXPECTED_FILE_WITH_ACTUAL = false;
    String WILL_ALWAYS_TERMINATE_QUERY = "red modelCheck(init,  <> [] allTerminated)";
    String CAN_TERMINATE_QUERY = "search init =>! X such that X |= allTerminated = true";
    String DOT_BPMN = ".bpmn";

    default void testBPMNMaudeGeneration(String resourceFileName) throws IOException {
        testBPMNMaudeGeneration(resourceFileName, WILL_ALWAYS_TERMINATE_QUERY);
    }

    default void testBPMNMaudeGeneration(String resourceFileName,
                                         String finalQuery) throws IOException {
        testBPMNMaudeGeneration(resourceFileName, finalQuery, name -> name);
    }

    default void testBPMNMaudeGeneration(String resourceFileName,
                                         String finalQuery,
                                         UnaryOperator<String> elementNameTransformer) throws IOException {
        testBPMNMaudeGeneration(resourceFileName,
                                finalQuery,
                                elementNameTransformer,
                                new MaudeBPMNGenerationSettings(MessagePersistence.PERSISTENT));
    }

    default void testBPMNMaudeGeneration(String resourceFileName,
                                         String finalQuery,
                                         UnaryOperator<String> elementNameTransformer,
                                         MaudeBPMNGenerationSettings settings) throws IOException {
        BPMNToMaudeTransformer transformer = new BPMNToMaudeTransformer(readModelFromResourceFolder(resourceFileName +
                                                                                                    DOT_BPMN,
                                                                                                    elementNameTransformer),
                                                                        settings);
        String actualMaudeModule = transformer.generate(finalQuery);

        String expectedMaudeModule = readExpectedMaudeModule(MAUDE_MODULE_FOLDER, resourceFileName);
        if (REPLACE_EXPECTED_FILE_WITH_ACTUAL) {
            replaceWithActualIfNeeded(MAUDE_MODULE_FOLDER, resourceFileName, actualMaudeModule, expectedMaudeModule);
        }
        assertThat(actualMaudeModule, is(expectedMaudeModule));
    }
}
