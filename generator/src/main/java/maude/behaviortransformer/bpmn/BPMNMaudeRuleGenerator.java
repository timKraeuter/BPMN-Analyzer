package maude.behaviortransformer.bpmn;

import behavior.bpmn.AbstractBPMNProcess;
import behavior.bpmn.BPMNCollaboration;
import behavior.bpmn.BPMNProcess;
import com.google.common.collect.Sets;
import java.util.Set;
import maude.behaviortransformer.bpmn.generators.BPMNMaudeEventRuleGenerator;
import maude.behaviortransformer.bpmn.generators.BPMNMaudeGatewayRuleGenerator;
import maude.behaviortransformer.bpmn.generators.BPMNMaudeSubprocessRuleGenerator;
import maude.behaviortransformer.bpmn.generators.BPMNMaudeTaskRuleGenerator;
import maude.behaviortransformer.bpmn.settings.MaudeBPMNGenerationSettings;
import maude.generation.BPMNMaudeRuleBuilder;

public class BPMNMaudeRuleGenerator {
  private final BPMNCollaboration collaboration;
  private final MaudeBPMNGenerationSettings settings;
  private final Set<BPMNProcess> visitedProcessModels;

  // Subgenerators
  private final BPMNMaudeTaskRuleGenerator taskRuleGenerator;
  private final BPMNMaudeEventRuleGenerator eventRuleGenerator;
  private final BPMNMaudeGatewayRuleGenerator gatewayRuleGenerator;
  private final BPMNMaudeSubprocessRuleGenerator subprocessRuleGenerator;

  BPMNMaudeRuleGenerator(
      BPMNMaudeRuleBuilder ruleBuilder,
      BPMNCollaboration collaboration,
      MaudeBPMNGenerationSettings settings) {
    this.collaboration = collaboration;
    this.settings = settings;
    visitedProcessModels = Sets.newHashSet();

    taskRuleGenerator = new BPMNMaudeTaskRuleGenerator(this, ruleBuilder);
    eventRuleGenerator = new BPMNMaudeEventRuleGenerator(this, ruleBuilder);
    gatewayRuleGenerator = new BPMNMaudeGatewayRuleGenerator(this, ruleBuilder);
    subprocessRuleGenerator = new BPMNMaudeSubprocessRuleGenerator(this, ruleBuilder);
  }

  public BPMNCollaboration getCollaboration() {
    return collaboration;
  }

  public void generateRules() {
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
        .forEach(node -> node.accept(new MaudeRuleGenerationFlowNodeVisitor(this, process)));
  }

  public BPMNMaudeTaskRuleGenerator getTaskRuleGenerator() {
    return taskRuleGenerator;
  }

  public BPMNMaudeEventRuleGenerator getEventRuleGenerator() {
    return eventRuleGenerator;
  }

  public BPMNMaudeGatewayRuleGenerator getGatewayRuleGenerator() {
    return gatewayRuleGenerator;
  }

  public BPMNMaudeSubprocessRuleGenerator getSubprocessRuleGenerator() {
    return subprocessRuleGenerator;
  }

  public Set<BPMNProcess> getVisitedProcessModels() {
    return visitedProcessModels;
  }

  public MaudeBPMNGenerationSettings getSettings() {
    return settings;
  }
}
