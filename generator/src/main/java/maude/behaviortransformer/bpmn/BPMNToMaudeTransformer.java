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

public class BPMNToMaudeTransformer {
    private final BPMNCollaboration collaboration;
    private static final String MODULE_TEMPLATE = "load model-checker.maude .\n" +
                                                  "\n" +
                                                  "--- Multiset implementation could be extracted as well.\n" +
                                                  "fmod MSET is pr\n" +
                                                  "    STRING .\n" +
                                                  "    sorts NeMSet MSet .\n" +
                                                  "    subsort String < NeMSet < MSet .\n" +
                                                  "\n" +
                                                  "    op none : -> MSet [ctor] .\n" +
                                                  "    op __ : MSet MSet -> MSet [ctor assoc comm id: none] .\n" +
                                                  "    op __ : NeMSet MSet -> NeMSet [ctor ditto] .\n" +
                                                  "    op __ : MSet NeMSet -> NeMSet [ctor ditto] .\n" +
                                                  "endfm\n" +
                                                  "\n" +
                                                  "mod BPMN-EXECUTION is\n" +
                                                  "    pr MSET .\n" +
                                                  "    pr STRING .\n" +
                                                  "    pr CONFIGURATION .\n" +
                                                  "\n" +
                                                  "    sort ProcessState .\n" +
                                                  "\n" +
                                                  "    ops Running, Terminated : -> ProcessState [ctor] .\n" +
                                                  "    op tokens :_ : MSet -> Attribute [ctor].\n" +
                                                  "    op subprocesses :_ : Configuration -> Attribute [ctor].\n" +
                                                  "    op state :_ : ProcessState -> Attribute [ctor].\n" +
                                                  "    op ProcessSnapshot : -> Cid [ctor] .\n" +
                                                  "    subsort String < Oid .\n" +
                                                  "\n" +
                                                  "    var P : String .\n" +
                                                  "\n" +
                                                  "    rl [terminateProcess] :\n" +
                                                  "    < P : ProcessSnapshot | tokens : none, subprocesses : none, " +
                                                  "state : Running >\n" +
                                                  "                            =>\n" +
                                                  "    < P : ProcessSnapshot | tokens : none, subprocesses : none, " +
                                                  "state : Terminated > .\n" +
                                                  "endm\n" +
                                                  "\n" +
                                                  "mod BPMN-EXECUTION-${name} is\n" +
                                                  "    pr BPMN-EXECUTION .\n" +
                                                  "\n" +
                                                  "    var T : MSet .\n" +
                                                  "    var S : Configuration .\n" +
                                                  "\n" +
                                                  "    --- Generated rules\n" +
                                                  "    ${rules}\n" +
                                                  "\n" +
                                                  "    --- Start configuration which would be generated\n" +
                                                  "    op init : -> Configuration .\n" +
                                                  "    eq init = ${init} .\n" +
                                                  "endm\n" +
                                                  "\n" +
                                                  "rew [10] init .\n";

    public BPMNToMaudeTransformer(BPMNCollaboration collaboration) {
        this.collaboration = collaboration;
    }

    public String generate(String ltlQuery) {
        Map<String, String> substitutionValues = new HashMap<>();
        substitutionValues.put("name", collaboration.getName());
        substitutionValues.put("init", this.makeInit());
        substitutionValues.put("rules", this.makeRules());
        substitutionValues.put("ltlQuery", ltlQuery);
        return new StringSubstitutor(substitutionValues).replace(MODULE_TEMPLATE);
    }

    private String makeInit() {
        MaudeObjectBuilder maudeObjectBuilder = new MaudeObjectBuilder();
        return collaboration.getParticipants().stream()
                            .filter(process -> process.getStartEvents().stream().anyMatch(startEvent ->
                                                                                                  startEvent.getType() ==
                                                                                                  StartEventType.NONE))
                            .map(process -> {
                                MaudeObject maudeObject = BPMNToMaudeTransformerHelper.createProcessSnapshotObjectNoSubProcess(maudeObjectBuilder,
                                                                                                                               process,
                                                                                                                               this.createStartTokens(process));
                                return maudeObject.generateObjectString();
                            })
                            .collect(Collectors.joining("\n    "));
    }

    private String createStartTokens(Process process) {
        // Add a token for each none start event
        return process.getStartEvents().stream()
                      .filter(startEvent -> startEvent.getType() == StartEventType.NONE)
                      .map(BPMNToMaudeTransformerHelper::getStartEventTokenName)
                      .collect(Collectors.joining(" "));
    }

    private String makeRules() {
        MaudeRuleBuilder ruleBuilder = new MaudeRuleBuilder();

        new BPMNMaudeRuleGenerator(ruleBuilder, collaboration).generateRules();

        return ruleBuilder.createdRules()
                          .map(MaudeRule::generateRuleString)
                          .collect(Collectors.joining("\n    "));
    }
}
