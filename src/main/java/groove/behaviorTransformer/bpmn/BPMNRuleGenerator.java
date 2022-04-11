package groove.behaviorTransformer.bpmn;

import behavior.bpmn.Process;
import behavior.bpmn.*;
import behavior.bpmn.gateways.InclusiveGateway;
import com.google.common.collect.Sets;
import groove.behaviorTransformer.bpmn.generators.*;
import groove.graph.GrooveNode;
import groove.graph.rule.GrooveGraphRule;
import groove.graph.rule.GrooveRuleBuilder;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static groove.behaviorTransformer.GrooveTransformerHelper.createStringNodeLabel;
import static groove.behaviorTransformer.bpmn.BPMNToGrooveTransformerConstants.*;

public class BPMNRuleGenerator {
    private final GrooveRuleBuilder ruleBuilder;
    private final BPMNCollaboration collaboration;
    private final Set<Process> visitedProcessModels;

    // Subgenerators
    private final BPMNTaskRuleGenerator taskRuleGenerator;
    private final BPMNEventRuleGenerator eventRuleGenerator;
    private final BPMNGatewayRuleGenerator gatewayRuleGenerator;
    private final BPMNEventSubprocessRuleGenerator eventSubprocessRuleGenerator;
    private final BPMNSubprocessRuleGenerator subprocessRuleGenerator;

    BPMNRuleGenerator(GrooveRuleBuilder ruleBuilder, BPMNCollaboration collaboration) {
        this.ruleBuilder = ruleBuilder;
        this.collaboration = collaboration;
        visitedProcessModels = Sets.newHashSet();

        taskRuleGenerator = new BPMNTaskRuleGeneratorImpl(collaboration, ruleBuilder);
        eventRuleGenerator = new BPMNEventRuleGeneratorImpl(this, ruleBuilder);
        gatewayRuleGenerator = new BPMNGatewayRuleGeneratorImpl(collaboration, ruleBuilder);
        eventSubprocessRuleGenerator = new BPMNEventSubprocessRuleGeneratorImpl(this, ruleBuilder);
        subprocessRuleGenerator = new BPMNSubprocessRuleGeneratorImpl(this, ruleBuilder);

        generateRules();
    }

    public Stream<GrooveGraphRule> getRules() {
        return ruleBuilder.getRules();
    }

    public Set<Process> getVisitedProcessModels() {
        return visitedProcessModels;
    }

    public BPMNCollaboration getCollaboration() {
        return collaboration;
    }

    public BPMNEventSubprocessRuleGenerator getEventSubprocessRuleGenerator() {
        return eventSubprocessRuleGenerator;
    }

    public BPMNSubprocessRuleGenerator getSubprocessRuleGenerator() {
        return subprocessRuleGenerator;
    }

    public BPMNGatewayRuleGenerator getGatewayRuleGenerator() {
        return gatewayRuleGenerator;
    }

    public BPMNTaskRuleGenerator getTaskRuleGenerator() {
        return taskRuleGenerator;
    }

    public BPMNEventRuleGenerator getEventRuleGenerator() {
        return eventRuleGenerator;
    }

    private void generateRules() {
        collaboration.getParticipants().forEach(process -> {
            if (!visitedProcessModels.contains(process)) {
                visitedProcessModels.add(process);
                generateRulesForProcess(process);
            }
        });
    }

    public void generateRulesForProcess(AbstractProcess process) {
        process.getControlFlowNodes().forEach(node -> node.accept(new RuleGenerationFlowNodeVisitor(this, process)));

        getEventSubprocessRuleGenerator().generateRulesForEventSubprocesses(process);
    }

    public String getStartEventTokenName(Process process) {
        return process.getName() + "_" + process.getStartEvent().getName();
    }

    public String getTaskOrCallActivityRuleName(FlowNode taskOrCallActivity, String incomingFlowId) {
        if (taskOrCallActivity.getIncomingFlows().count() > 1) {
            return taskOrCallActivity.getName() + "_" + incomingFlowId;
        }
        return taskOrCallActivity.getName();
    }

