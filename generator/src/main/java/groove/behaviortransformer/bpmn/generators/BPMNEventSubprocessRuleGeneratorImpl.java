package groove.behaviortransformer.bpmn.generators;

import static groove.behaviortransformer.GrooveTransformerHelper.createStringNodeLabel;
import static groove.behaviortransformer.bpmn.BPMNToGrooveTransformerConstants.*;

import behavior.bpmn.AbstractBPMNProcess;
import behavior.bpmn.BPMNCollaboration;
import behavior.bpmn.BPMNEventSubprocess;
import behavior.bpmn.MessageFlow;
import behavior.bpmn.auxiliary.exceptions.BPMNRuntimeException;
import behavior.bpmn.events.StartEvent;
import groove.behaviortransformer.bpmn.BPMNRuleGenerator;
import groove.behaviortransformer.bpmn.BPMNToGrooveTransformerHelper;
import groove.graph.GrooveNode;
import groove.graph.rule.GrooveRuleBuilder;
import java.util.Set;

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
        .getEventSubprocesses()
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
