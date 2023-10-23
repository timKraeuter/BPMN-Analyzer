package no.tk.groove.behaviortransformer.bpmn.generators;

import static no.tk.groove.behaviortransformer.GrooveTransformerHelper.createStringNodeLabel;
import static no.tk.groove.behaviortransformer.bpmn.BPMNToGrooveTransformerConstants.*;

import io.github.timkraeuter.groove.graph.GrooveNode;
import io.github.timkraeuter.groove.rule.GrooveRuleBuilder;
import java.util.Set;
import no.tk.behavior.bpmn.AbstractBPMNProcess;
import no.tk.behavior.bpmn.BPMNCollaboration;
import no.tk.behavior.bpmn.BPMNEventSubprocess;
import no.tk.behavior.bpmn.MessageFlow;
import no.tk.behavior.bpmn.auxiliary.exceptions.BPMNRuntimeException;
import no.tk.behavior.bpmn.events.StartEvent;
import no.tk.groove.behaviortransformer.bpmn.BPMNRuleGenerator;
import no.tk.groove.behaviortransformer.bpmn.BPMNToGrooveTransformerHelper;

public class BPMNEventSubprocessRuleGeneratorImpl implements BPMNEventSubprocessRuleGenerator {
  private final BPMNRuleGenerator bpmnRuleGenerator;
  private final BPMNCollaboration collaboration;
  private final GrooveRuleBuilder ruleBuilder;

  public BPMNEventSubprocessRuleGeneratorImpl(
      BPMNRuleGenerator bpmnRuleGenerator, GrooveRuleBuilder ruleBuilder) {
    this.bpmnRuleGenerator = bpmnRuleGenerator;
    this.collaboration = bpmnRuleGenerator.getCollaboration();
    this.ruleBuilder = ruleBuilder;
  }

  @Override
  public void generateRulesForEventSubprocesses(AbstractBPMNProcess process) {
    process
        .eventSubprocesses()
        .forEach(eventSubprocess -> this.generateRulesForEventSubprocess(process, eventSubprocess));
  }

  private void generateRulesForEventSubprocess(
      AbstractBPMNProcess process, BPMNEventSubprocess eventSubprocess) {
    // Start event rule generation is special
    generateRulesForStartEvents(process, eventSubprocess, collaboration, ruleBuilder);
    // Standard rule generation for other elements.
    bpmnRuleGenerator.generateRulesForProcess(eventSubprocess);
    // Termination rule
    generateTerminateEventSubProcessRule(process, eventSubprocess, ruleBuilder);
  }

  private void generateTerminateEventSubProcessRule(
      AbstractBPMNProcess parentProcess,
      BPMNEventSubprocess eventSubprocess,
      GrooveRuleBuilder ruleBuilder) {
    String eSubprocessName = eventSubprocess.getName();
    ruleBuilder.startRule(eSubprocessName + END);
    GrooveNode parentProcessInstance =
        BPMNToGrooveTransformerHelper.contextProcessInstance(parentProcess, ruleBuilder);
    bpmnRuleGenerator.deleteTerminatedSubprocess(
        ruleBuilder, eSubprocessName, parentProcessInstance);
    ruleBuilder.buildRule();
  }

  private void generateRulesForStartEvents(
      AbstractBPMNProcess process,
      BPMNEventSubprocess eventSubprocess,
      BPMNCollaboration collaboration,
      GrooveRuleBuilder ruleBuilder) {
    eventSubprocess
        .getStartEvents()
        .forEach(
            startEvent -> {
              switch (startEvent.getType()) {
                case NONE:
                  throw new BPMNRuntimeException(
                      "None start events in event subprocesses are useless!");
                case MESSAGE:
                  if (startEvent.isInterrupt()) {
                    createStartInterruptingEvenSubprocessFromMessageRules(
                        process, eventSubprocess, collaboration, ruleBuilder, startEvent);
                  } else {
                    createStartNonInterruptingEvenSubprocessFromMessageRules(
                        process, eventSubprocess, collaboration, ruleBuilder, startEvent);
                  }
                  break;
                case SIGNAL, ERROR, ESCALATION:
                  break;
                default:
                  throw new BPMNRuntimeException(
                      "Unexpected start event type encountered: " + startEvent.getType());
              }
            });
  }

