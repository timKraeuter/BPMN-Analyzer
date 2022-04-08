package groove.behaviorTransformer.bpmn;

import static groove.behaviorTransformer.GrooveTransformer.TYPE;

public class BPMNToGrooveTransformerConstants {
    // Rule prefixes/suffixes
    static final String END = "_end";
    static final String THROW = "Throw_";
    static final String CATCH = "Catch_";
    static final String START = "_start";
    // Node names
    static final String TYPE_TOKEN = TYPE + "Token";
    static final String TYPE_PROCESS_SNAPSHOT = TYPE + "ProcessSnapshot";
    static final String TYPE_RUNNING = TYPE + "Running";
    static final String TYPE_TERMINATED = TYPE + "Terminated";
    static final String TYPE_DECISION = TYPE + "Decision";
    static final String TYPE_MESSAGE = TYPE + "Message";
    // Edge names
    static final String POSITION = "position";
    static final String STATE = "state";
    static final String TOKENS = "tokens";
    static final String MESSAGES = "messages";
    static final String DECISIONS = "decisions";
    static final String DECISION = "decision";
    static final String SUBPROCESS = "subprocess";
    static final String NAME = "name";
    static final String FIXED_RULES_AND_TYPE_GRAPH_DIR = "/BPMNFixedRulesAndTypeGraph";

    private BPMNToGrooveTransformerConstants() {
        // Only constants
    }
}
