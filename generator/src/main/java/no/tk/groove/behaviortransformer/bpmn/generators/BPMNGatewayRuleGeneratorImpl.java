package no.tk.groove.behaviortransformer.bpmn.generators;

import static no.tk.groove.behaviortransformer.GrooveTransformerHelper.createStringNodeLabel;
import static no.tk.groove.behaviortransformer.bpmn.BPMNToGrooveTransformerConstants.*;
import static no.tk.groove.behaviortransformer.bpmn.BPMNToGrooveTransformerHelper.addOutgoingTokensForFlowNodeToProcessInstance;
import static no.tk.groove.behaviortransformer.bpmn.BPMNToGrooveTransformerHelper.deleteFlowNodeToken;
import static no.tk.groove.behaviortransformer.bpmn.BPMNToGrooveTransformerHelper.deleteSequenceFlowToken;
import static no.tk.groove.behaviortransformer.bpmn.BPMNToGrooveTransformerHelper.getFlowNodePosition;
import static no.tk.groove.behaviortransformer.bpmn.BPMNToGrooveTransformerHelper.updateTokenPositionWhenRunning;

import com.google.common.collect.Sets;
import io.github.timkraeuter.groove.graph.GrooveNode;
import io.github.timkraeuter.groove.rule.GrooveRuleBuilder;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import no.tk.behavior.bpmn.AbstractBPMNProcess;
import no.tk.behavior.bpmn.FlowNode;
import no.tk.behavior.bpmn.SequenceFlow;
import no.tk.behavior.bpmn.auxiliary.exceptions.BPMNRuntimeException;
import no.tk.behavior.bpmn.gateways.*;
import no.tk.groove.behaviortransformer.bpmn.BPMNToGrooveTransformerHelper;

public class BPMNGatewayRuleGeneratorImpl implements BPMNGatewayRuleGenerator {

  private static final String EXG_RULE_NAME_FORMAT_CHOICE_OR_MERGE = "%s_%s";
  private final GrooveRuleBuilder ruleBuilder;

  public BPMNGatewayRuleGeneratorImpl(GrooveRuleBuilder ruleBuilder) {
    this.ruleBuilder = ruleBuilder;
  }

  @Override
  public void createExclusiveGatewayRules(
      AbstractBPMNProcess process, ExclusiveGateway exclusiveGateway) {
    exclusiveGateway
        .getIncomingFlows()
        .forEach(
            inFlow -> {
              String oldTokenPosition = inFlow.getDescriptiveName();
              exclusiveGateway
                  .getOutgoingFlows()
                  .forEach(
                      outFlow -> {
                        String ruleName =
                            this.getExclusiveGatewayRuleName(exclusiveGateway, inFlow, outFlow);
                        String newTokenPosition = outFlow.getDescriptiveName();
                        createExclusiveGatewayRule(
                            process, ruleBuilder, ruleName, oldTokenPosition, newTokenPosition);
                      });
            });
    // No incoming flows means we expect a token sitting at the gateway.
    if (exclusiveGateway.getIncomingFlows().findAny().isEmpty()) {
      exclusiveGateway
          .getOutgoingFlows()
          .forEach(
              outFlow -> {
                String newTokenPosition = outFlow.getDescriptiveName();
                String ruleName = this.getExclusiveGatewayRuleName(exclusiveGateway, null, outFlow);
                createExclusiveGatewayRule(
                    process,
                    ruleBuilder,
                    ruleName,
                    getFlowNodePosition(exclusiveGateway),
                    newTokenPosition);
              });
    }
  }

  @Override
  public void createParallelGatewayRule(
      AbstractBPMNProcess process, ParallelGateway parallelGateway) {
    ruleBuilder.startRule(parallelGateway.getName());
    GrooveNode processInstance =
        BPMNToGrooveTransformerHelper.contextProcessInstance(process, ruleBuilder);

    parallelGateway
        .getIncomingFlows()
        .forEach(sf -> deleteSequenceFlowToken(ruleBuilder, processInstance, sf));
    // If no incoming flows we consume a token at the position of the gateway.
    if (parallelGateway.getIncomingFlows().findAny().isEmpty()) {
      deleteFlowNodeToken(ruleBuilder, processInstance, parallelGateway);
    }

    addOutgoingTokensForFlowNodeToProcessInstance(parallelGateway, ruleBuilder, processInstance);

    ruleBuilder.buildRule();
  }

