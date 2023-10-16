package no.tk.scalability;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.FlowElement;
import org.camunda.bpm.model.bpmn.instance.FlowNode;
import org.camunda.bpm.model.bpmn.instance.Gateway;
import org.camunda.bpm.model.bpmn.instance.SequenceFlow;

public class BPMNStatPrinter {

  /** Print statistics for the BPMN files in the given folder. */
  public static void printStats(Path folder) throws IOException {
    try (Stream<Path> files = Files.walk(folder)) {
      files.forEach(
          bpmnFile -> {
            String fileName = bpmnFile.getFileName().toString();
            if (fileName.endsWith(".bpmn")) {
              BpmnModelInstance bpmnModelInstance = Bpmn.readModelFromFile(bpmnFile.toFile());
              int numberOfFlowNodes =
                  bpmnModelInstance.getModelElementsByType(FlowNode.class).size();
              int numberOfSequenceFlows =
                  bpmnModelInstance.getModelElementsByType(SequenceFlow.class).size();
              int numberOfFlowElements =
                  bpmnModelInstance.getModelElementsByType(FlowElement.class).size();
              int numberOfGateways = bpmnModelInstance.getModelElementsByType(Gateway.class).size();

              // modelName;numberOfGateways;numberOfFlowNodes;numberOfSequenceFlows;numberOfFlowElements
              System.out.printf(
                  "%s;%s;%s;%s;%s%n",
                  fileName,
                  numberOfGateways,
                  numberOfFlowNodes,
                  numberOfSequenceFlows,
                  numberOfFlowElements);
            }
          });
    }
  }
}
