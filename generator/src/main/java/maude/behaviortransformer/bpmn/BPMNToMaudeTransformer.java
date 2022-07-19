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
                                                  "\r\n" +
                                                  "    ops Running, Terminated : -> ProcessState [ctor] .\r\n" +
                                                  "    op tokens :_ : MSet -> Attribute [ctor].\r\n" +
                                                  "    op subprocesses :_ : Configuration -> Attribute [ctor].\r\n" +
                                                  "    op state :_ : ProcessState -> Attribute [ctor].\r\n" +
                                                  "    op ProcessSnapshot : -> Cid [ctor] .\r\n" +
                                                  "    subsort String < Oid .\r\n" +
                                                  "\r\n" +
                                                  "    var P : String .\r\n" +
                                                  "\r\n" +
                                                  "    rl [terminateProcess] :\r\n" +
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
                                                  "    var T : MSet .\r\n" +
                                                  "    var S : Configuration .\r\n" +
                                                  "\r\n" +
                                                  "    --- Generated rules\r\n" +
                                                  "    ${rules}\r\n" +
                                                  "\r\n" +
                                                  "    --- Start configuration which would be generated\r\n" +
                                                  "    op init : -> Configuration .\r\n" +
                                                  "    eq init = ${init} .\r\n" +
                                                  "endm\r\n" +
                                                  "\r\n" +
                                                  "rew [50] init .\r\n";

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
                            .collect(Collectors.joining("\r\n    "));
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
                          .collect(Collectors.joining("\r\n    "));
    }
}
