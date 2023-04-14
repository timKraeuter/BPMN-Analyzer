package groove.behaviortransformer.bpmn;

import static groove.behaviortransformer.GrooveTransformerHelper.createStringNodeLabel;
import static groove.behaviortransformer.bpmn.BPMNToGrooveTransformerConstants.*;

import behavior.bpmn.AbstractBPMNProcess;
import behavior.bpmn.BPMNCollaboration;
import behavior.bpmn.BPMNProcess;
import behavior.bpmn.FlowNode;
import com.google.common.collect.Sets;
import groove.behaviortransformer.bpmn.generators.*;
import groove.graph.GrooveNode;
import groove.graph.rule.GrooveGraphRule;
import groove.graph.rule.GrooveRuleBuilder;
import java.util.Set;
import java.util.stream.Stream;

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

  BPMNRuleGenerator(
      GrooveRuleBuilder ruleBuilder, BPMNCollaboration collaboration) {
    this.ruleBuilder = ruleBuilder;
    this.collaboration = collaboration;
    visitedProcessModels = Sets.newHashSet();

    taskRuleGenerator = new BPMNTaskRuleGeneratorImpl(collaboration, ruleBuilder);
    eventRuleGenerator = new BPMNEventRuleGeneratorImpl(this, ruleBuilder);
    gatewayRuleGenerator = new BPMNGatewayRuleGeneratorImpl(ruleBuilder);
    eventSubprocessRuleGenerator =
        new BPMNEventSubprocessRuleGeneratorImpl(this, ruleBuilder);
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
  public String getTaskOrCallActivityRuleName(FlowNode taskOrCallActivity, String incomingFlowId) {
    if (taskOrCallActivity.getIncomingFlows().count() > 1) {
      return taskOrCallActivity.getName() + "_" + incomingFlowId;
    }
    return taskOrCallActivity.getName();
  }

  public void deleteTerminatedSubprocess(
      GrooveRuleBuilder ruleBuilder, String eSubprocessName, GrooveNode parentProcessInstance) {
    GrooveNode subProcessInstance = ruleBuilder.deleteNode(TYPE_PROCESS_SNAPSHOT);
    ruleBuilder.deleteEdge(
        NAME, subProcessInstance, ruleBuilder.contextNode(createStringNodeLabel(eSubprocessName)));
    ruleBuilder.deleteEdge(SUBPROCESS, parentProcessInstance, subProcessInstance);
    GrooveNode terminated = ruleBuilder.deleteNode(TYPE_TERMINATED);
    ruleBuilder.deleteEdge(STATE, subProcessInstance, terminated);
  }
}
