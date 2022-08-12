package maude.behaviortransformer;

import behavior.bpmn.reader.BPMNFileReaderTestHelper;
import maude.behaviortransformer.bpmn.BPMNToMaudeTransformer;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public interface BPMNMaudeTestHelper extends BPMNFileReaderTestHelper {

    String MAUDE_MODULE_FOLDER = "/bpmn/maude/";
    boolean REPLACE_EXPECTED_FILE_WITH_ACTUAL = true;
    String WILL_ALWAYS_TERMINATE_QUERY = "red modelCheck(init,  <> [] allTerminated)";
    String CAN_TERMINATE_QUERY = "search init =>! X such that X |= allTerminated = true";

    default void testBPMNMaudeGeneration(String resourceFileName) throws IOException {
        testBPMNMaudeGenerationWithCustomQuery(resourceFileName, WILL_ALWAYS_TERMINATE_QUERY);
    }

    default void testBPMNMaudeGenerationWithCustomQuery(String resourceFileName, String finalQuery) throws IOException {
        BPMNToMaudeTransformer transformer = new BPMNToMaudeTransformer(readModelFromResourceFolder(resourceFileName +
                                                                                                    ".bpmn"));
        String actualMaudeModule = transformer.generate(finalQuery);

        String expectedMaudeModule = readExpectedMaudeModule(resourceFileName);
        if (REPLACE_EXPECTED_FILE_WITH_ACTUAL) {
            replaceWithActualIfNeeded(resourceFileName, actualMaudeModule, expectedMaudeModule);
        }
        assertThat(actualMaudeModule, actualMaudeModule, is(expectedMaudeModule));
    }

    private void replaceWithActualIfNeeded(
            String resourceFileName,
            String actualMaudeModule,
            String expectedMaudeModule) throws IOException {
        String expectedFileFilePath = "src/test/resources/" + MAUDE_MODULE_FOLDER + resourceFileName + ".maude";
        if (!actualMaudeModule.equals(expectedMaudeModule)) {
            // Only replace if reduced to true. Run model!
            FileUtils.writeStringToFile(new File(expectedFileFilePath),
                                        actualMaudeModule,
                                        Charset.defaultCharset());
            System.out.println("Replaced module with actual!");
        }
    }

    default String readExpectedMaudeModule(String resourceFileName) throws IOException {
        File maudeModel = getMaudeModuleFile(resourceFileName);
        return FileUtils.readFileToString(maudeModel, StandardCharsets.UTF_8).replaceAll("\r?\n",
                                                                                         "\r\n");
        // force identical line separators;
    }

    private File getMaudeModuleFile(String resourceFileName) {
        String resourcePath = MAUDE_MODULE_FOLDER + resourceFileName + ".maude";

        @SuppressWarnings("ConstantConditions") File maudeModel =
                new File(this.getClass().getResource(resourcePath).getFile());
        return maudeModel;
    }
}
