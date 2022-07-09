package maude.behaviortransformer.bpmn;

import behavior.bpmn.AbstractProcess;
import behavior.bpmn.BPMNCollaboration;
import behavior.bpmn.Process;
import com.google.common.collect.Sets;
import maude.behaviortransformer.bpmn.generators.BPMNMaudeEventRuleGenerator;
import maude.behaviortransformer.bpmn.generators.BPMNMaudeTaskRuleGenerator;
import maude.generation.MaudeRuleBuilder;

import java.util.Set;

public class BPMNMaudeRuleGenerator {
    private final BPMNCollaboration collaboration;
    private final Set<Process> visitedProcessModels;

    // Subgenerators
    private final BPMNMaudeTaskRuleGenerator taskRuleGenerator;
    private final BPMNMaudeEventRuleGenerator eventRuleGenerator;

    BPMNMaudeRuleGenerator(MaudeRuleBuilder ruleBuilder, BPMNCollaboration collaboration) {
        this.collaboration = collaboration;
        visitedProcessModels = Sets.newHashSet();

        taskRuleGenerator = new BPMNMaudeTaskRuleGenerator(ruleBuilder);
        eventRuleGenerator = new BPMNMaudeEventRuleGenerator(ruleBuilder);
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
}
