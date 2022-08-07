package maude.behaviortransformer.bpmn;

import behavior.bpmn.BPMNCollaboration;
import behavior.bpmn.Process;
import behavior.bpmn.events.StartEventType;
import maude.generation.MaudeObject;
import maude.generation.MaudeObjectBuilder;
import maude.generation.MaudeRule;
import maude.generation.MaudeRuleBuilder;
import org.apache.commons.text.StringSubstitutor;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static groove.behaviortransformer.bpmn.BPMNToGrooveTransformerConstants.*;

public class BPMNToMaudeTransformer implements BPMNToMaudeTransformerHelper {
    public static final String DELIMITER = "\r\n    ";
    private final BPMNCollaboration collaboration;
    private final MaudeRuleBuilder ruleBuilder;
    private final MaudeObjectBuilder objectBuilder;

    private static final String MODULE_TEMPLATE = "load model-checker.maude .\r\n" +
                                                  "\r\n" +
                                                  "--- Multiset implementation could be extracted as well.\r\n" +
                                                  "fmod MSET is pr\r\n" +
                                                  "    STRING .\r\n" +
                                                  "    sorts NeMSet MSet .\r\n" +
                                                  "    subsort String < NeMSet < MSet .\r\n" +
                                                  "\r\n" +
                                                  "    op none : -> MSet [ctor] .\r\n" +
                                                  "    op __ : MSet MSet -> MSet [ctor assoc comm id: none] .\r\n" +
                                                  "    op __ : NeMSet MSet -> NeMSet [ctor ditto] .\r\n" +
                                                  "    op __ : MSet NeMSet -> NeMSet [ctor ditto] .\r\n" +
                                                  "endfm\r\n" +
                                                  "\r\n" +
                                                  "mod BPMN-EXECUTION is\r\n" +
                                                  "    pr MSET .\r\n" +
                                                  "    pr STRING .\r\n" +
                                                  "    pr CONFIGURATION .\r\n" +
                                                  "\r\n" +
                                                  "    sort ProcessState .\r\n" +
                                                  "    subsort String < Oid .\r\n" +
                                                  "\r\n" +
                                                  "    --- Processes\r\n" +
                                                  "    ops Running, Terminated : -> ProcessState [ctor] .\r\n" +
                                                  "    op tokens :_ : MSet -> Attribute [ctor] .\r\n" +
                                                  "    op subprocesses :_ : Configuration -> Attribute [ctor] .\r\n" +
                                                  "    op state :_ : ProcessState -> Attribute [ctor] .\r\n" +
                                                  "    op ProcessSnapshot : -> Cid [ctor] .\r\n" +
                                                  "\r\n" +
                                                  "    --- Message holder\r\n" +
                                                  "    op MessageHolder : -> Cid [ctor] .\r\n" +
                                                  "    op messages :_ : MSet -> Attribute [ctor] .\r\n" +
                                                  "\r\n" +
                                                  "    op terminate : Configuration -> Configuration .\r\n" +
                                                  "\r\n" +
                                                  "    vars P, P1 : String .\r\n" +
                                                  "    vars T : MSet . --- tokens\r\n" +
                                                  "    vars S : Configuration . --- subprocesses\r\n" +
                                                  "    vars STATE : ProcessState . --- state\r\n" +
                                                  "    var PS : Configuration .\r\n" +
                                                  "\r\n" +
                                                  "    --- NOOP if none\r\n" +
                                                  "    eq terminate(none) = none .\r\n" +
                                                  "    --- NOOP if already terminated\r\n" +
                                                  "    eq terminate(< P : ProcessSnapshot | tokens : T, subprocesses " +
                                                  ": S, state : Terminated >) = < P : ProcessSnapshot | tokens : T, " +
                                                  "subprocesses : S, state : Terminated > .\r\n" +
                                                  "    --- Terminate all subprocesses recursively\r\n" +
                                                  "    eq terminate(< P : ProcessSnapshot | tokens : T, subprocesses " +
                                                  ": S, state : STATE > PS) = < P : ProcessSnapshot | tokens : T, " +
                                                  "subprocesses : terminate(S), state : Terminated > terminate(PS) " +
                                                  ".\r\n" +
                                                  "\r\n" +
                                                  "    rl [naturalTerminate] :\r\n" +
                                                  "    < P : ProcessSnapshot | tokens : none, subprocesses : none, " +
                                                  "state : Running >\r\n" +
                                                  "                            =>\r\n" +
                                                  "    < P : ProcessSnapshot | tokens : none, subprocesses : none, " +
                                                  "state : Terminated > .\r\n" +
                                                  "endm\r\n" +
                                                  "\r\n" +
                                                  "mod BPMN-EXECUTION-${name} is\r\n" +
                                                  "    pr BPMN-EXECUTION .\r\n" +
                                                  "\r\n" +
                                                  "    --- Generated variables\r\n" +
                                                  "    ${tokens}\r\n" +
                                                  "    ${messages}\r\n" +
                                                  "    ${subprocesses}\r\n" +
                                                  "\r\n" +
                                                  "    --- Generated rules\r\n" +
                                                  "    ${rules}\r\n" +
                                                  "\r\n" +
                                                  "    --- Start configuration which would be generated\r\n" +
                                                  "    op init : -> Configuration .\r\n" +
                                                  "    eq init = ${init} .\r\n" +
                                                  "endm\r\n" +
                                                  "\r\n" +
                                                  "mod BPMN-PREDS is\r\n" +
                                                  "    pr BPMN-EXECUTION-${name} .\r\n" +
                                                  "    pr SATISFACTION .\r\n" +
                                                  "    subsort Configuration < State .\r\n" +
                                                  "\r\n" +
                                                  "    var C : Configuration .\r\n" +
                                                  "    var P : Prop .\r\n" +
                                                  "    var X : Oid .\r\n" +
                                                  "    var T : MSet .\r\n" +
                                                  "    var T1 : NeMSet .\r\n" +
                                                  "    var S : Configuration .\r\n" +
                                                  "    var State : ProcessState .\r\n" +
                                                  "\r\n" +
                                                  "\r\n" +
                                                  "    op allTerminated : -> Prop .\r\n" +
                                                  "    eq < X : ProcessSnapshot | tokens : T, subprocesses : S, state" +
                                                  " : Running > C |= allTerminated = false .\r\n" +
                                                  "    eq C |= allTerminated = true [owise] .\r\n" +
                                                  "\r\n" +
                                                  "    op unsafe : -> Prop .\r\n" +
                                                  "    eq < X : ProcessSnapshot | tokens : (T1 T1 T), subprocesses : " +
                                                  "S, state : State > C |= unsafe = true .\r\n" +
                                                  "    eq C |= unsafe = false [owise] .\r\n" +
                                                  "\r\n" +
                                                  "    --- Generated atomic propositions\r\n" +
                                                  "    ${atomicPropositions}\r\n" +
                                                  "endm\r\n" +
                                                  "\r\n" +
                                                  "mod BPMN-CHECK is\r\n" +
                                                  "    pr BPMN-PREDS .\r\n" +
                                                  "    pr MODEL-CHECKER .\r\n" +
                                                  "    pr LTL-SIMPLIFIER .\r\n" +
                                                  "endm\r\n" +
                                                  "\r\n" +
                                                  "red modelCheck(init, ${ltlQuery}) .\r\n";

