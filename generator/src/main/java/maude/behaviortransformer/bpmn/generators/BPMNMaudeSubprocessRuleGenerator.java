package maude.behaviortransformer.bpmn.generators;

import behavior.bpmn.AbstractProcess;
import behavior.bpmn.BPMNCollaboration;
import behavior.bpmn.SequenceFlow;
import behavior.bpmn.activities.CallActivity;
import behavior.bpmn.events.BoundaryEvent;
import behavior.bpmn.events.StartEventType;
import groove.behaviortransformer.bpmn.generators.BPMNSubprocessRuleGenerator;
import maude.behaviortransformer.bpmn.BPMNMaudeRuleGenerator;
import maude.behaviortransformer.bpmn.BPMNToMaudeTransformerHelper;
import maude.generation.MaudeObject;
import maude.generation.MaudeObjectBuilder;
import maude.generation.MaudeRuleBuilder;

import java.util.function.Consumer;
import java.util.stream.Collectors;

import static groove.behaviortransformer.bpmn.BPMNToGrooveTransformerConstants.*;

public class BPMNMaudeSubprocessRuleGenerator implements BPMNSubprocessRuleGenerator, BPMNToMaudeTransformerHelper {
    private final BPMNMaudeRuleGenerator bpmnMaudeRuleGenerator;
    private final MaudeRuleBuilder ruleBuilder;
    private final MaudeObjectBuilder objectBuilder;

    public BPMNMaudeSubprocessRuleGenerator(BPMNMaudeRuleGenerator bpmnMaudeRuleGenerator,
                                            MaudeRuleBuilder ruleBuilder) {
        this.bpmnMaudeRuleGenerator = bpmnMaudeRuleGenerator;
        this.ruleBuilder = ruleBuilder;
        objectBuilder = new MaudeObjectBuilder();
    }

    public void createCallActivityRulesForProcess(AbstractProcess process, CallActivity callActivity) {
        // Rules for instantiating a subprocess
        callActivity.getIncomingFlows().forEach(incomingFlow -> this.createSubProcessInstantiationRule(process,
                                                                                                       callActivity,
                                                                                                       incomingFlow));

        // Rule for terminating a subprocess
        this.createTerminateSubProcessRule(process, callActivity);

        // Generate rules for the sub process
        this.createRulesForExecutingTheSubProcess(callActivity);

        // Generate rules for boundary events
        this.createBoundaryEventRules(process, callActivity, bpmnMaudeRuleGenerator.getCollaboration());
    }

    private void createBoundaryEventRules(AbstractProcess process,
                                          CallActivity callActivity,
                                          BPMNCollaboration collaboration) {
        callActivity.getBoundaryEvents().forEach(boundaryEvent -> {
            switch (boundaryEvent.getType()) {
                case NONE:
                case TIMER:
                    createSubProcessBoundaryEventRule(process, callActivity, boundaryEvent, rb -> { }); // NOOP
                    break;
                case MESSAGE:
                    createSubProcessMessageBoundaryEventRule(process, callActivity, boundaryEvent, collaboration);
                    break;
                case SIGNAL:
                    // Handled in the throw rule part.
                    break;
                default:
                    throw new IllegalStateException("Unexpected value: " + boundaryEvent.getType());
            }
        });

    }

    private void createSubProcessMessageBoundaryEventRule(AbstractProcess process,
                                                          CallActivity callActivity,
                                                          BoundaryEvent boundaryEvent,
                                                          BPMNCollaboration collaboration) {
        collaboration.getIncomingMessageFlows(boundaryEvent).forEach(messageFlow -> createSubProcessBoundaryEventRule(
                process,
                callActivity,
                boundaryEvent,
                maudeRuleBuilder -> addMessageConsumption(messageFlow)));
    }

