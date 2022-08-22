package maude.behaviortransformer.bpmn;

import behavior.bpmn.BPMNCollaboration;
import behavior.bpmn.Process;
import behavior.bpmn.events.StartEventType;
import maude.behaviortransformer.bpmn.settings.MaudeBPMNGenerationSettings;
import maude.generation.BPMNMaudeRuleBuilder;
import maude.generation.MaudeObject;
import maude.generation.MaudeObjectBuilder;
import maude.generation.MaudeRule;
import org.apache.commons.text.StringSubstitutor;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static groove.behaviortransformer.bpmn.BPMNToGrooveTransformerConstants.*;
import static maude.behaviortransformer.bpmn.BPMNToMaudeTransformerConstants.*;

public class BPMNToMaudeTransformer implements BPMNToMaudeTransformerHelper {
    private final BPMNCollaboration collaboration;
    private final BPMNMaudeRuleBuilder ruleBuilder;
    private final MaudeBPMNGenerationSettings settings;
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
                                                  "\r\n" +
                                                  "    op contains : MSet String -> Bool .\r\n" +
                                                  "\r\n" +
                                                  "    vars X Y : String .\r\n" +
                                                  "    var S S1 : MSet .\r\n" +
                                                  "\r\n" +
                                                  "    eq contains(none, X) = false .\r\n" +
                                                  "    eq contains(X S, X) = true .\r\n" +
                                                  "    ceq contains(Y S, X) = contains(S, X) if X =/= Y .\r\n" +
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
                                                  "    --- BPMNSystem\r\n" +
                                                  "    op BPMNSystem : -> Cid [ctor] .\r\n" +
                                                  "    op messages :_ : MSet -> Attribute [ctor] .\r\n" +
                                                  "    op processes :_ : Configuration -> Attribute [ctor] .\r\n" +
                                                  "\r\n" +
                                                  "    --- Processes\r\n" +
                                                  "    op ProcessSnapshot : -> Cid [ctor] .\r\n" +
                                                  "    op tokens :_ : MSet -> Attribute [ctor] .\r\n" +
                                                  "    op signals :_ : MSet -> Attribute [ctor] .\r\n" +
                                                  "    op subprocesses :_ : Configuration -> Attribute [ctor] .\r\n" +
                                                  "    ops Running, Terminated : -> ProcessState [ctor] .\r\n" +
                                                  "    op state :_ : ProcessState -> Attribute [ctor] .\r\n" +
                                                  "\r\n" +
                                                  "    --- Auxiliary\r\n" +
                                                  "    op signalAll : Configuration MSet -> Configuration .\r\n" +
                                                  "    op signal : MSet MSet -> MSet .\r\n" +
                                                  "    op terminate : Configuration -> Configuration .\r\n" +
                                                  "\r\n" +
                                                  "    vars P P1 : String .\r\n" +
                                                  "    vars T T1 : MSet . --- tokens\r\n" +
                                                  "    vars SIG : MSet . --- signals\r\n" +
                                                  "    vars S : Configuration . --- subprocesses\r\n" +
                                                  "    vars STATE : ProcessState . --- state\r\n" +
                                                  "    var PS : Configuration .\r\n" +
                                                  "\r\n" +
                                                  "    eq signalAll(none, T) = none .\r\n" +
                                                  "    eq signalAll(< P : ProcessSnapshot | tokens : T, signals : " +
                                                  "SIG, subprocesses : S, state : STATE > PS, T1) = < P : " +
                                                  "ProcessSnapshot | tokens : T, signals : (SIG signal(T, T1)), " +
                                                  "subprocesses : signalAll(S, T1), state : STATE > signalAll(PS, T1)" +
                                                  " .\r\n" +
                                                  "\r\n" +
                                                  "    ceq signal(P T, T1) = (P + \"_signal\") signal(T, T1) if " +
                                                  "contains(T1, P) .\r\n" +
                                                  "    eq signal(P T, T1) = signal(T, T1) [owise] .\r\n" +
                                                  "    eq signal(none, T1) = none .\r\n" +
                                                  "\r\n" +
                                                  "    eq terminate(none) = none .\r\n" +
                                                  "    --- NOOP if already terminated\r\n" +
                                                  "    eq terminate(< P : ProcessSnapshot | tokens : T, signals : " +
                                                  "SIG, subprocesses : S, state : Terminated >) = < P : " +
                                                  "ProcessSnapshot | tokens : T, signals : SIG, subprocesses : S, " +
                                                  "state : Terminated > .\r\n" +
                                                  "    --- Terminate all subprocesses recursively\r\n" +
                                                  "    eq terminate(< P : ProcessSnapshot | tokens : T, signals : " +
                                                  "SIG, subprocesses : S, state : STATE > PS) = < P : ProcessSnapshot" +
                                                  " | tokens : T, signals : SIG, subprocesses : terminate(S), state :" +
                                                  " Terminated > terminate(PS) .\r\n" +
                                                  "\r\n" +
                                                  "    rl [naturalTerminate] :\r\n" +
                                                  "    < P : ProcessSnapshot | tokens : none, signals : SIG, " +
                                                  "subprocesses : none, state : Running >\r\n" +
                                                  "                            =>\r\n" +
                                                  "    < P : ProcessSnapshot | tokens : none, signals : SIG, " +
                                                  "subprocesses : none, state : Terminated > .\r\n" +
                                                  "endm\r\n" +
                                                  "\r\n" +
                                                  "mod BPMN-EXECUTION-${name} is\r\n" +
                                                  "    pr BPMN-EXECUTION .\r\n" +
                                                  "\r\n" +
                                                  "    --- Generated variables\r\n" +
                                                  "    ${vars}\r\n" +
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
                                                  "    var X Y : Oid .\r\n" +
                                                  "    var T : MSet . --- tokens\r\n" +
                                                  "    var SIG : MSet . --- signals\r\n" +
                                                  "    var M : MSet . --- messages\r\n" +
                                                  "    var T1 : NeMSet .\r\n" +
                                                  "    var S : Configuration .\r\n" +
                                                  "    var State : ProcessState .\r\n" +
                                                  "\r\n" +
                                                  "\r\n" +
                                                  "    op allTerminated : -> Prop .\r\n" +
                                                  "    eq < X : BPMNSystem | messages : M, processes : (< Y : " +
                                                  "ProcessSnapshot | tokens : T, signals : SIG, subprocesses : S, " +
                                                  "state : Running > C) > |= allTerminated = false .\r\n" +
                                                  "    eq < X : BPMNSystem | messages : M, processes : (C) > |= " +
                                                  "allTerminated = true [owise] .\r\n" +
                                                  "\r\n" +
                                                  "    op unsafe : -> Prop .\r\n" +
                                                  "    eq < X : BPMNSystem | messages : M, processes : (< Y : " +
                                                  "ProcessSnapshot | tokens : (T1 T1 T), signals : SIG, subprocesses " +
                                                  ": S, state : State > C) > |= unsafe = true .\r\n" +
                                                  "    eq < X : BPMNSystem | messages : M, processes : (C) > |= " +
                                                  "unsafe = false [owise] .\r\n" +
                                                  "\r\n" +
                                                  "    --- Generated atomic propositions\r\n" +
                                                  "    ${atomicPropositions}\r\n" +
                                                  "endm\r\n" +
                                                  "\r\n" +
                                                  "mod BPMN-CHECK is\r\n" +
                                                  "    pr BPMN-PREDS .\r\n" +
                                                  "    pr MODEL-CHECKER .\r\n" +
                                                  "    pr LTL-SIMPLIFIER .\r\n" +
                                                  "\r\n" +
                                                  "    var X : Configuration .\r\n" +
                                                  "endm\r\n" +
                                                  "\r\n" +
                                                  "${finalQuery} .\r\n";

