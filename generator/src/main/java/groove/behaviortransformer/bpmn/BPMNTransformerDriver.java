package groove.behaviortransformer.bpmn;

import behavior.bpmn.BPMNCollaboration;
import behavior.bpmn.auxiliary.exceptions.GrooveGenerationRuntimeException;
import behavior.bpmn.reader.BPMNFileReader;
import groove.behaviortransformer.BehaviorToGrooveTransformer;
import java.io.File;

public class BPMNTransformerDriver {

  public static void main(String[] args) {
    checkBPMNFilePathIsPresent(args);

    String pathToBPMNFile = args[0];
    BPMNCollaboration bpmnCollaboration = readBPMNFileFromPath(pathToBPMNFile);

    String outputPath = args[1];
    generateGraphGrammar(bpmnCollaboration, outputPath);
  }

  private static void generateGraphGrammar(BPMNCollaboration bpmnCollaboration, String outputPath) {
    BehaviorToGrooveTransformer transformer = new BehaviorToGrooveTransformer();
    File outputDir = new File(outputPath);
    File file = transformer.generateGrooveGrammar(bpmnCollaboration, outputDir, false);

    System.out.println("Generation finished see " + file.getAbsolutePath());
  }

  private static void checkBPMNFilePathIsPresent(String[] args) {
    if (args.length < 2) {
      throw new GrooveGenerationRuntimeException(
          "Please provide to arguments. The first for the input file and the second for the output path.");
    }
  }

  private static BPMNCollaboration readBPMNFileFromPath(String pathToBPMNFile) {
    File model = new File(pathToBPMNFile);
    if (!model.exists()) {
      throw new GrooveGenerationRuntimeException(
          String.format("No file at the path %s exists.", pathToBPMNFile));
    }
    BPMNFileReader bpmnFileReader =
        new BPMNFileReader(BPMNToGrooveTransformerHelper::transformToQualifiedGrooveNameIfNeeded);
    return bpmnFileReader.readModelFromFile(model);
  }
}
