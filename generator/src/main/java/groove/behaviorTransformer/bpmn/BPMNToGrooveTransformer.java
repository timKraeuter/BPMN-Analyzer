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
import java.io.InputStream;
import java.util.stream.Stream;

import static groove.behaviorTransformer.GrooveTransformerHelper.createStringNodeLabel;
import static groove.behaviorTransformer.bpmn.BPMNToGrooveTransformerConstants.*;

public class BPMNToGrooveTransformer implements GrooveTransformer<BPMNCollaboration> {

    public static final String TYPE_GRAPH_FILE_NAME = "bpmn_e_model.gty";
    public static final String TERMINATE_RULE_FILE_NAME = "Terminate.gpr";
    // Graph conditions for model-checking
    public static final String ALL_TERMINATED_FILE_NAME = "AllTerminated.gpr";
    public static final String UNSAFE_FILE_NAME = "Unsafe.gpr";

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
    public void generateAndWriteRulesFurther(BPMNCollaboration collaboration, boolean addPrefix, File targetFolder) {
        this.copyTypeGraphAndFixedRules(targetFolder);
    }

    private void copyTypeGraphAndFixedRules(File targetFolder) {
        InputStream typeGraph = this.getClass().getResourceAsStream(FIXED_RULES_AND_TYPE_GRAPH_DIR + TYPE_GRAPH_FILE_NAME);
        InputStream terminateRule = this.getClass().getResourceAsStream(FIXED_RULES_AND_TYPE_GRAPH_DIR + TERMINATE_RULE_FILE_NAME);
        InputStream unsafeGraph = this.getClass().getResourceAsStream(FIXED_RULES_AND_TYPE_GRAPH_DIR + UNSAFE_FILE_NAME);
        InputStream allterminatedGraph = this.getClass().getResourceAsStream(FIXED_RULES_AND_TYPE_GRAPH_DIR + ALL_TERMINATED_FILE_NAME);
        try {
            FileUtils.copyInputStreamToFile(typeGraph, new File(targetFolder, TYPE_GRAPH_FILE_NAME));
            FileUtils.copyInputStreamToFile(terminateRule, new File(targetFolder, TERMINATE_RULE_FILE_NAME));
            FileUtils.copyInputStreamToFile(unsafeGraph, new File(targetFolder, UNSAFE_FILE_NAME));
            FileUtils.copyInputStreamToFile(allterminatedGraph, new File(targetFolder, ALL_TERMINATED_FILE_NAME));
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean isLayoutActivated() {
        return true; // TODO: implement layout as parameter!
    }
}