    public BPMNToMaudeTransformer(BPMNCollaboration collaboration) {
        this.collaboration = collaboration;
        ruleBuilder = new MaudeRuleBuilder();
        ruleBuilder.addVar(TOKENS, MSET, "T");
        ruleBuilder.addVar(MESSAGES, MSET, "M");
        ruleBuilder.addVar(SUBPROCESSES, CONFIGURATION, "S");
        objectBuilder = new MaudeObjectBuilder();
    }

    public String generate(String ltlQuery) {
        Map<String, String> substitutionValues = new HashMap<>();
        substitutionValues.put("name", collaboration.getName());
        substitutionValues.put("init", this.makeInit());
        substitutionValues.put("rules", this.makeRules());
        substitutionValues.put(TOKENS, ruleBuilder.getVarsForGroup(TOKENS));
        substitutionValues.put(MESSAGES, ruleBuilder.getVarsForGroup(MESSAGES));
        substitutionValues.put(SUBPROCESSES, ruleBuilder.getVarsForGroup(SUBPROCESSES));
        substitutionValues.put("atomicPropositions", "--- no propositions"); // Add at some point
        substitutionValues.put("ltlQuery", ltlQuery);
        return new StringSubstitutor(substitutionValues).replace(MODULE_TEMPLATE);
    }

    private String makeInit() {
        String processes = collaboration.getParticipants().stream()
                                        .filter(process -> process.getStartEvents().stream().anyMatch(startEvent ->
                                                                                                              startEvent.getType() ==
                                                                                                              StartEventType.NONE))
                                        .map(process -> {
                                            MaudeObject maudeObject =
                                                    createProcessSnapshotObjectNoSubProcess(
                                                            process,
                                                            this.createStartTokens(process));
                                            return maudeObject.generateObjectString();
                                        })
                                        .collect(Collectors.joining(DELIMITER));
        return createEmptyMessageHolder().generateObjectString() + DELIMITER + processes;
    }

    private String createStartTokens(Process process) {
        // Add a token for each none start event
        return process.getStartEvents().stream()
                      .filter(startEvent -> startEvent.getType() == StartEventType.NONE)
                      .map(this::getStartEventTokenName)
                      .collect(Collectors.joining(" "));
    }

    private String makeRules() {
        new BPMNMaudeRuleGenerator(ruleBuilder, collaboration).generateRules();

        return ruleBuilder.createdRules()
                          .map(MaudeRule::generateRuleString)
                          .collect(Collectors.joining(DELIMITER));
    }

    @Override
    public MaudeRuleBuilder getRuleBuilder() {
        return ruleBuilder;
    }

    @Override
    public MaudeObjectBuilder getObjectBuilder() {
        return objectBuilder;
    }
}
