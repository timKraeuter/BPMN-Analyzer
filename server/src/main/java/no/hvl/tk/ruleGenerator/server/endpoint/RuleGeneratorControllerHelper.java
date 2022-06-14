package no.hvl.tk.ruleGenerator.server.endpoint;

import behavior.bpmn.BPMNCollaboration;
import behavior.bpmn.reader.BPMNFileReader;
import groove.behaviorTransformer.BehaviorToGrooveTransformer;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class RuleGeneratorControllerHelper {
    public static final String GRAPH_GRAMMAR_TEMP_DIR = getTempDir() +
                                                        "bpmnAnalyzerGraphGrammars/";
    public static final String STATE_SPACE_TEMP_DIR = getTempDir() + "bpmnAnalyzerStateSpaces/";

    private static String getTempDir() {
        String tempDir = System.getProperty("java.io.tmpdir");
        if (tempDir.endsWith(File.separator)) {
            return tempDir;
        }
        return tempDir + File.separator;
    }


    private RuleGeneratorControllerHelper() {
    }

    public static File deleteOldGGsAndCreateNewDir() throws IOException {
        FileUtils.deleteDirectory(new File(GRAPH_GRAMMAR_TEMP_DIR));
        FileUtils.deleteDirectory(new File(STATE_SPACE_TEMP_DIR));
        return new File(GRAPH_GRAMMAR_TEMP_DIR + UUID.randomUUID() + File.separator);
    }

    public static Pair<File, BPMNCollaboration> generateGGForBPMNFile(MultipartFile file) throws IOException {
        // Sanitize input.
        BPMNFileReader bpmnFileReader = new BPMNFileReader(name -> name.replaceAll("[\\\\/:*?\"<>|]",
                                                                                   "") // Remove unallowed characters
                                                                       // for windows filenames.
                                                                       .replaceAll("\\s+",
                                                                                   "_")); // Replace whitespaces with _
        BPMNCollaboration bpmnCollaboration = bpmnFileReader.readModelFromStream(file.getInputStream());

        BehaviorToGrooveTransformer transformer = new BehaviorToGrooveTransformer();
        final File grooveGrammarFolder = transformer.generateGrooveGrammar(bpmnCollaboration,
                                                                           new File(GRAPH_GRAMMAR_TEMP_DIR),
                                                                           false);

        return Pair.of(grooveGrammarFolder, bpmnCollaboration);
    }
}