  private void createStartInterruptingEvenSubprocessFromMessageRules(
      AbstractBPMNProcess parentProcess,
      BPMNEventSubprocess eventSubprocess,
      BPMNCollaboration collaboration,
      GrooveRuleBuilder ruleBuilder,
      StartEvent startEvent) {
    Set<MessageFlow> incomingMessageFlows = collaboration.getIncomingMessageFlows(startEvent);
    incomingMessageFlows.forEach(
        incomingMessageFlow -> {
          ruleBuilder.startRule(
              getMessageStartEventRuleName(incomingMessageFlows, incomingMessageFlow, startEvent));
          GrooveNode parentProcessInstance =
              createMessageStartEventRulePart(
                  parentProcess, ruleBuilder, eventSubprocess, incomingMessageFlow, startEvent);
          // The parent is interrupted, i.e., all its tokens are deleted.
          BPMNToGrooveTransformerHelper.deleteAllTokensForProcess(
              ruleBuilder, parentProcessInstance);
          ruleBuilder.buildRule();
        });
  }

  private GrooveNode createMessageStartEventRulePart(
      AbstractBPMNProcess parentProcess,
      GrooveRuleBuilder ruleBuilder,
      BPMNEventSubprocess eventSubprocess,
      MessageFlow incomingMessageFlow,
      StartEvent startEvent) {
    // Needs a running parent process
    GrooveNode parentProcessInstance =
        BPMNToGrooveTransformerHelper.contextProcessInstance(parentProcess, ruleBuilder);

    // Start new subprocess instance of process
    GrooveNode eventSubProcessInstance =
        BPMNToGrooveTransformerHelper.addProcessInstance(ruleBuilder, eventSubprocess.getName());
    ruleBuilder.addEdge(SUBPROCESS, parentProcessInstance, eventSubProcessInstance);

    // Consumes the message
    GrooveNode message = ruleBuilder.deleteNode(TYPE_MESSAGE);
    ruleBuilder.deleteEdge(
        POSITION,
        message,
        ruleBuilder.contextNode(
            createStringNodeLabel(incomingMessageFlow.getNameOrDescriptiveName())));

    // Spawns a new token at each outgoing flow.
    BPMNToGrooveTransformerHelper.addOutgoingTokensForFlowNodeToProcessInstance(
        startEvent, ruleBuilder, eventSubProcessInstance);
    return parentProcessInstance;
  }

  private String getMessageStartEventRuleName(
      Set<MessageFlow> incomingMessageFlows,
      MessageFlow incomingMessageFlow,
      StartEvent startEvent) {
    return incomingMessageFlows.size() > 1
        ? incomingMessageFlow.getNameOrDescriptiveName()
        : startEvent.getName();
  }

  private void createStartNonInterruptingEvenSubprocessFromMessageRules(
      AbstractBPMNProcess process,
      BPMNEventSubprocess eventSubprocess,
      BPMNCollaboration collaboration,
      GrooveRuleBuilder ruleBuilder,
      StartEvent startEvent) {
    Set<MessageFlow> incomingMessageFlows = collaboration.getIncomingMessageFlows(startEvent);
    incomingMessageFlows.forEach(
        incomingMessageFlow -> {
          ruleBuilder.startRule(
              getMessageStartEventRuleName(incomingMessageFlows, incomingMessageFlow, startEvent));
          createMessageStartEventRulePart(
              process, ruleBuilder, eventSubprocess, incomingMessageFlow, startEvent);
          ruleBuilder.buildRule();
        });
  }
}
