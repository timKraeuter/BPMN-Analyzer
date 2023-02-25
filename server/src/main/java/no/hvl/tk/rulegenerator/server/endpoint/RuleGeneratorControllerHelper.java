package no.hvl.tk.rulegenerator.server.endpoint;

import behavior.bpmn.BPMNCollaboration;
import behavior.bpmn.auxiliary.exceptions.GrooveGenerationRuntimeException;
import behavior.bpmn.reader.BPMNFileReader;
import groove.behaviortransformer.BehaviorToGrooveTransformer;
import groove.behaviortransformer.bpmn.BPMNToGrooveTransformerHelper;
import java.io.File;
import java.io.IOException;
import java.util.UUID;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.springframework.web.multipart.MultipartFile;

public class RuleGeneratorControllerHelper {
  public static final String GRAPH_GRAMMAR_TEMP_DIR = getTempDir() + "bpmnAnalyzerGraphGrammars/";
  public static final String STATE_SPACE_TEMP_DIR = getTempDir() + "bpmnAnalyzerStateSpaces/";

  private static String getTempDir() {
    String tempDir = System.getProperty("java.io.tmpdir");
    if (tempDir.endsWith(File.separator)) {
      return tempDir;
    }
    return tempDir + File.separator;
  }

  private RuleGeneratorControllerHelper() {}

  public static File deleteOldGGsAndCreateNewDir() throws IOException {
    FileUtils.deleteDirectory(new File(GRAPH_GRAMMAR_TEMP_DIR));
    FileUtils.deleteDirectory(new File(STATE_SPACE_TEMP_DIR));
    return new File(GRAPH_GRAMMAR_TEMP_DIR + UUID.randomUUID() + File.separator);
  }

  public static Pair<File, BPMNCollaboration> generateGGForBPMNFile(MultipartFile file)
      throws IOException {
    BPMNFileReader bpmnFileReader =
        new BPMNFileReader(BPMNToGrooveTransformerHelper::transformToQualifiedGrooveNameIfNeeded);
    BPMNCollaboration bpmnCollaboration = bpmnFileReader.readModelFromStream(file.getInputStream());

    final File grooveGrammarFolder = generateGG(bpmnCollaboration);

    return Pair.of(grooveGrammarFolder, bpmnCollaboration);
  }

  private static File generateGG(BPMNCollaboration bpmnCollaboration) {
    BehaviorToGrooveTransformer transformer = new BehaviorToGrooveTransformer();
    File grooveGrammarFolder;
    try {

      grooveGrammarFolder =
          transformer.generateGrooveGrammarForBPMNProcessModel(
              bpmnCollaboration, new File(GRAPH_GRAMMAR_TEMP_DIR), false);
    } catch (GrooveGenerationRuntimeException e) {
      // Retry but using ids everywhere.
      grooveGrammarFolder =
          transformer.generateGrooveGrammarForBPMNProcessModel(
              bpmnCollaboration, new File(GRAPH_GRAMMAR_TEMP_DIR), true);
    }
    return grooveGrammarFolder;
  }
}
