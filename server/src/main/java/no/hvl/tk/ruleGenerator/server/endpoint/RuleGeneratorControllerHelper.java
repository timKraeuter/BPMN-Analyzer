package no.hvl.tk.ruleGenerator.server.endpoint;

import behavior.bpmn.BPMNCollaboration;
import behavior.bpmn.reader.BPMNFileReader;
import groove.behaviorTransformer.BehaviorToGrooveTransformer;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class RuleGeneratorControllerHelper {
    public static final String graphGrammarTempDir = System.getProperty("java.io.tmpdir") +
                                                     "bpmnAnalyzerGraphGrammars/";
    public static final String stateSpaceTempDir = System.getProperty("java.io.tmpdir") + "bpmnAnalyzerStateSpaces/";

    private RuleGeneratorControllerHelper() {
    }

    public static File deleteOldGGsAndCreateNewDir() throws IOException {
        FileUtils.deleteDirectory(new File(graphGrammarTempDir));
        FileUtils.deleteDirectory(new File(stateSpaceTempDir));
        return new File(graphGrammarTempDir + UUID.randomUUID() + "/");
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
                                                                     new File(graphGrammarTempDir),
                                                                     false);

        return Pair.of(grooveGrammarFolder, bpmnCollaboration);
    }
}
