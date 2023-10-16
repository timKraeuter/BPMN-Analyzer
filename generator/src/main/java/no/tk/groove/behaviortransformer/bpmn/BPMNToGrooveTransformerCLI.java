package no.tk.groove.behaviortransformer.bpmn;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import no.tk.behavior.bpmn.BPMNCollaboration;
import no.tk.behavior.bpmn.auxiliary.exceptions.GrooveGenerationRuntimeException;
import no.tk.behavior.bpmn.reader.BPMNFileReader;
import no.tk.groove.behaviortransformer.BehaviorToGrooveTransformer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class BPMNToGrooveTransformerCLI {

  protected static final Logger logger = LogManager.getLogger();

  public static void main(String[] args) throws IOException {
    checkBPMNFilePathIsPresent(args);

    String pathToBPMNFile = args[0];
    BPMNCollaboration bpmnCollaboration = readBPMNFileFromPath(pathToBPMNFile);

    String outputPath = args[1];
    generateGraphGrammar(bpmnCollaboration, outputPath, getLayout(args));
  }

  private static boolean getLayout(String[] args) {
    if (args.length == 3) {
      return Boolean.parseBoolean(args[2]);
    }
    return false;
  }

  private static void generateGraphGrammar(
      BPMNCollaboration bpmnCollaboration, String outputPath, boolean layout) {
    BehaviorToGrooveTransformer transformer = new BehaviorToGrooveTransformer(layout);
    Path outputDir = Path.of(outputPath);
    Path file = transformer.generateGrooveGrammar(bpmnCollaboration, outputDir);
    logger.info("Generation finished see {}", file);
  }

  private static void checkBPMNFilePathIsPresent(String[] args) {
    if (args.length < 2) {
      throw new GrooveGenerationRuntimeException(
          "Please provide two arguments. The first for the input file and the second for the output path.");
    }
  }

  private static BPMNCollaboration readBPMNFileFromPath(String pathToBPMNFile) throws IOException {
    Path model = Path.of(pathToBPMNFile);
    if (!Files.exists(model)) {
      throw new GrooveGenerationRuntimeException(
          String.format("No file at the path %s exists.", pathToBPMNFile));
    }
    BPMNFileReader bpmnFileReader =
        new BPMNFileReader(BPMNToGrooveTransformerHelper::transformToQualifiedGrooveNameIfNeeded);
    return bpmnFileReader.readModelFromFilePath(model);
  }
}