    public void deleteTerminatedSubprocess(GrooveRuleBuilder ruleBuilder,
                                           String eSubprocessName,
                                           GrooveNode parentProcessInstance) {
        GrooveNode subProcessInstance = ruleBuilder.deleteNode(TYPE_PROCESS_SNAPSHOT);
        ruleBuilder.deleteEdge(NAME,
                               subProcessInstance,
                               ruleBuilder.contextNode(createStringNodeLabel(eSubprocessName)));
        ruleBuilder.deleteEdge(SUBPROCESS, parentProcessInstance, subProcessInstance);
        GrooveNode terminated = ruleBuilder.deleteNode(TYPE_TERMINATED);
        ruleBuilder.deleteEdge(STATE, subProcessInstance, terminated);
    }

    void createInclusiveGatewayRules(AbstractProcess process, InclusiveGateway inclusiveGateway) {
        long incomingFlowCount = inclusiveGateway.getIncomingFlows().count();
        long outgoingFlowCount = inclusiveGateway.getOutgoingFlows().count();
        if (incomingFlowCount <= 1 && outgoingFlowCount > 1) {
            this.createBranchingInclusiveGatewayRules(process, ruleBuilder, inclusiveGateway);
            return;
        }
        if (incomingFlowCount > 1 && outgoingFlowCount == 1) {
            this.createMergingInclusiveGatewayRules(process, ruleBuilder, inclusiveGateway);
            return;
        }
        if (outgoingFlowCount == 0) {
            throw new RuntimeException(String.format("The inclusive gateway \"%s\" has no outgoing flows!",
                                                     inclusiveGateway.getName()));
        }
        throw new RuntimeException("Inclusive gateway should not have multiple incoming and outgoing flows.");
    }

    private void createMergingInclusiveGatewayRules(AbstractProcess process,
                                                    GrooveRuleBuilder ruleBuilder,
                                                    InclusiveGateway inclusiveGateway) {
        // Find the corresponding branching inclusive gateway
        Map<SequenceFlow, SequenceFlow> branchFlowsToInFlows = new HashMap<>();
        FlowNode branchGateway = this.findCorrespondingBranchGateway(inclusiveGateway, branchFlowsToInFlows);
        //noinspection OptionalGetWithoutIsPresent size 1 means this operation is save.
        SequenceFlow outFlow = inclusiveGateway.getOutgoingFlows().findFirst().get();
        int i = 1;
        for (Set<SequenceFlow> branchGatewayOutFlows :
                Sets.powerSet(branchGateway.getOutgoingFlows().collect(Collectors.toCollection(
                        LinkedHashSet::new)))) {
            if (branchGatewayOutFlows.size() >= 1) { // Empty set is also part of the power set.
                ruleBuilder.startRule(inclusiveGateway.getName() + "_" + i);
                GrooveNode processInstance = BPMNToGrooveTransformerHelper.createContextRunningProcessInstance(process,
                                                                                                               ruleBuilder);
                GrooveNode newToken = ruleBuilder.addNode(TYPE_TOKEN);
                ruleBuilder.addEdge(TOKENS, processInstance, newToken);
                String outFlowID = outFlow.getID();
                ruleBuilder.addEdge(POSITION, newToken, ruleBuilder.contextNode(createStringNodeLabel(outFlowID)));

                StringBuilder stringBuilder = new StringBuilder();
                branchGatewayOutFlows.forEach(branchOutFlow -> {
                    String branchOutFlowID = branchOutFlow.getID();
                    final SequenceFlow correspondingInFlow = branchFlowsToInFlows.get(branchOutFlow);
                    BPMNToGrooveTransformerHelper.deleteTokenWithPosition(ruleBuilder,
                                                                          processInstance,
                                                                          correspondingInFlow.getID());
                    stringBuilder.append(branchOutFlowID);
                });
                GrooveNode decision = ruleBuilder.deleteNode(TYPE_DECISION);
                ruleBuilder.deleteEdge(DECISIONS, processInstance, decision);
                ruleBuilder.deleteEdge(DECISION,
                                       decision,
                                       ruleBuilder.contextNode(createStringNodeLabel(stringBuilder.toString())));
                ruleBuilder.buildRule();
                i++;
            }
        }
    }

