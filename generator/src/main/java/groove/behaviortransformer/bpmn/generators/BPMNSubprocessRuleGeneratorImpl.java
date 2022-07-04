package groove.behaviortransformer.bpmn.generators;

import behavior.bpmn.AbstractProcess;
import behavior.bpmn.BPMNCollaboration;
import behavior.bpmn.SequenceFlow;
import behavior.bpmn.activities.CallActivity;
import behavior.bpmn.events.BoundaryEvent;
import groove.behaviortransformer.bpmn.BPMNRuleGenerator;
import groove.behaviortransformer.bpmn.BPMNToGrooveTransformerHelper;
import groove.graph.GrooveNode;
import groove.graph.rule.GrooveRuleBuilder;

import java.util.function.Consumer;

import static groove.behaviortransformer.GrooveTransformerHelper.createStringNodeLabel;
import static groove.behaviortransformer.bpmn.BPMNToGrooveTransformerConstants.*;
import static groove.behaviortransformer.bpmn.BPMNToGrooveTransformerHelper.*;

public class BPMNSubprocessRuleGeneratorImpl implements BPMNSubprocessRuleGenerator {
    private final BPMNRuleGenerator bpmnRuleGenerator;
    private final GrooveRuleBuilder ruleBuilder;

    public BPMNSubprocessRuleGeneratorImpl(BPMNRuleGenerator bpmnRuleGenerator, GrooveRuleBuilder ruleBuilder) {
        this.bpmnRuleGenerator = bpmnRuleGenerator;
        this.ruleBuilder = ruleBuilder;
    }

    @Override
    public void createCallActivityRulesForProcess(AbstractProcess process, CallActivity callActivity) {
        // Rules for instantiating a subprocess
        callActivity.getIncomingFlows().forEach(incomingFlow -> this.createSubProcessInstantiationRule(process,
                                                                                                       callActivity,
                                                                                                       incomingFlow));

        // Rule for terminating a subprocess
        this.createTerminateSubProcessRule(process, callActivity);

        // Generate rules for the sub process
        this.createRulesForExecutingTheSubProcess(callActivity);

        this.createBoundaryEventRules(process, callActivity, bpmnRuleGenerator.getCollaboration());
    }

    void createSubProcessInstantiationRule(AbstractProcess process,
                                           CallActivity callActivity,
                                           SequenceFlow incomingFlow) {
        final String incomingFlowId = incomingFlow.getDescriptiveID();
        ruleBuilder.startRule(bpmnRuleGenerator.getTaskOrCallActivityRuleName(callActivity, incomingFlowId));
        GrooveNode processInstance = BPMNToGrooveTransformerHelper.contextProcessInstance(process, ruleBuilder);
        BPMNToGrooveTransformerHelper.deleteTokenWithPosition(ruleBuilder, processInstance, incomingFlowId);

        // TODO: reuse createNewProcessInstance
        GrooveNode subProcessInstance = ruleBuilder.addNode(TYPE_PROCESS_SNAPSHOT);
        ruleBuilder.contextEdge(NAME,
                                subProcessInstance,
                                ruleBuilder.contextNode(createStringNodeLabel(callActivity.getSubProcessModel().getName())));
        ruleBuilder.addEdge(SUBPROCESS, processInstance, subProcessInstance);
        GrooveNode running = ruleBuilder.addNode(TYPE_RUNNING);
        ruleBuilder.addEdge(STATE, subProcessInstance, running);
        if (subprocessHasStartEvents(callActivity)) {
            // Subprocess has a start events which get tokens!
            callActivity.getSubProcessModel().getStartEvents().forEach(startEvent -> BPMNToGrooveTransformerHelper.addTokenWithPosition(
                    ruleBuilder,
                    subProcessInstance,
                    getStartEventTokenName(callActivity.getSubProcessModel(), startEvent)));
        } else {
            // All activites and gateways without incoming sequence flows get a token.
            callActivity.getSubProcessModel().getFlowNodes().filter(controlFlowNode -> controlFlowNode.isTask() ||
                                                                                       controlFlowNode.isGateway()).forEach(
                    controlFlowNode -> BPMNToGrooveTransformerHelper.addTokenWithPosition(ruleBuilder,
                                                                                          subProcessInstance,
                                                                                          controlFlowNode.getName()));
        }

        ruleBuilder.buildRule();
    }

    private boolean subprocessHasStartEvents(CallActivity callActivity) {
        return !callActivity.getSubProcessModel().getStartEvents().isEmpty();
    }

    void createTerminateSubProcessRule(AbstractProcess process, CallActivity callActivity) {
        ruleBuilder.startRule(callActivity.getName() + END);

        // Parent process is running
        final GrooveNode parentProcess = BPMNToGrooveTransformerHelper.contextProcessInstance(process, ruleBuilder);

        // Delete subprocess
        String subProcessName = callActivity.getSubProcessModel().getName();
        bpmnRuleGenerator.deleteTerminatedSubprocess(ruleBuilder, subProcessName, parentProcess);

        // Add outgoing tokens
        callActivity.getOutgoingFlows().forEach(outgoingFlow -> {
            final String outgoingFlowID = outgoingFlow.getDescriptiveID();
            BPMNToGrooveTransformerHelper.addTokenWithPosition(ruleBuilder, parentProcess, outgoingFlowID);
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

    void createBoundaryEventRules(AbstractProcess process, CallActivity callActivity, BPMNCollaboration collaboration) {
        callActivity.getBoundaryEvents().forEach(boundaryEvent -> {
            switch (boundaryEvent.getType()) {
                case NONE:
                case TIMER:
                    createSubProcessBoundaryEventRule(process, callActivity, boundaryEvent, x -> {
                    });
                    break;
                case MESSAGE:
                    collaboration.getIncomingMessageFlows(boundaryEvent).forEach(messageFlow -> createSubProcessBoundaryEventRule(
                            process,
                            callActivity,
                            boundaryEvent,
                            processInstance -> deleteMessageToProcessInstanceWithPosition(ruleBuilder,
                                                                                          processInstance,
                                                                                          messageFlow.getName())));

                    break;
                case SIGNAL:
                    // Handled in the throw rule part.
                    break;
                default:
                    throw new IllegalStateException("Unexpected value: " + boundaryEvent.getType());
            }
        });
    }

    private void createSubProcessBoundaryEventRule(AbstractProcess process,
                                                   CallActivity callActivity,
                                                   BoundaryEvent boundaryEvent,
                                                   Consumer<GrooveNode> additionalActions) {
        ruleBuilder.startRule(boundaryEvent.getName());
        GrooveNode processInstance = BPMNToGrooveTransformerHelper.addTokensForOutgoingFlowsToRunningInstance(
                boundaryEvent,
                process,
                ruleBuilder);
        additionalActions.accept(processInstance);

        if (boundaryEvent.isInterrupt()) {
            interruptSubprocess(ruleBuilder, callActivity, processInstance, null);
        } else {
            // Subprocess must be running
            GrooveNode subprocessInstance =
                    BPMNToGrooveTransformerHelper.contextProcessInstance(callActivity.getSubProcessModel(),
                                                                         ruleBuilder);
            ruleBuilder.contextEdge(SUBPROCESS, processInstance, subprocessInstance);
        }

        ruleBuilder.buildRule();
    }
}
