package maude.generation;

import behavior.bpmn.BPMNCollaboration;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static groove.behaviortransformer.bpmn.BPMNToGrooveTransformerConstants.CONFIGURATION;
import static groove.behaviortransformer.bpmn.BPMNToGrooveTransformerConstants.MESSAGES;
import static maude.behaviortransformer.bpmn.BPMNToMaudeTransformerHelper.*;

/**
 * A special smart Maude rule builder to build BPMN rules for a given collaboration.
 */
public class BPMNMaudeRuleBuilder extends MaudeRuleBuilderBase<BPMNMaudeRuleBuilder> {
    private static final String BPMN_SYSTEM = "BPMNSystem";
    private static final String PROCESSES = "processes";
    public static final String PS = "PS";

    private final MaudeObjectBuilder objectBuilder;
    private final BPMNCollaboration collaboration;
    private final Set<String> consumedMessages;
    private final Set<String> createdMessages;

    private Set<String> signalAll;

    public BPMNMaudeRuleBuilder(BPMNCollaboration collaboration) {
        super();
        setSelfReference(this);
        this.collaboration = collaboration;
        consumedMessages = new LinkedHashSet<>();
        createdMessages = new LinkedHashSet<>();
        objectBuilder = new MaudeObjectBuilder();
        signalAll = new LinkedHashSet<>();
    }

    @Override
    public MaudeRule buildRule() {
        if (ruleName == null || preObjects.isEmpty() || postObjects.isEmpty()) {
            throw new MaudeGenerationException("A rule should have a name and at least one pre/post object");
        }
        if (consumedMessages.isEmpty() && createdMessages.isEmpty() && signalAll == null) {
            return buildProcessesRule();
        }
        return buildSystemRule();
    }

    public void addMessageConsumption(String message) {
        this.consumedMessages.add(message);
    }

    public void addMessageCreation(String message) {
        this.createdMessages.add(message);
    }

    public void addSignalAll(Set<String> signalAll) {
        this.addVar("other processes", CONFIGURATION, PS);
        this.signalAll = signalAll;
    }

    private MaudeRule buildSystemRule() {
        if (signalAll != null && !signalAll.isEmpty()) {
            return buildSystemRuleWithSignalAll();
        }

        // Pre object is a BPMN system
        MaudeObject preObject = createBPMNSystem(getObjectStringAndAddAnyOtherProcesses(preObjects),
                                                 getMessagesString(consumedMessages));

        // Post object is a BPMN system
        MaudeObject postObject = createBPMNSystem(getObjectStringAndAddAnyOtherProcesses(postObjects),
                                                  getMessagesString(createdMessages));
        return createSaveRuleAndResetBuilder(Collections.singleton(preObject), Collections.singleton(postObject));
    }

    private MaudeRule createSaveRuleAndResetBuilder(Set<MaudeObject> preObject, Set<MaudeObject> postObject) {
        MaudeRule maudeRule = new MaudeRule(ruleName,
                                            preObject,
                                            postObject,
                                            condition);
        createdRules.add(maudeRule);
        this.reset();
        return maudeRule;
    }

    private MaudeRule buildSystemRuleWithSignalAll() {
        // Pre object is a BPMN system
        MaudeObject preObject = createBPMNSystem(getObjectString(preObjects) + WHITE_SPACE + PS,
                                                 getMessagesString(consumedMessages));

        if (preObjects.size() != 1) {
            throw new MaudeGenerationException("There must be exactly one pre object in signal throw rules!");
        }
        String postProcesses = String.format("signalAll(%s %s, %s)",
                                             getObjectString(postObjects),
                                             PS,
                                             String.join(WHITE_SPACE, signalAll));
        // Post object is a BPMN system
        MaudeObject postObject = createBPMNSystem(postProcesses, getMessagesString(createdMessages));

        return createSaveRuleAndResetBuilder(Collections.singleton(preObject), Collections.singleton(postObject));
    }

    private String getObjectStringAndAddAnyOtherProcesses(Set<MaudeObject> objects) {
        return getObjectString(objects) + ANY_OTHER_PROCESSES;
    }

    private String getObjectString(Set<MaudeObject> objects) {
        return objects.stream()
                      .map(MaudeObject::generateObjectString)
                      .collect(Collectors.joining(WHITE_SPACE));
    }

    private String getMessagesString(Set<String> messages) {
        if (messages.isEmpty()) {
            return ANY_MESSAGES;
        }
        return String.join(WHITE_SPACE, messages) + ANY_OTHER_MESSAGES;
    }

    public MaudeObject createBPMNSystem(String processes, String messages) {
        String name = collaboration.getName().isBlank() ? "unnamedCollaboration" : collaboration.getName();
        return objectBuilder.oid(name)
                            .oidType(BPMN_SYSTEM)
                            .addAttributeValue(MESSAGES, String.format(BRACKET_FORMAT, messages))
                            .addAttributeValue(PROCESSES, String.format(BRACKET_FORMAT, processes))
                            .build();
    }

    private MaudeRule buildProcessesRule() {
        return createSaveRuleAndResetBuilder(preObjects, postObjects);
    }

    @Override
    void reset() {
        this.ruleName = null;
        this.preObjects = new LinkedHashSet<>();
        this.postObjects = new LinkedHashSet<>();
        consumedMessages.clear();
        createdMessages.clear();
        signalAll = new LinkedHashSet<>();
    }
}
