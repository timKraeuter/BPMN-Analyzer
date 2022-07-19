package maude.behaviortransformer.bpmn;

import behavior.bpmn.AbstractProcess;
import behavior.bpmn.FlowNode;
import behavior.bpmn.SequenceFlow;
import behavior.bpmn.activities.Activity;
import behavior.bpmn.events.StartEvent;
import maude.generation.MaudeObject;
import maude.generation.MaudeObjectBuilder;

import java.util.stream.Collectors;

public class BPMNToMaudeTransformerHelper {
    public static final String ANY_TOKEN = "T";
    public static final String ANY_OTHER_TOKENS = " " + ANY_TOKEN;

    public static final String RULE_NAME_NAME_ID_FORMAT = "%s_%s";
    public static final String RULE_NAME_ID_FORMAT = "%s";
    private static final String TOKEN_FORMAT = "\"%s (%s)\""; // Name of the FlowElement followed by id.
    private static final String ENQUOTE_FORMAT = "\"%s\""; // Id of the FlowElement.

    private BPMNToMaudeTransformerHelper() {
    }
    public static String getFlowNodeNameAndID(FlowNode flowNode) {
        if (flowNode.getName() == null || flowNode.getName().isBlank()) {
            return String.format(RULE_NAME_ID_FORMAT, flowNode.getId());
        }
        return String.format(RULE_NAME_NAME_ID_FORMAT, flowNode.getName(), flowNode.getId());
    }

    public static String getStartEventTokenName(StartEvent event) {
        return String.format(TOKEN_FORMAT, event.getName(), event.getId());
    }

    public static String getOutgoingTokensForFlowNode(FlowNode flowNode) {
        return flowNode.getOutgoingFlows()
                       .map(BPMNToMaudeTransformerHelper::getTokenForSequenceFlow)
                       .collect(Collectors.joining(" "));
    }

    public static String getTokenForSequenceFlow(SequenceFlow sequenceFlow) {
        String nameOrDescriptiveName = sequenceFlow.getName() ==
                                       null ||
                                       sequenceFlow.getName().isBlank() ?
                sequenceFlow.getDescriptiveName() :
                sequenceFlow.getName();
        return String.format(TOKEN_FORMAT, nameOrDescriptiveName, sequenceFlow.getId());
    }

    public static String getTokenForActivity(Activity activity) {
        return activity.getName() ==
               null ||
               activity.getName().isBlank() ?
                String.format(ENQUOTE_FORMAT, activity.getId()) :
                String.format(TOKEN_FORMAT, activity.getName(), activity.getId());
    }

    public static MaudeObject createProcessSnapshotObjectNoSubProcess(MaudeObjectBuilder maudeObjectBuilder,
                                                                      AbstractProcess process,
                                                                      String tokens) {
        return createProcessSnapshotObject(maudeObjectBuilder, process, "none", tokens);
    }

    public static MaudeObject createProcessSnapshotObjectAnySubProcess(MaudeObjectBuilder maudeObjectBuilder,
                                                                       AbstractProcess process,
                                                                       String tokens) {
        return createProcessSnapshotObject(maudeObjectBuilder, process, "S", tokens);
    }

    public static MaudeObject createProcessSnapshotObject(MaudeObjectBuilder maudeObjectBuilder,
                                                          AbstractProcess process,
                                                          String subprocesses,
                                                          String tokens) {
        return maudeObjectBuilder
                .oid(String.format(ENQUOTE_FORMAT, process.getName()))
                .oidType("ProcessSnapshot")
                .addAttributeValue("tokens", String.format("(%s)", tokens))
                .addAttributeValue("subprocesses", subprocesses)
                .addAttributeValue("state", "Running")
                .build();
    }
}
