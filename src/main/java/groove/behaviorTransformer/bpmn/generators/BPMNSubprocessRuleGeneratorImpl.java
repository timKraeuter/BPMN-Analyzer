package groove.behaviorTransformer.bpmn.generators;

import behavior.bpmn.AbstractProcess;
import behavior.bpmn.SequenceFlow;
import behavior.bpmn.activities.CallActivity;
import groove.behaviorTransformer.bpmn.BPMNRuleGenerator;
import groove.behaviorTransformer.bpmn.BPMNToGrooveTransformerHelper;
import groove.graph.GrooveNode;
import groove.graph.rule.GrooveRuleBuilder;

import static groove.behaviorTransformer.GrooveTransformer.AT;
import static groove.behaviorTransformer.GrooveTransformer.FORALL;
import static groove.behaviorTransformer.GrooveTransformerHelper.createStringNodeLabel;
import static groove.behaviorTransformer.bpmn.BPMNToGrooveTransformerConstants.*;

public class BPMNSubprocessRuleGeneratorImpl implements BPMNSubprocessRuleGenerator {
    private final BPMNRuleGenerator bpmnRuleGenerator;
    private final GrooveRuleBuilder ruleBuilder;

    public BPMNSubprocessRuleGeneratorImpl(BPMNRuleGenerator bpmnRuleGenerator,
                                           GrooveRuleBuilder ruleBuilder) {
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

        this.createBoundaryEventRules(process, callActivity);
    }

    void createSubProcessInstantiationRule(AbstractProcess process,
                                           CallActivity callActivity,
                                           SequenceFlow incomingFlow) {
        final String incomingFlowId = incomingFlow.getID();
        ruleBuilder.startRule(bpmnRuleGenerator.getTaskOrCallActivityRuleName(callActivity, incomingFlowId));
        GrooveNode processInstance = BPMNToGrooveTransformerHelper.createContextRunningProcessInstance(process,
                                                                                                       ruleBuilder);
        BPMNToGrooveTransformerHelper.deleteTokenWithPosition(ruleBuilder, processInstance, incomingFlowId);

        // TODO: reuse createNewProcessInstance
        GrooveNode subProcessInstance = ruleBuilder.addNode(TYPE_PROCESS_SNAPSHOT);
        ruleBuilder.contextEdge(NAME,
                                subProcessInstance,
                                ruleBuilder.contextNode(createStringNodeLabel(callActivity.getSubProcessModel().getName())));
        ruleBuilder.addEdge(SUBPROCESS, processInstance, subProcessInstance);
        GrooveNode running = ruleBuilder.addNode(TYPE_RUNNING);
        ruleBuilder.addEdge(STATE, subProcessInstance, running);
        if (callActivity.getSubProcessModel().getStartEvent() != null) {
            // Subprocess has a unique start event which gets a token!
            BPMNToGrooveTransformerHelper.addTokenWithPosition(ruleBuilder,
                                                               subProcessInstance,
                                                               bpmnRuleGenerator.getStartEventTokenName(callActivity.getSubProcessModel()));
        } else {
            // All activites and gateways without incoming sequence flows get a token.
            callActivity.getSubProcessModel().getControlFlowNodes().filter(controlFlowNode -> controlFlowNode.isTask() || controlFlowNode.isGateway()).forEach(
                    controlFlowNode -> BPMNToGrooveTransformerHelper.addTokenWithPosition(ruleBuilder,
                                                                                          subProcessInstance,
                                                                                          controlFlowNode.getName()));
        }

        ruleBuilder.buildRule();
    }

    void createTerminateSubProcessRule(AbstractProcess process, CallActivity callActivity) {
        ruleBuilder.startRule(callActivity.getName() + END);

        // Parent process is running
        final GrooveNode parentProcess =
                BPMNToGrooveTransformerHelper.createContextRunningProcessInstance(
                        process,
                        ruleBuilder);

        // Delete subprocess
        String subProcessName = callActivity.getSubProcessModel().getName();
        bpmnRuleGenerator.deleteTerminatedSubprocess(ruleBuilder, subProcessName, parentProcess);

        // Add outgoing tokens
        callActivity.getOutgoingFlows().forEach(outgoingFlow -> {
            final String outgoingFlowID = outgoingFlow.getID();
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

    void createBoundaryEventRules(AbstractProcess process, CallActivity callActivity) {
        callActivity.getBoundaryEvents().forEach(boundaryEvent -> {
            ruleBuilder.startRule(boundaryEvent.getName());
            GrooveNode processInstance = BPMNToGrooveTransformerHelper.addTokensForOutgoingFlowsToRunningInstance(
                    boundaryEvent,
                    process,
                    ruleBuilder);

            if (boundaryEvent.isCancelActivity()) {
                // Terminate subprocess and delete all its tokens.
                GrooveNode subProcess = ruleBuilder.deleteNode(TYPE_PROCESS_SNAPSHOT);
                ruleBuilder.contextEdge(SUBPROCESS, processInstance, subProcess);
                String subprocessName = callActivity.getSubProcessModel().getName();
                ruleBuilder.contextEdge(NAME,
                                        subProcess,
                                        ruleBuilder.contextNode(createStringNodeLabel(subprocessName)));
                GrooveNode subprocessRunning = ruleBuilder.deleteNode(TYPE_RUNNING);
                ruleBuilder.deleteEdge(STATE, subProcess, subprocessRunning);

                GrooveNode forAllTokens = ruleBuilder.contextNode(FORALL);
                GrooveNode arbitraryToken = ruleBuilder.deleteNode(TYPE_TOKEN);
                ruleBuilder.deleteEdge(TOKENS, subProcess, arbitraryToken);
                ruleBuilder.contextEdge(AT, arbitraryToken, forAllTokens);

                GrooveNode forAllMessages = ruleBuilder.contextNode(FORALL);
                GrooveNode arbitraryMessage = ruleBuilder.deleteNode(TYPE_MESSAGE);
                ruleBuilder.deleteEdge(MESSAGES, subProcess, arbitraryMessage);
                ruleBuilder.contextEdge(AT, arbitraryMessage, forAllMessages);
            }

            ruleBuilder.buildRule();
        });
    }
}