    private void createSubProcessBoundaryEventRule(AbstractProcess process,
                                                   CallActivity callActivity,
                                                   BoundaryEvent boundaryEvent,
                                                   Consumer<MaudeRuleBuilder> ruleAddditions) {
        ruleBuilder.startRule(getFlowNodeRuleName(boundaryEvent));
        ruleAddditions.accept(getRuleBuilder());

        // Setup vars
        String anyOtherTokens1 = ANY_TOKENS + "1";
        String anyOtherSubprocesses1 = ANY_SUBPROCESSES + "1";
        String anyOtherSubprocesses2 = ANY_SUBPROCESSES + "2";
        ruleBuilder.addVar(TOKENS, MSET, anyOtherTokens1);
        ruleBuilder.addVar(SUBPROCESSES, CONFIGURATION, anyOtherSubprocesses1);
        ruleBuilder.addVar(SUBPROCESSES, CONFIGURATION, anyOtherSubprocesses2);

        // Setup pre
        // Subprocess must be running
        String subprocesses = createProcessSnapshotObject(callActivity.getSubProcessModel(),
                                                          anyOtherSubprocesses1,
                                                          anyOtherTokens1,
                                                          RUNNING)
                                      .generateObjectString() + " " + anyOtherSubprocesses2;
        ruleBuilder.addPreObject(createProcessSnapshotObject(process,
                                                             subprocesses,
                                                             ANY_TOKENS,
                                                             RUNNING));
        if (boundaryEvent.isInterrupt()) {
            // Interrupt removes subprocesses
            // Add outgoing tokens
            String postTokens = getOutgoingTokensForFlowNode(boundaryEvent) + ANY_OTHER_TOKENS;
            ruleBuilder.addPostObject(createProcessSnapshotObject(process,
                                                                  anyOtherSubprocesses2,
                                                                  postTokens));
        } else {
            // Add outgoing tokens
            String postTokens = getOutgoingTokensForFlowNode(boundaryEvent) + ANY_OTHER_TOKENS;
            ruleBuilder.addPostObject(createProcessSnapshotObject(process,
                                                                  subprocesses,
                                                                  postTokens));
        }
        ruleBuilder.buildRule();
    }

    private void createSubProcessInstantiationRule(AbstractProcess process,
                                                   CallActivity callActivity,
                                                   SequenceFlow incomingFlow) {

        ruleBuilder.startRule(getFlowNodeRuleNameWithIncFlow(callActivity, incomingFlow.getId()));

        String preTokens = getTokenForSequenceFlow(incomingFlow) + ANY_OTHER_TOKENS;
        ruleBuilder.addPreObject(createProcessSnapshotObjectAnySubProcess(process,
                                                                          preTokens));

        String subProcessTokens;
        if (subprocessHasStartEvents(callActivity)) {
            // Subprocess has start events which get tokens!
            subProcessTokens = callActivity.getSubProcessModel().getStartEvents().stream()
                                           .filter(startEvent -> startEvent.getType() == StartEventType.NONE)
                                           .map(this::getTokenForFlowNode)
                                           .collect(Collectors.joining(" "));
        } else {
            // All activites and gateways without incoming sequence flows get a token.
            subProcessTokens = callActivity.getSubProcessModel().getFlowNodes()
                                           .filter(flowNode -> flowNode.isTask() ||
                                                               flowNode.isGateway())
                                           .filter(flowNode -> flowNode.getIncomingFlows().findAny().isEmpty())
                                           .map(this::getTokenForFlowNode)
                                           .collect(Collectors.joining(" "));
        }
        MaudeObject subProcess = createProcessSnapshotObjectNoSubProcess(callActivity.getSubProcessModel(),
                                                                         subProcessTokens);
        ruleBuilder.addPostObject(createProcessSnapshotObject(process,
                                                                         subProcess.generateObjectString() +
                                                                         ANY_OTHER_SUBPROCESSES,
                                                              ANY_TOKENS));

        ruleBuilder.buildRule();
    }

    private void createTerminateSubProcessRule(AbstractProcess process, CallActivity callActivity) {
        ruleBuilder.startRule(getFlowNodeRuleName(callActivity) + END);

        MaudeObject subProcess = createTerminatedProcessSnapshot(callActivity.getSubProcessModel());
        ruleBuilder.addPreObject(createProcessSnapshotObject(process,
                                                                        subProcess.generateObjectString() +
                                                                        ANY_OTHER_SUBPROCESSES,
                                                             ANY_TOKENS));

        // Add outgoing tokens
        String postTokens = getOutgoingTokensForFlowNode(callActivity) + ANY_OTHER_TOKENS;

        // Subprocess is deleted (since it is not in the post object).
        ruleBuilder.addPostObject(createProcessSnapshotObjectAnySubProcess(process,
                                                                           postTokens));


        ruleBuilder.buildRule();
    }

    private void createRulesForExecutingTheSubProcess(CallActivity callActivity) {
        if (bpmnMaudeRuleGenerator.getVisitedProcessModels().contains(callActivity.getSubProcessModel())) {
            return;
        }
        bpmnMaudeRuleGenerator.getVisitedProcessModels().add(callActivity.getSubProcessModel());
        bpmnMaudeRuleGenerator.generateRulesForProcess(callActivity.getSubProcessModel());
    }

    @Override
    public MaudeRuleBuilder getRuleBuilder() {
        return ruleBuilder;
    }

    @Override
    public MaudeObjectBuilder getObjectBuilder() {
        return objectBuilder;
    }
}
