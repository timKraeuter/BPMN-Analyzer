package no.tk.maude.behaviortransformer.bpmn;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import no.tk.behavior.bpmn.BPMNCollaboration;
import no.tk.behavior.bpmn.auxiliary.exceptions.GrooveGenerationRuntimeException;
import no.tk.behavior.bpmn.reader.BPMNFileReader;
import no.tk.groove.behaviortransformer.bpmn.BPMNToGrooveTransformerHelper;
import no.tk.maude.behaviortransformer.bpmn.settings.MaudeBPMNGenerationSettings;
import no.tk.maude.behaviortransformer.bpmn.settings.MessagePersistence;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class BPMNToMaudeTransformerCLI {

  protected static final Logger logger = LogManager.getLogger(BPMNToMaudeTransformerCLI.class);

  public static void main(String[] args) throws IOException {
    checkBPMNFilePathIsPresent(args);

    String pathToBPMNFile = args[0];
    BPMNCollaboration bpmnCollaboration = readBPMNFileFromPath(pathToBPMNFile);

    String outputPath = args[1];
    generateMaudeFile(bpmnCollaboration, outputPath);
  }

  private static void generateMaudeFile(BPMNCollaboration bpmnCollaboration, String outputPath)
      throws IOException {
    BPMNToMaudeTransformer transformer =
        new BPMNToMaudeTransformer(
            bpmnCollaboration, new MaudeBPMNGenerationSettings(MessagePersistence.PERSISTENT));
    Path outputDir = Path.of(outputPath, bpmnCollaboration.getName() + ".maude");
    String maudeFileContent =
        transformer.generate("search init =>! X such that X |= allTerminated = true");
    Files.writeString(outputDir, maudeFileContent);
    logger.info("Generation finished see {}", outputDir);
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
