package no.tk.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import no.tk.behavior.bpmn.AbstractBPMNProcess;
import no.tk.behavior.bpmn.BPMNCollaboration;
import no.tk.behavior.bpmn.FlowElement;

public class DuplicateNameChecker {

  private DuplicateNameChecker() {}

  /** Returns a set of warnings about duplicates names or an empty set. */
  public static Set<String> checkForDuplicateNames(BPMNCollaboration collaboration) {
    Set<String> warnings = new HashSet<>();

    Map<String, FlowElement> flowElements = new HashMap<>();
    collaboration
        .getParticipants()
        .forEach(bpmnProcess -> checkProcess(bpmnProcess, warnings, flowElements));

    return warnings;
  }

  private static void checkProcess(
      AbstractBPMNProcess bpmnProcess,
      Set<String> warnings,
      Map<String, FlowElement> flowElements) {
    bpmnProcess.flowNodes().forEach(flowNode -> checkAndSaveName(flowNode, warnings, flowElements));
    bpmnProcess
        .sequenceFlows()
        .forEach(sequenceFlow -> checkAndSaveName(sequenceFlow, warnings, flowElements));
    bpmnProcess
        .allSubProcesses()
        .forEach(subProcess -> checkProcess(subProcess, warnings, flowElements));
    bpmnProcess
        .eventSubprocesses()
        .forEach(eventSubprocess -> checkProcess(eventSubprocess, warnings, flowElements));
  }

  private static void checkAndSaveName(
      FlowElement flowElement, Set<String> warnings, Map<String, FlowElement> flowElements) {
    if (flowElement.getName().isEmpty()) {
      // Empty names are allowed
      return;
    }
    FlowElement flowElementWithTheSameName = flowElements.get(flowElement.getName());
    if (flowElementWithTheSameName != null) {
      warnings.add(
          String.format(
              "The name \"%s\" is shared between multiple bpmn elements. Please use unique or empty names.",
              flowElement.getName()));
    }
    flowElements.put(flowElement.getName(), flowElement);
  }
}