    public BPMNToMaudeTransformer(BPMNCollaboration collaboration, MaudeBPMNGenerationSettings settings) {
        this.collaboration = collaboration;
        this.settings = settings;
        objectBuilder = new MaudeObjectBuilder();
        ruleBuilder = new BPMNMaudeRuleBuilder(collaboration);
        ruleBuilder.addVar(TOKENS, MSET, ANY_TOKENS);
        ruleBuilder.addVar(SIGNALS, MSET, ANY_SIGNALS);
        ruleBuilder.addVar(MESSAGES, MSET, ANY_MESSAGES);
        ruleBuilder.addVar(SUBPROCESSES, CONFIGURATION, ANY_SUBPROCESSES);
        ruleBuilder.addVar(PROCESSES, CONFIGURATION, ANY_PROCESS);
    }

    public String generate(String finalQuery) {
        Map<String, String> substitutionValues = new HashMap<>();
        substitutionValues.put("name", collaboration.getName());
        substitutionValues.put("init", this.makeInit());
        substitutionValues.put("rules", this.makeRules());
        substitutionValues.put("vars", ruleBuilder.getVars());
        substitutionValues.put("atomicPropositions", "--- no propositions"); // Add at some point
        substitutionValues.put("finalQuery", finalQuery);
        return new StringSubstitutor(substitutionValues).replace(MODULE_TEMPLATE);
    }

    private String makeInit() {
        String processes = collaboration.getParticipants().stream()
                                        .filter(process -> process.getStartEvents().stream().anyMatch(startEvent ->
                                                                                                              startEvent.getType() ==
                                                                                                              StartEventType.NONE))
                                        .map(process -> {
                                            MaudeObject maudeObject =
                                                    createProcessSnapshotObjectNoSubProcessAndSignals(
                                                            process,
                                                            this.createStartTokens(process));
                                            return maudeObject.generateObjectString();
                                        })
                                        .collect(Collectors.joining(NEW_LINE));
        return ruleBuilder.createBPMNSystem(processes, NONE).generateObjectString();
    }

    private String createStartTokens(Process process) {
        // Add a token for each none start event
        return process.getStartEvents().stream()
                      .filter(startEvent -> startEvent.getType() == StartEventType.NONE)
                      .map(this::getStartEventTokenName)
                      .collect(Collectors.joining(WHITE_SPACE));
    }

    private String makeRules() {
        new BPMNMaudeRuleGenerator(ruleBuilder, collaboration, settings).generateRules();

        return ruleBuilder.createdRules()
                          .map(MaudeRule::generateRuleString)
                          .collect(Collectors.joining(NEW_LINE));
    }

    @Override
    public BPMNMaudeRuleBuilder getRuleBuilder() {
        return ruleBuilder;
    }

    @Override
    public MaudeObjectBuilder getObjectBuilder() {
        return objectBuilder;
    }

    @Override
    public BPMNCollaboration getCollaboration() {
        return collaboration;
    }

    @Override
    public MaudeBPMNGenerationSettings getSettings() {
        return settings;
    }
}
