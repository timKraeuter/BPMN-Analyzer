package maude.behaviortransformer;

import behavior.bpmn.reader.BPMNFileReaderTestHelper;
import maude.behaviortransformer.bpmn.BPMNToMaudeTransformer;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public interface BPMNMaudeTestHelper extends BPMNFileReaderTestHelper {

    String MAUDE_MODULE_FOLDER = "/bpmn/maude/";

    default void testBPMNMaudeGeneration(String resourceFileName) throws IOException {
        BPMNToMaudeTransformer transformer = new BPMNToMaudeTransformer(readModelFromResourceFolder(resourceFileName +
                                                                                                    ".bpmn"));
        String maudeModule = transformer.generate("<> True");
        System.out.println(maudeModule);

        assertThat(maudeModule, is(readExpectedMaudeModule(resourceFileName)));
    }

    default String readExpectedMaudeModule(String resourceFileName) throws IOException {
        String resourcePath = MAUDE_MODULE_FOLDER + resourceFileName + ".maude";

        @SuppressWarnings("ConstantConditions") File maudeModel =
                new File(this.getClass().getResource(resourcePath).getFile());
        return FileUtils.readFileToString(maudeModel, StandardCharsets.UTF_8).replaceAll("\r?\n",
                                                                                         "\r\n");
        // force identical line separators;
    }
}
