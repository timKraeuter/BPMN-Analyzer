package maude.behaviortransformer.bpmn;

public final class BPMNToMaudeTransformerConstants {
    private BPMNToMaudeTransformerConstants() {
        // Only constants
    }

    public static final String NEW_LINE = "\r\n    ";
    public static final String WHITE_SPACE = " ";
    public static final String PROCESSES = "processes";
    public static final String BPMN_SYSTEM = "BPMNSystem";

    public static final String PS = "PS";
    public static final String ANY_PROCESS = "P";
    public static final String ANY_TOKENS = "T";
    public static final String ANY_SIGNALS = "SIG";
    public static final String ANY_SUBPROCESSES = "S";
    public static final String ANY_MESSAGES = "M";
    public static final String ANY_OTHER_TOKENS = WHITE_SPACE + ANY_TOKENS;
    public static final String ANY_OTHER_SIGNALS = WHITE_SPACE + ANY_SIGNALS;
    public static final String ANY_OTHER_SUBPROCESSES = WHITE_SPACE + ANY_SUBPROCESSES;
    public static final String ANY_OTHER_MESSAGES = WHITE_SPACE + ANY_MESSAGES;
    public static final String ANY_OTHER_PROCESSES = WHITE_SPACE + ANY_PROCESS;
    public static final String NONE = "none";
    public static final String RULE_NAME_NAME_ID_FORMAT = "%s_%s";
    public static final String RULE_NAME_ID_FORMAT = "%s";
    public static final String TOKEN_FORMAT = "\"%s (%s)\""; // Name of the FlowElement followed by id.
    public static final String SIGNAL_OCCURRENCE_FORMAT = "\"%s (%s)_signal\"";
    public static final String TOKEN_FORMAT_ONLY_ID = "\"%s\""; // Name of the FlowElement followed by id.
    public static final String SIGNAL_OCCURRENCE_FORMAT_ONLY_ID = "\"%s_signal\""; // Name of the FlowElement followed by id.
    public static final String ENQUOTE_FORMAT = "\"%s\""; // Id of the FlowElement.
    public static final String BRACKET_FORMAT = "(%s)";
    public static final String RUNNING = "Running";
    public static final String TERMINATED = "Terminated";
}
