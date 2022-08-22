package maude.behaviortransformer.bpmn;

public interface BPMNToMaudeTransformerConstants {
    String NEW_LINE = "\r\n    ";
    String WHITE_SPACE = " ";
    String PROCESSES = "processes";
    String BPMN_SYSTEM = "BPMNSystem";

    String PS = "PS";
    String ANY_PROCESS = "P";
    String ANY_TOKENS = "T";
    String ANY_SIGNALS = "SIG";
    String ANY_SUBPROCESSES = "S";
    String ANY_MESSAGES = "M";
    String ANY_OTHER_TOKENS = WHITE_SPACE + ANY_TOKENS;
    String ANY_OTHER_SIGNALS = WHITE_SPACE + ANY_SIGNALS;
    String ANY_OTHER_SUBPROCESSES = WHITE_SPACE + ANY_SUBPROCESSES;
    String ANY_OTHER_MESSAGES = WHITE_SPACE + ANY_MESSAGES;
    String ANY_OTHER_PROCESSES = WHITE_SPACE + ANY_PROCESS;
    String NONE = "none";
    String RULE_NAME_NAME_ID_FORMAT = "%s_%s";
    String RULE_NAME_ID_FORMAT = "%s";
    String TOKEN_FORMAT = "\"%s (%s)\""; // Name of the FlowElement followed by id.
    String SIGNAL_OCCURENCE_FORMAT = "\"%s (%s)_signal\"";
    String TOKEN_FORMAT_ONLY_ID = "\"%s\""; // Name of the FlowElement followed by id.
    String SIGNAL_OCCURENCE_FORMAT_ONLY_ID = "\"%s_signal\""; // Name of the FlowElement followed by id.
    String ENQUOTE_FORMAT = "\"%s\""; // Id of the FlowElement.
    String BRACKET_FORMAT = "(%s)";
    String RUNNING = "Running";
    String TERMINATED = "Terminated";
}