  @Override
  public void createEventBasedGatewayRule(
      EventBasedGateway eventBasedGateway, AbstractBPMNProcess process) {
    boolean implicitExclusiveGateway = eventBasedGateway.getIncomingFlows().count() > 1;
    eventBasedGateway
        .getIncomingFlows()
        .forEach(
            inFlow -> {
              String ruleName =
                  implicitExclusiveGateway
                      ? (inFlow.getNameOrIDIfEmpty() + "_" + eventBasedGateway.getName())
                      : eventBasedGateway.getName();
              ruleBuilder.startRule(ruleName);
              updateTokenPositionWhenRunning(
                  process,
                  inFlow.getDescriptiveName(),
                  getFlowNodePosition(eventBasedGateway),
                  ruleBuilder);
              ruleBuilder.buildRule();
            });
    // Effects the rules of the subsequent flow nodes!
    // Possible subsequent nodes: Message catch, Receive task, Signal catch, timer and condition.
    // We currently only implemented the first three.
  }

  @Override
  public void createInclusiveGatewayRules(
      AbstractBPMNProcess process, InclusiveGateway inclusiveGateway) {
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
      throw new BPMNRuntimeException(
          String.format(
              "The inclusive gateway \"%s\" has no outgoing flows!", inclusiveGateway.getName()));
    }
    throw new BPMNRuntimeException(
        "Inclusive gateway should not have multiple incoming and outgoing flows.");
  }

  private void createMergingInclusiveGatewayRules(
      AbstractBPMNProcess process,
      GrooveRuleBuilder ruleBuilder,
      InclusiveGateway inclusiveGateway) {
    // Find the corresponding branching inclusive gateway
    Map<SequenceFlow, SequenceFlow> branchFlowsToInFlows = new HashMap<>();
    FlowNode branchGateway =
        this.findCorrespondingBranchGateway(inclusiveGateway, branchFlowsToInFlows);
    //noinspection OptionalGetWithoutIsPresent size 1 means this operation is save.
    SequenceFlow outFlow = inclusiveGateway.getOutgoingFlows().findFirst().get();
    int i = 1;
    for (Set<SequenceFlow> branchGatewayOutFlows :
        Sets.powerSet(
            branchGateway
                .getOutgoingFlows()
                .collect(Collectors.toCollection(LinkedHashSet::new)))) {
      if (!branchGatewayOutFlows.isEmpty()) { // Empty set is also part of the power set.
        ruleBuilder.startRule(inclusiveGateway.getName() + "_" + i);
        GrooveNode processInstance =
            BPMNToGrooveTransformerHelper.contextProcessInstance(process, ruleBuilder);
        GrooveNode newToken = ruleBuilder.addNode(TYPE_TOKEN);
        ruleBuilder.addEdge(TOKENS, processInstance, newToken);
        String outFlowID = outFlow.getDescriptiveName();
        ruleBuilder.addEdge(
            POSITION, newToken, ruleBuilder.contextNode(createStringNodeLabel(outFlowID)));

        StringBuilder stringBuilder = new StringBuilder();
        branchGatewayOutFlows.forEach(
            branchOutFlow -> {
              String branchOutFlowID = branchOutFlow.getDescriptiveName();
              final SequenceFlow correspondingInFlow = branchFlowsToInFlows.get(branchOutFlow);
              deleteSequenceFlowToken(ruleBuilder, processInstance, correspondingInFlow);
              stringBuilder.append(branchOutFlowID);
            });
        GrooveNode decision = ruleBuilder.deleteNode(TYPE_DECISION);
        ruleBuilder.deleteEdge(DECISIONS, processInstance, decision);
        ruleBuilder.deleteEdge(
            DECISION,
            decision,
            ruleBuilder.contextNode(createStringNodeLabel(stringBuilder.toString())));
        ruleBuilder.buildRule();
        i++;
      }
    }
  }

  private FlowNode findCorrespondingBranchGateway(
      InclusiveGateway inclusiveGateway, Map<SequenceFlow, SequenceFlow> branchFlowsToInFlows) {
    final Set<FlowNode> iGateways =
        inclusiveGateway
            .getIncomingFlows()
            .map(inFlow -> searchBranchingGateway(inFlow, inFlow, branchFlowsToInFlows))
            .collect(Collectors.toSet());
    return getSingleGatewayOrThrowException(iGateways);
  }

  private FlowNode searchBranchingGateway(
      SequenceFlow originalFlow,
      SequenceFlow currentFlow,
      Map<SequenceFlow, SequenceFlow> branchFlowsToInFlows) {
    final FlowNode source = currentFlow.getSource();
    if (source.isInclusiveGateway()) {
      branchFlowsToInFlows.put(currentFlow, originalFlow);
      return source;
    }
    final Set<FlowNode> iGateways =
        source
            .getIncomingFlows()
            .map(inFlow -> searchBranchingGateway(originalFlow, inFlow, branchFlowsToInFlows))
            .collect(Collectors.toSet());
    return getSingleGatewayOrThrowException(iGateways);
  }

  private FlowNode getSingleGatewayOrThrowException(Set<FlowNode> iGateways) {
    if (iGateways.size() == 1) {
      return iGateways.iterator().next();
    } else {
      throw new BPMNRuntimeException("No matching branching inclusive Gateway found!");
    }
  }

  private void createBranchingInclusiveGatewayRules(
      AbstractBPMNProcess process,
      GrooveRuleBuilder ruleBuilder,
      InclusiveGateway inclusiveGateway) {
    Optional<SequenceFlow> incFlow = inclusiveGateway.getIncomingFlows().findFirst();
    int i = 1;
    for (Set<SequenceFlow> outFlows :
        Sets.powerSet(
            inclusiveGateway
                .getOutgoingFlows()
                .collect(Collectors.toCollection(LinkedHashSet::new)))) {
      if (!outFlows.isEmpty()) { // Empty set is also part of the power set.
        ruleBuilder.startRule(inclusiveGateway.getName() + "_" + i);
        GrooveNode processInstance =
            BPMNToGrooveTransformerHelper.contextProcessInstance(process, ruleBuilder);
        if (incFlow.isPresent()) {
          deleteSequenceFlowToken(ruleBuilder, processInstance, incFlow.get());
        } else {
          deleteFlowNodeToken(ruleBuilder, processInstance, inclusiveGateway);
        }

        StringBuilder stringBuilder = new StringBuilder();
        outFlows.forEach(
            outFlow -> {
              String outFlowID = outFlow.getDescriptiveName();
              GrooveNode newToken = ruleBuilder.addNode(TYPE_TOKEN);
              ruleBuilder.addEdge(TOKENS, processInstance, newToken);
              ruleBuilder.addEdge(
                  POSITION, newToken, ruleBuilder.contextNode(createStringNodeLabel(outFlowID)));
              stringBuilder.append(outFlowID);
            });
        GrooveNode decision = ruleBuilder.addNode(TYPE_DECISION);
        ruleBuilder.addEdge(DECISIONS, processInstance, decision);
        ruleBuilder.addEdge(
            DECISION,
            decision,
            ruleBuilder.contextNode(createStringNodeLabel(stringBuilder.toString())));
        ruleBuilder.buildRule();
        i++;
      }
    }
  }

  private void createExclusiveGatewayRule(
      AbstractBPMNProcess process,
      GrooveRuleBuilder ruleBuilder,
      String ruleName,
      String oldTokenPosition,
      String newTokenPosition) {
    ruleBuilder.startRule(ruleName);
    updateTokenPositionWhenRunning(process, oldTokenPosition, newTokenPosition, ruleBuilder);
    ruleBuilder.buildRule();
  }

  private String getExclusiveGatewayRuleName(
      Gateway exclusiveGateway, SequenceFlow inFlow, SequenceFlow outFlow) {
    final long inCount = exclusiveGateway.getIncomingFlows().count();
    final long outCount = exclusiveGateway.getOutgoingFlows().count();

    if (inCount <= 1 && outCount == 1) {
      return exclusiveGateway.getName();
    }
    boolean duplicateTargetOfOutFlows = hasDuplicateTargets(exclusiveGateway.getOutgoingFlows());
    if (inCount <= 1) {
      return String.format(
          EXG_RULE_NAME_FORMAT_CHOICE_OR_MERGE,
          exclusiveGateway.getName(),
          duplicateTargetOfOutFlows ? outFlow.getNameOrIDIfEmpty() : outFlow.getTarget().getName());
    }
    if (outCount == 1) {
      return String.format(
          EXG_RULE_NAME_FORMAT_CHOICE_OR_MERGE,
          inFlow.getNameOrIDIfEmpty(),
          exclusiveGateway.getName());
    }
    return String.format(
        "%s__%s_%s",
        exclusiveGateway.getName(),
        inFlow.getNameOrIDIfEmpty(),
        duplicateTargetOfOutFlows ? outFlow.getNameOrIDIfEmpty() : outFlow.getTarget().getName());
  }

  private boolean hasDuplicateTargets(Stream<SequenceFlow> outgoingFlows) {
    return outgoingFlows
        .map(SequenceFlow::getTarget)
        .collect(Collectors.groupingBy(Function.identity()))
        .entrySet()
        .stream()
        .anyMatch(flowNodeListEntry -> flowNodeListEntry.getValue().size() > 1);
  }
}
