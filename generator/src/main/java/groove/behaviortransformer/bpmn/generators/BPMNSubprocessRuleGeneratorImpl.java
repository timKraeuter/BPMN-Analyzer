package groove.behaviortransformer.bpmn.generators;

import static groove.behaviortransformer.bpmn.BPMNToGrooveTransformerConstants.*;
import static groove.behaviortransformer.bpmn.BPMNToGrooveTransformerHelper.*;

import behavior.bpmn.AbstractBPMNProcess;
import behavior.bpmn.BPMNCollaboration;
import behavior.bpmn.SequenceFlow;
import behavior.bpmn.activities.CallActivity;
import behavior.bpmn.events.BoundaryEvent;
import groove.behaviortransformer.bpmn.BPMNRuleGenerator;
import groove.behaviortransformer.bpmn.BPMNToGrooveTransformerHelper;
import groove.graph.GrooveNode;
import groove.graph.rule.GrooveRuleBuilder;
import java.util.function.Consumer;

public class BPMNSubprocessRuleGeneratorImpl implements BPMNSubprocessRuleGenerator {
  private final BPMNRuleGenerator bpmnRuleGenerator;
  private final GrooveRuleBuilder ruleBuilder;

  public BPMNSubprocessRuleGeneratorImpl(
      BPMNRuleGenerator bpmnRuleGenerator, GrooveRuleBuilder ruleBuilder) {
    this.bpmnRuleGenerator = bpmnRuleGenerator;
    this.ruleBuilder = ruleBuilder;
  }

  @Override
  public void createCallActivityRulesForProcess(
      AbstractBPMNProcess process, CallActivity callActivity) {
    // Rules for instantiating a subprocess
    callActivity
        .getIncomingFlows()
        .forEach(
            incomingFlow ->
                this.createSubProcessInstantiationRule(process, callActivity, incomingFlow));

    // Rule for terminating a subprocess
    this.createTerminateSubProcessRule(process, callActivity);

    // Generate rules for the sub process
    this.createRulesForExecutingTheSubProcess(callActivity);

    // Generate rules for boundary events
    this.createBoundaryEventRules(process, callActivity, bpmnRuleGenerator.getCollaboration());
  }

  void createSubProcessInstantiationRule(
      AbstractBPMNProcess process, CallActivity callActivity, SequenceFlow incomingFlow) {
    final String incomingFlowId = getSequenceFlowIdOrDescriptiveName(incomingFlow);
    ruleBuilder.startRule(
        bpmnRuleGenerator.getTaskOrCallActivityRuleName(callActivity, incomingFlowId));
    GrooveNode processInstance = contextProcessInstance(process, ruleBuilder);
    BPMNToGrooveTransformerHelper.deleteTokenWithPosition(
        ruleBuilder, processInstance, incomingFlowId);

    GrooveNode subProcessInstance =
        addProcessInstance(ruleBuilder, callActivity.getSubProcessModel().getName());
    ruleBuilder.addEdge(SUBPROCESS, processInstance, subProcessInstance);
    if (subprocessHasStartEvents(callActivity)) {
      // Subprocess has a start events which get tokens!
      callActivity
          .getSubProcessModel()
          .getStartEvents()
          .forEach(
              startEvent ->
                  BPMNToGrooveTransformerHelper.addOutgoingTokensForFlowNodeToProcessInstance(
                      startEvent, ruleBuilder, subProcessInstance));
    } else {
      // All activities and gateways without incoming sequence flows get a token.
      callActivity
          .getSubProcessModel()
          .getFlowNodes()
          .filter(flowNode -> flowNode.isTask() || flowNode.isGateway())
          .filter(flowNode -> flowNode.getIncomingFlows().findAny().isEmpty())
          .forEach(
              flowNode ->
                  BPMNToGrooveTransformerHelper.addTokenWithPosition(
                      ruleBuilder, subProcessInstance, getFlowNodeNameAndID(flowNode)));
    }

    ruleBuilder.buildRule();
  }

  void createTerminateSubProcessRule(AbstractBPMNProcess process, CallActivity callActivity) {
    ruleBuilder.startRule(callActivity.getName() + END);

    // Parent process is running
    final GrooveNode parentProcess = contextProcessInstance(process, ruleBuilder);

    // Delete subprocess
    String subProcessName = callActivity.getSubProcessModel().getName();
    bpmnRuleGenerator.deleteTerminatedSubprocess(ruleBuilder, subProcessName, parentProcess);

    // Add outgoing tokens
    callActivity
        .getOutgoingFlows()
        .forEach(
            outgoingFlow -> {
              final String outgoingFlowID = getSequenceFlowIdOrDescriptiveName(outgoingFlow);
              BPMNToGrooveTransformerHelper.addTokenWithPosition(
                  ruleBuilder, parentProcess, outgoingFlowID);
            });

    ruleBuilder.buildRule();
  }

  void createRulesForExecutingTheSubProcess(CallActivity callActivity) {
    if (bpmnRuleGenerator.getVisitedProcessModels().contains(callActivity.getSubProcessModel())) {
      return;
    }
    bpmnRuleGenerator.getVisitedProcessModels().add(callActivity.getSubProcessModel());
    bpmnRuleGenerator.generateRulesForProcess(callActivity.getSubProcessModel());
  }

  void createBoundaryEventRules(
      AbstractBPMNProcess process, CallActivity callActivity, BPMNCollaboration collaboration) {
    callActivity
        .getBoundaryEvents()
        .forEach(
            boundaryEvent -> {
              switch (boundaryEvent.getType()) {
                case NONE, TIMER:
                  createSubProcessBoundaryEventRule(process, callActivity, boundaryEvent, x -> {});
                  break;
                case MESSAGE:
                  collaboration
                      .getIncomingMessageFlows(boundaryEvent)
                      .forEach(
                          messageFlow ->
                              createSubProcessBoundaryEventRule(
                                  process,
                                  callActivity,
                                  boundaryEvent,
                                  processInstance ->
                                      deleteMessageToProcessInstanceWithPosition(
                                          ruleBuilder,
                                          processInstance,
                                          messageFlow.getNameOrDescriptiveName())));

                  break;
                case SIGNAL, ERROR, ESCALATION:
                  // Handled in the throw rule part.
                  break;
                default:
                  throw new IllegalStateException("Unexpected value: " + boundaryEvent.getType());
              }
            });
  }

  private void createSubProcessBoundaryEventRule(
      AbstractBPMNProcess process,
      CallActivity callActivity,
      BoundaryEvent boundaryEvent,
      Consumer<GrooveNode> additionalActions) {
    ruleBuilder.startRule(boundaryEvent.getName());
    GrooveNode processInstance =
        BPMNToGrooveTransformerHelper.addTokensForOutgoingFlowsToRunningInstance(
            boundaryEvent, process, ruleBuilder);
    additionalActions.accept(processInstance);

    if (boundaryEvent.isInterrupt()) {
      interruptSubprocess(ruleBuilder, callActivity.getSubProcessModel(), processInstance, false);
    } else {
      // Subprocess must be running
      GrooveNode subprocessInstance =
          contextProcessInstance(callActivity.getSubProcessModel(), ruleBuilder);
      ruleBuilder.contextEdge(SUBPROCESS, processInstance, subprocessInstance);
    }

    ruleBuilder.buildRule();
  }
}
