package maude.behaviortransformer.bpmn;

import static groove.behaviortransformer.bpmn.BPMNToGrooveTransformerConstants.*;
import static maude.behaviortransformer.bpmn.BPMNToMaudeTransformerConstants.*;

import behavior.bpmn.BPMNCollaboration;
import behavior.bpmn.BPMNProcess;
import behavior.bpmn.events.StartEventType;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import maude.behaviortransformer.bpmn.settings.MaudeBPMNGenerationSettings;
import maude.generation.BPMNMaudeRuleBuilder;
import maude.generation.MaudeObject;
import maude.generation.MaudeObjectBuilder;
import maude.generation.MaudeRule;
import org.apache.commons.text.StringSubstitutor;

public class BPMNToMaudeTransformer implements BPMNToMaudeTransformerHelper {
  private final BPMNCollaboration collaboration;
  private final BPMNMaudeRuleBuilder ruleBuilder;
  private final MaudeBPMNGenerationSettings settings;
  private final MaudeObjectBuilder objectBuilder;

  private static final String MODULE_TEMPLATE =
      """
load model-checker.maude .\r
\r
--- Multiset implementation could be extracted as well.\r
fmod MSET is\r
    pr STRING .\r
    pr NAT .\r
    sorts NeMSet MSet .\r
    subsort String < NeMSet < MSet .\r
\r
    op none : -> MSet [ctor] .\r
    op __ : MSet MSet -> MSet [ctor assoc comm id: none] .\r
    op __ : NeMSet MSet -> NeMSet [ctor ditto] .\r
    op __ : MSet NeMSet -> NeMSet [ctor ditto] .\r
\r
    op contains : MSet String -> Bool .\r
\r
    var X : String .\r
    var Y : String .\r
    var S S1 : MSet .\r
\r
    eq contains(none, X) = false .\r
    eq contains(X S, X) = true .\r
    ceq contains(Y S, X) = contains(S, X) if X =/= Y .\r
\r
    op size : MSet -> Nat .\r
    eq size(none) = 0 .\r
    eq size(X S) = size(S) + 1 .\r
\r
    op lessOrEqualSize : MSet MSet -> Bool .\r
    ceq lessOrEqualSize(S, S1) = true if size(S) <= size(S1) .\r
    eq lessOrEqualSize(S, S1) = false [owise] .\r
endfm\r
\r
mod BPMN-EXECUTION is\r
    pr MSET .\r
    pr STRING .\r
    pr CONFIGURATION .\r
\r
    sort ProcessState .\r
    subsort String < Oid .\r
\r
    --- BPMNSystem\r
    op BPMNSystem : -> Cid [ctor] .\r
    op messages :_ : MSet -> Attribute [ctor] .\r
    op processes :_ : Configuration -> Attribute [ctor] .\r
\r
    --- Processes\r
    op ProcessSnapshot : -> Cid [ctor] .\r
    op name :_ : String -> Attribute [ctor] .\r
    op tokens :_ : MSet -> Attribute [ctor] .\r
    op signals :_ : MSet -> Attribute [ctor] .\r
    op subprocesses :_ : Configuration -> Attribute [ctor] .\r
    ops Running, Terminated : -> ProcessState [ctor] .\r
    op state :_ : ProcessState -> Attribute [ctor] .\r
\r
    --- Auxiliary\r
    op signalAll : Configuration MSet -> Configuration .\r
    op signal : MSet MSet -> MSet .\r
    op terminate : Configuration -> Configuration .\r
\r
    var X : Oid .\r
    vars P P1 : String .\r
    vars T T1 : MSet . --- tokens\r
    vars SIG : MSet . --- signals\r
    vars S : Configuration . --- subprocesses\r
    vars STATE : ProcessState . --- state\r
    var PS : Configuration .\r
\r
    eq signalAll(none, T) = none .\r
    ceq signalAll(< X : ProcessSnapshot | name : P, tokens : T, signals : SIG, subprocesses : S, state : STATE > PS, T1) = < X : ProcessSnapshot | name : P, tokens : T, signals : (SIG signal(T, T1)), subprocesses : signalAll(S, T1), state : STATE > signalAll(PS, T1) if lessOrEqualSize(SIG, T1). --- maximum signal bound is the number of tokens.\r
    eq signalAll(< X : ProcessSnapshot | name : P, tokens : T, signals : SIG, subprocesses : S, state : STATE > PS, T1) = < X : ProcessSnapshot | name : P, tokens : T, signals : (SIG), subprocesses : signalAll(S, T1), state : STATE > signalAll(PS, T1) [owise] .\r
\r
    ceq signal(P T, T1) = (P + "_signal") signal(T, T1) if contains(T1, P) .\r
    eq signal(P T, T1) = signal(T, T1) [owise] .\r
    eq signal(none, T1) = none .\r
\r
    eq terminate(none) = none .\r
    --- NOOP if already terminated\r
    eq terminate(< X : ProcessSnapshot | name : P, tokens : T, signals : SIG, subprocesses : S, state : Terminated >) = < X : ProcessSnapshot | name : P, tokens : T, signals : SIG, subprocesses : S, state : Terminated > .\r
    --- Terminate all subprocesses recursively\r
    eq terminate(< X : ProcessSnapshot | name : P, tokens : T, signals : SIG, subprocesses : S, state : STATE > PS) = < X : ProcessSnapshot | name : P, tokens : T, signals : SIG, subprocesses : terminate(S), state : Terminated > terminate(PS) .\r
\r
    rl [naturalTerminate] :\r
    < X : ProcessSnapshot | name : P, tokens : none, signals : SIG, subprocesses : none, state : Running >\r
                            =>\r
    < X : ProcessSnapshot | name : P, tokens : none, signals : none, subprocesses : none, state : Terminated > .\r
endm\r
\r
mod BPMN-EXECUTION-${name} is\r
    pr BPMN-EXECUTION .\r
\r
    --- Generated variables\r
    ${vars}\r
\r
    --- Generated rules\r
    ${rules}\r
\r
    --- Start configuration which would be generated\r
    op init : -> Configuration .\r
    eq init = ${init} .\r
endm\r
\r
mod BPMN-PREDS is\r
    pr BPMN-EXECUTION-${name} .\r
    pr SATISFACTION .\r
    subsort Configuration < State .\r
\r
    var C : Configuration .\r
    var P : Prop .\r
    var X Y : Oid .\r
    var N : String . --- name\r
    var T : MSet . --- tokens\r
    var SIG : MSet . --- signals\r
    var M : MSet . --- messages\r
    var T1 : NeMSet .\r
    var S : Configuration .\r
    var State : ProcessState .\r
\r
\r
    op allTerminated : -> Prop .\r
    eq < X : BPMNSystem | messages : M, processes : (< Y : ProcessSnapshot | name : N, tokens : T, signals : SIG, subprocesses : S, state : Running > C) > |= allTerminated = false .\r
    eq < X : BPMNSystem | messages : M, processes : (C) > |= allTerminated = true [owise] .\r
\r
    op unsafe : -> Prop .\r
    eq < X : BPMNSystem | messages : M, processes : (< Y : ProcessSnapshot | name : N, tokens : (T1 T1 T), signals : SIG, subprocesses : S, state : State > C) > |= unsafe = true .\r
    eq < X : BPMNSystem | messages : M, processes : (C) > |= unsafe = false [owise] .\r
\r
    --- Generated atomic propositions\r
    ${atomicPropositions}\r
endm\r
\r
mod BPMN-CHECK is\r
    pr BPMN-PREDS .\r
    pr MODEL-CHECKER .\r
    pr LTL-SIMPLIFIER .\r
\r
    var X : Configuration .\r
endm\r
\r
${finalQuery} .\r
""";

