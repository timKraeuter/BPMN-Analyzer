package no.hvl.tk.rulegenerator.server.endpoint;

import behavior.bpmn.BPMNCollaboration;
import behavior.bpmn.reader.BPMNFileReader;
import groove.behaviortransformer.BehaviorToGrooveTransformer;
import org.apache.commons.lang3.StringUtils;
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
        BPMNFileReader bpmnFileReader =
                new BPMNFileReader(RuleGeneratorControllerHelper::transformToQualifiedGrooveNameIfNeeded);
        BPMNCollaboration bpmnCollaboration = bpmnFileReader.readModelFromStream(file.getInputStream());

        BehaviorToGrooveTransformer transformer = new BehaviorToGrooveTransformer();
        final File grooveGrammarFolder = transformer.generateGrooveGrammar(bpmnCollaboration,
                                                                           new File(GRAPH_GRAMMAR_TEMP_DIR),
                                                                           false);

        return Pair.of(grooveGrammarFolder, bpmnCollaboration);
    }

    private static String transformToQualifiedGrooveNameIfNeeded(String name) {
        String transformedName = name.replaceAll("[\\\\/:*?\"<>|]",
                                                 "") // Remove unallowed characters for windows filenames.
                                     //
                                     .replaceAll("\u00a0", "_") // Replace non-breaking whitespaces with _
                                     .replaceAll("\\s+", "_"); // Replace whitespaces with _
        if (!transformedName.isEmpty() && Character.isDigit(transformedName.charAt(0))) {
            // Prefix the name with a number to make it a qualified name in Groove.
            return "_" + transformedName;
        }
        return transformedName;
    }
}
