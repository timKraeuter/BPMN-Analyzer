package groove.behaviorTransformer.bpmn;

import static groove.behaviorTransformer.GrooveTransformer.TYPE;

public class BPMNToGrooveTransformerConstants {
    // Rule prefixes/suffixes
    public static final String END = "_end";
    public static final String THROW = "Throw_";
    public static final String CATCH = "Catch_";
    public static final String START = "_start";
    // Node names
    public static final String TYPE_TOKEN = TYPE + "Token";
    public static final String TYPE_PROCESS_SNAPSHOT = TYPE + "ProcessSnapshot";
    public static final String TYPE_RUNNING = TYPE + "Running";
    public static final String TYPE_TERMINATED = TYPE + "Terminated";
    public static final String TYPE_DECISION = TYPE + "Decision";
    public static final String TYPE_MESSAGE = TYPE + "Message";
    // Edge names
    public static final String POSITION = "position";
    public static final String STATE = "state";
    public static final String TOKENS = "tokens";
    public static final String MESSAGES = "messages";
    public static final String DECISIONS = "decisions";
    public static final String DECISION = "decision";
    public static final String SUBPROCESS = "subprocess";
    public static final String NAME = "name";
    public static final String FIXED_RULES_AND_TYPE_GRAPH_DIR = "/BPMNFixedRulesAndTypeGraph";

    private BPMNToGrooveTransformerConstants() {
        // Only constants
    }
}