  public BPMNToMaudeTransformer(
      BPMNCollaboration collaboration, MaudeBPMNGenerationSettings settings) {
    this.collaboration = collaboration;
    this.settings = settings;
    objectBuilder = new MaudeObjectBuilder();
    ruleBuilder = new BPMNMaudeRuleBuilder(collaboration, settings);
    ruleBuilder.addVar(OIDS, OID, O + 0);
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
    AtomicLong oidCounter = new AtomicLong(0);
    String processes =
        collaboration.getParticipants().stream()
            .filter(
                process ->
                    process.getStartEvents().stream()
                        .anyMatch(startEvent -> startEvent.getType() == StartEventType.NONE))
            .map(
                process -> {
                  MaudeObject maudeObject =
                      createProcessSnapshotObjectNoSubProcessAndSignals(
                          process,
                          String.format(ENQUOTE_FORMAT, oidCounter.getAndIncrement()),
                          this.createStartTokens(process));
                  return maudeObject.generateObjectString();
                })
            .collect(Collectors.joining(NEW_LINE));
    return ruleBuilder.createBPMNSystem(processes, NONE).generateObjectString();
  }

  private String createStartTokens(BPMNProcess process) {
    // Add a token for each none start event
    return process.getStartEvents().stream()
        .filter(startEvent -> startEvent.getType() == StartEventType.NONE)
        .map(this::getOutgoingTokensForFlowNode)
        .collect(Collectors.joining(WHITE_SPACE));
  }

  private String makeRules() {
    new BPMNMaudeRuleGenerator(ruleBuilder, collaboration, settings).generateRules();

    return ruleBuilder
        .createdRules()
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
