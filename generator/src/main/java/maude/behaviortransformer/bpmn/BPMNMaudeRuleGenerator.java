package maude.behaviortransformer.bpmn;

import behavior.bpmn.AbstractProcess;
import behavior.bpmn.BPMNCollaboration;
import behavior.bpmn.Process;
import com.google.common.collect.Sets;
import maude.behaviortransformer.bpmn.generators.BPMNMaudeEventRuleGenerator;
import maude.behaviortransformer.bpmn.generators.BPMNMaudeGatewayRuleGenerator;
import maude.behaviortransformer.bpmn.generators.BPMNMaudeSubprocessRuleGenerator;
import maude.behaviortransformer.bpmn.generators.BPMNMaudeTaskRuleGenerator;
import maude.generation.MaudeRuleBuilder;

import java.util.Set;

public class BPMNMaudeRuleGenerator {
    private final BPMNCollaboration collaboration;
    private final Set<Process> visitedProcessModels;

    // Subgenerators
    private final BPMNMaudeTaskRuleGenerator taskRuleGenerator;
    private final BPMNMaudeEventRuleGenerator eventRuleGenerator;
    private final BPMNMaudeGatewayRuleGenerator gatewayRuleGenerator;
    private final BPMNMaudeSubprocessRuleGenerator subprocessRuleGenerator;

    BPMNMaudeRuleGenerator(MaudeRuleBuilder ruleBuilder, BPMNCollaboration collaboration) {
        this.collaboration = collaboration;
        visitedProcessModels = Sets.newHashSet();

        taskRuleGenerator = new BPMNMaudeTaskRuleGenerator(ruleBuilder);
        eventRuleGenerator = new BPMNMaudeEventRuleGenerator(ruleBuilder);
        gatewayRuleGenerator = new BPMNMaudeGatewayRuleGenerator(ruleBuilder);
        subprocessRuleGenerator = new BPMNMaudeSubprocessRuleGenerator(this, ruleBuilder);
    }
    public void generateRules() {
        collaboration.getParticipants().forEach(process -> {
            if (!visitedProcessModels.contains(process)) {
                visitedProcessModels.add(process);
                generateRulesForProcess(process);
            }
        });
    }

    public void generateRulesForProcess(AbstractProcess process) {
        process.getFlowNodes().forEach(node -> node.accept(new MaudeRuleGenerationFlowNodeVisitor(this, process)));
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
    public Set<Process> getVisitedProcessModels() {
        return visitedProcessModels;
    }
}
