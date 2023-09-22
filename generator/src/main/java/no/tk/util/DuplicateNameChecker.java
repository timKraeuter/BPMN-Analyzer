package no.tk.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import no.tk.behavior.bpmn.AbstractBPMNProcess;
import no.tk.behavior.bpmn.BPMNCollaboration;
import no.tk.behavior.bpmn.FlowElement;
import no.tk.behavior.bpmn.auxiliary.visitors.DoNothingFlowElementVisitor;
import no.tk.behavior.bpmn.events.EndEvent;
import no.tk.behavior.bpmn.events.EndEventType;
import no.tk.behavior.bpmn.events.IntermediateCatchEvent;
import no.tk.behavior.bpmn.events.IntermediateCatchEventType;
import no.tk.behavior.bpmn.events.IntermediateThrowEvent;
import no.tk.behavior.bpmn.events.IntermediateThrowEventType;
import no.tk.behavior.bpmn.events.StartEvent;
import no.tk.behavior.bpmn.events.StartEventType;

public class DuplicateNameChecker {

  private DuplicateNameChecker() {}

  /** Returns duplicates names or an empty set. */
  public static Set<String> checkForDuplicateNames(
      BPMNCollaboration collaboration, Predicate<FlowElement> exceptions) {
    Set<String> warnings = new HashSet<>();

    Map<String, FlowElement> flowElements = new HashMap<>();
    collaboration
        .getParticipants()
        .forEach(bpmnProcess -> checkProcess(bpmnProcess, warnings, flowElements, exceptions));

    return warnings;
  }

  public static Predicate<FlowElement> signalLinkErrorEscalationExclusion() {
    ValueWrapper<Boolean> result = new ValueWrapper<>(false);
    DoNothingFlowElementVisitor visitor =
        new DoNothingFlowElementVisitor() {
          @Override
          public void handle(StartEvent startEvent) {
            result.setValue(
                startEvent
                    .getType()
                    .isAny(StartEventType.SIGNAL, StartEventType.ERROR, StartEventType.ESCALATION));
          }

          @Override
          public void handle(IntermediateCatchEvent intermediateCatchEvent) {
            result.setValue(
                intermediateCatchEvent
                    .getType()
                    .isAny(IntermediateCatchEventType.SIGNAL, IntermediateCatchEventType.LINK));
          }

          @Override
          public void handle(IntermediateThrowEvent intermediateThrowEvent) {
            result.setValue(
                intermediateThrowEvent
                    .getType()
                    .isAny(
                        IntermediateThrowEventType.SIGNAL,
                        IntermediateThrowEventType.LINK,
                        IntermediateThrowEventType.ESCALATION));
          }

          @Override
          public void handle(EndEvent endEvent) {
            result.setValue(
                endEvent
                    .getType()
                    .isAny(EndEventType.SIGNAL, EndEventType.ERROR, EndEventType.ESCALATION));
          }
        };

    return flowElement -> {
      flowElement.accept(visitor);
      Boolean resultValue = result.getValueIfExists();
      result.setValue(false);
      return resultValue;
    };
  }

  private static void checkProcess(
      AbstractBPMNProcess bpmnProcess,
      Set<String> warnings,
      Map<String, FlowElement> flowElements,
      Predicate<FlowElement> exceptions) {
    bpmnProcess
        .flowNodes()
        .forEach(flowNode -> checkAndSaveName(flowNode, warnings, flowElements, exceptions));
    bpmnProcess
        .sequenceFlows()
        .forEach(
            sequenceFlow -> checkAndSaveName(sequenceFlow, warnings, flowElements, exceptions));
    bpmnProcess
        .allSubProcesses()
        .forEach(subProcess -> checkProcess(subProcess, warnings, flowElements, exceptions));
    bpmnProcess
        .eventSubprocesses()
        .forEach(
            eventSubprocess -> checkProcess(eventSubprocess, warnings, flowElements, exceptions));
  }

  private static void checkAndSaveName(
      FlowElement flowElement,
      Set<String> warnings,
      Map<String, FlowElement> flowElements,
      Predicate<FlowElement> exceptions) {
    if (flowElement.getName().isEmpty()) {
      // Empty names are allowed
      return;
    }
    FlowElement flowElementWithTheSameName = flowElements.get(flowElement.getName());
    if (flowElementWithTheSameName != null && !exceptions.test(flowElement)) {
      warnings.add(flowElement.getName());
    }
    flowElements.put(flowElement.getName(), flowElement);
  }
}