    private FlowNode findCorrespondingBranchGateway(InclusiveGateway inclusiveGateway,
                                                    Map<SequenceFlow, SequenceFlow> branchFlowsToInFlows) {
        final Set<FlowNode> iGateways = inclusiveGateway.getIncomingFlows().map(inFlow -> searchBranchingGateway(inFlow,
                                                                                                                 inFlow,
                                                                                                                 branchFlowsToInFlows)).collect(
                Collectors.toSet());
        return getSingleGatewayOrThrowException(iGateways);
    }

    private FlowNode searchBranchingGateway(SequenceFlow originalFlow,
                                            SequenceFlow currentFlow,
                                            Map<SequenceFlow, SequenceFlow> branchFlowsToInFlows) {
        final FlowNode source = currentFlow.getSource();
        if (source.isInclusiveGateway()) {
            branchFlowsToInFlows.put(currentFlow, originalFlow);
            return source;
        }
        final Set<FlowNode> iGateways = source.getIncomingFlows().map(inFlow -> searchBranchingGateway(originalFlow,
                                                                                                       inFlow,
                                                                                                       branchFlowsToInFlows)).collect(
                Collectors.toSet());
        return getSingleGatewayOrThrowException(iGateways);
    }

    private FlowNode getSingleGatewayOrThrowException(Set<FlowNode> iGateways) {
        if (iGateways.size() == 1) {
            return iGateways.iterator().next();
        } else {
            throw new RuntimeException("No matching branching inclusive Gateway found!");
        }
    }

    private void createBranchingInclusiveGatewayRules(AbstractProcess process,
                                                      GrooveRuleBuilder ruleBuilder,
                                                      InclusiveGateway inclusiveGateway) {
        Optional<SequenceFlow> incFlow = inclusiveGateway.getIncomingFlows().findFirst();
        int i = 1;
        for (Set<SequenceFlow> outFlows :
                Sets.powerSet(inclusiveGateway.getOutgoingFlows().collect(Collectors.toCollection(
                        LinkedHashSet::new)))) {
            if (outFlows.size() >= 1) { // Empty set is also part of the power set.
                ruleBuilder.startRule(inclusiveGateway.getName() + "_" + i);
                GrooveNode processInstance = BPMNToGrooveTransformerHelper.createContextRunningProcessInstance(process,
                                                                                                               ruleBuilder);
                String deleteTokenPosition;
                if (incFlow.isPresent()) {
                    deleteTokenPosition = incFlow.get().getID();
                } else {
                    deleteTokenPosition = inclusiveGateway.getName();
                }
                BPMNToGrooveTransformerHelper.deleteTokenWithPosition(ruleBuilder,
                                                                      processInstance,
                                                                      deleteTokenPosition);

                StringBuilder stringBuilder = new StringBuilder();
                outFlows.forEach(outFlow -> {
                    String outFlowID = outFlow.getID();
                    GrooveNode newToken = ruleBuilder.addNode(TYPE_TOKEN);
                    ruleBuilder.addEdge(TOKENS, processInstance, newToken);
                    ruleBuilder.addEdge(POSITION, newToken, ruleBuilder.contextNode(createStringNodeLabel(outFlowID)));
                    stringBuilder.append(outFlowID);
                });
                GrooveNode decision = ruleBuilder.addNode(TYPE_DECISION);
                ruleBuilder.addEdge(DECISIONS, processInstance, decision);
                ruleBuilder.addEdge(DECISION,
                                    decision,
                                    ruleBuilder.contextNode(createStringNodeLabel(stringBuilder.toString())));
                ruleBuilder.buildRule();
                i++;
            }
        }
    }
}
