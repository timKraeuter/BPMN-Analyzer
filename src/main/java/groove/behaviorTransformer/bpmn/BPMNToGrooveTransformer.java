package groove.behaviorTransformer.bpmn;

import behavior.bpmn.BPMNCollaboration;
import behavior.bpmn.Process;
import behavior.bpmn.events.StartEventType;
import groove.behaviorTransformer.GrooveTransformer;
import groove.graph.GrooveGraph;
import groove.graph.GrooveGraphBuilder;
import groove.graph.GrooveNode;
import groove.graph.rule.GrooveGraphRule;
import groove.graph.rule.GrooveRuleBuilder;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.stream.Stream;

import static groove.behaviorTransformer.GrooveTransformerHelper.createStringNodeLabel;
import static groove.behaviorTransformer.bpmn.BPMNToGrooveTransformerConstants.*;

public class BPMNToGrooveTransformer implements GrooveTransformer<BPMNCollaboration> {

    @Override
    public void generateAndWriteRulesFurther(BPMNCollaboration collaboration, boolean addPrefix, File targetFolder) {
        this.copyTypeGraphAndFixedRules(targetFolder);
    }

    private void copyTypeGraphAndFixedRules(File targetFolder) {
        //noinspection ConstantConditions must be present!. Otherwise, tests will also fail!
        File sourceDirectory = new File(this.getClass().getResource(FIXED_RULES_AND_TYPE_GRAPH_DIR).getFile());
        try {
            FileUtils.copyDirectory(sourceDirectory, targetFolder);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public GrooveGraph generateStartGraph(BPMNCollaboration collaboration, boolean addPrefix) {
        // TODO: Add prefix if needed!
        GrooveGraphBuilder startGraphBuilder = new GrooveGraphBuilder().setName(collaboration.getName());

        collaboration.getParticipants().stream().filter(process -> process.getStartEvent() != null).forEach(process -> {
            if (process.getStartEvent().getType() == StartEventType.NONE) {
                // TODO: Maybe reuse helper method?
                GrooveNode processInstance = new GrooveNode(TYPE_PROCESS_SNAPSHOT);
                GrooveNode processName = new GrooveNode(createStringNodeLabel(process.getName()));
                startGraphBuilder.addEdge(NAME, processInstance, processName);
                GrooveNode running = new GrooveNode(TYPE_RUNNING);
                startGraphBuilder.addEdge(STATE, processInstance, running);
                GrooveNode startToken = new GrooveNode(TYPE_TOKEN);
                GrooveNode tokenName = new GrooveNode(createStringNodeLabel(getStartEventTokenName(process)));
                startGraphBuilder.addEdge(POSITION, startToken, tokenName);
                startGraphBuilder.addEdge(TOKENS, processInstance, startToken);
            }
        });

        return startGraphBuilder.build();
    }

    private String getStartEventTokenName(Process process) {
        return process.getName() + "_" + process.getStartEvent().getName();
    }

    @Override
    public Stream<GrooveGraphRule> generateRules(BPMNCollaboration collaboration, boolean addPrefix) {
        GrooveRuleBuilder ruleBuilder = new GrooveRuleBuilder(collaboration, addPrefix);
        BPMNRuleGenerator bpmnRuleGenerator = new BPMNRuleGenerator(ruleBuilder, collaboration);

        return bpmnRuleGenerator.getRules();
    }

    @Override
    public boolean isLayoutActivated() {
        return true; // TODO: implement layout as parameter!
    }
}
