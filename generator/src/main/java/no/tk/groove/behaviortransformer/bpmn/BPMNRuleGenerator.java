package no.tk.groove.behaviortransformer.bpmn;

import static no.tk.groove.behaviortransformer.GrooveTransformerHelper.createStringNodeLabel;

import com.google.common.collect.Sets;
import java.util.Set;
import java.util.stream.Stream;
import no.tk.behavior.bpmn.AbstractBPMNProcess;
import no.tk.behavior.bpmn.BPMNCollaboration;
import no.tk.behavior.bpmn.BPMNProcess;
import no.tk.behavior.bpmn.FlowNode;
import no.tk.groove.behaviortransformer.bpmn.generators.*;
import no.tk.groove.graph.GrooveNode;
import no.tk.groove.graph.rule.GrooveGraphRule;
import no.tk.groove.graph.rule.GrooveRuleBuilder;

public class BPMNRuleGenerator {
  private final GrooveRuleBuilder ruleBuilder;
  private final BPMNCollaboration collaboration;
  private final Set<BPMNProcess> visitedProcessModels;

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
    gatewayRuleGenerator = new BPMNGatewayRuleGeneratorImpl(ruleBuilder);
    eventSubprocessRuleGenerator = new BPMNEventSubprocessRuleGeneratorImpl(this, ruleBuilder);
    subprocessRuleGenerator = new BPMNSubprocessRuleGeneratorImpl(this, ruleBuilder);

    generateRules();
  }

  public Stream<GrooveGraphRule> getRules() {
    return ruleBuilder.getRules();
  }

  public Set<BPMNProcess> getVisitedProcessModels() {
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
    collaboration
        .getParticipants()
        .forEach(
            process -> {
              if (!visitedProcessModels.contains(process)) {
                visitedProcessModels.add(process);
                generateRulesForProcess(process);
              }
            });
  }

  public void generateRulesForProcess(AbstractBPMNProcess process) {
    process
        .getFlowNodes()
        .forEach(node -> node.accept(new GrooveRuleGenerationFlowNodeVisitor(this, process)));

    getEventSubprocessRuleGenerator().generateRulesForEventSubprocesses(process);
  }

  // Methods shared between the generators.
  public static String getTaskOrCallActivityRuleName(
      FlowNode taskOrCallActivity, String incomingFlowId) {
    if (taskOrCallActivity.getIncomingFlows().count() > 1) {
      return taskOrCallActivity.getName() + "_" + incomingFlowId;
    }
    return taskOrCallActivity.getName();
  }

  public void deleteTerminatedSubprocess(
      GrooveRuleBuilder ruleBuilder, String eSubprocessName, GrooveNode parentProcessInstance) {
    GrooveNode subProcessInstance =
        ruleBuilder.deleteNode(BPMNToGrooveTransformerConstants.TYPE_PROCESS_SNAPSHOT);
    ruleBuilder.deleteEdge(
        BPMNToGrooveTransformerConstants.NAME,
        subProcessInstance,
        ruleBuilder.contextNode(createStringNodeLabel(eSubprocessName)));
    ruleBuilder.deleteEdge(
        BPMNToGrooveTransformerConstants.SUBPROCESS, parentProcessInstance, subProcessInstance);
    GrooveNode terminated =
        ruleBuilder.deleteNode(BPMNToGrooveTransformerConstants.TYPE_TERMINATED);
    ruleBuilder.deleteEdge(BPMNToGrooveTransformerConstants.STATE, subProcessInstance, terminated);
  }
}
