package maude.behaviortransformer.bpmn;

import behavior.bpmn.*;
import behavior.bpmn.activities.Activity;
import behavior.bpmn.events.StartEvent;
import maude.generation.MaudeObject;
import maude.generation.MaudeObjectBuilder;

import java.util.Set;
import java.util.stream.Collectors;

public class BPMNToMaudeTransformerHelper {
    public static final String ANY_TOKENS = "T";
    public static final String ANY_SUBPROCESSES = "S";
    public static final String ANY_MESSAGES = "M";
    public static final String ANY_OTHER_TOKENS = " " + ANY_TOKENS;
    public static final String ANY_OTHER_SUBPROCESSES = " " + ANY_SUBPROCESSES;
    public static final String NONE = "none";

    public static final String RULE_NAME_NAME_ID_FORMAT = "%s_%s";
    public static final String RULE_NAME_ID_FORMAT = "%s";
    private static final String TOKEN_FORMAT = "\"%s (%s)\""; // Name of the FlowElement followed by id.
    private static final String TOKEN_FORMAT_ONLY_ID = "\"%s\""; // Name of the FlowElement followed by id.
    public static final String ENQUOTE_FORMAT = "\"%s\""; // Id of the FlowElement.
    public static final String BRACKET_FORMAT = "(%s)";
    public static final String RUNNING = "Running";
    public static final String TERMINATED = "Terminated";

    private BPMNToMaudeTransformerHelper() {
    }

    public static String getFlowNodeRuleNameWithIncFlow(FlowNode taskOrCallActivity, String incomingFlowId) {
        if (taskOrCallActivity.getIncomingFlows().count() > 1) {
            return String.format(RULE_NAME_NAME_ID_FORMAT, getFlowNodeNameAndID(taskOrCallActivity), incomingFlowId);
        }
        return getFlowNodeNameAndID(taskOrCallActivity);
    }

    public static String getFlowNodeNameAndID(FlowNode flowNode) {
        if (flowNode.getName() == null || flowNode.getName().isBlank()) {
            return String.format(RULE_NAME_ID_FORMAT, flowNode.getId());
        }
        return String.format(RULE_NAME_NAME_ID_FORMAT, flowNode.getName(), flowNode.getId());
    }

    public static String getTokenForFlowNode(FlowNode flowNode) {
        if (flowNode.getName() == null || flowNode.getName().isBlank()) {
            return String.format(TOKEN_FORMAT_ONLY_ID, flowNode.getId());
        }
        return String.format(TOKEN_FORMAT, flowNode.getName(), flowNode.getId());
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

    public static MaudeObject createTerminatedProcessSnapshot(MaudeObjectBuilder maudeObjectBuilder,
                                                              AbstractProcess process) {
        return createProcessSnapshotObject(maudeObjectBuilder,
                                           process,
                                           NONE,
                                           NONE,
                                           NONE,
                                           TERMINATED);
    }


    public static MaudeObject createProcessSnapshotObjectNoSubProcess(MaudeObjectBuilder maudeObjectBuilder,
                                                                      AbstractProcess process,
                                                                      String tokens) {
        return createProcessSnapshotObject(maudeObjectBuilder, process, NONE, tokens, ANY_MESSAGES, RUNNING);
    }

    public static MaudeObject createProcessSnapshotObjectAnySubProcessAndMessages(MaudeObjectBuilder maudeObjectBuilder,
                                                                                  AbstractProcess process,
                                                                                  String tokens) {
        return createProcessSnapshotObject(maudeObjectBuilder,
                                           process,
                                           ANY_SUBPROCESSES,
                                           tokens,
                                           ANY_MESSAGES,
                                           RUNNING);
    }

    public static MaudeObject createProcessSnapshotObjectAnySubProcess(MaudeObjectBuilder maudeObjectBuilder,
                                                                                  AbstractProcess process,
                                                                                  String tokens,
                                                                                  String messages) {
        return createProcessSnapshotObject(maudeObjectBuilder,
                                           process,
                                           ANY_SUBPROCESSES,
                                           tokens,
                                           messages,
                                           RUNNING);
    }

    public static MaudeObject createProcessSnapshotObjectAnyMessages(MaudeObjectBuilder maudeObjectBuilder,
                                                                     AbstractProcess process,
                                                                     String subprocesses,
                                                                     String tokens,
                                                                     String state) {
        return createProcessSnapshotObject(maudeObjectBuilder, process, subprocesses, tokens, ANY_MESSAGES, state);
    }

    public static MaudeObject createProcessSnapshotObject(MaudeObjectBuilder maudeObjectBuilder,
                                                          AbstractProcess process,
                                                          String subprocesses,
                                                          String tokens,
                                                          String messages,
                                                          String state) {
        return maudeObjectBuilder
                .oid(String.format(ENQUOTE_FORMAT, process.getName()))
                .oidType("ProcessSnapshot")
                .addAttributeValue("tokens", String.format(BRACKET_FORMAT, tokens))
                .addAttributeValue("messages", String.format(BRACKET_FORMAT, messages))
                .addAttributeValue("subprocesses", String.format(BRACKET_FORMAT, subprocesses))
                .addAttributeValue("state", state)
                .build();
    }

    public static String getIncomingMessagesForFlowNode(FlowNode flowNode,
                                                        BPMNCollaboration collaboration) {
        Set<MessageFlow> incomingMessageFlows = collaboration.getIncomingMessageFlows(flowNode);
        if (incomingMessageFlows.isEmpty()) {
            return NONE;
        }
        // TODO: Check reading message flows. They should have ids too!
        return incomingMessageFlows.stream()
                                   .map(messageFlow -> String.format(ENQUOTE_FORMAT, messageFlow.getName()))
                                   .collect(Collectors.joining(" "));
    }
}
