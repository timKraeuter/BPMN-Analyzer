package groove.behaviorTransformer.bpmn.generators;

import behavior.bpmn.Process;
import behavior.bpmn.*;
import behavior.bpmn.auxiliary.AbstractProcessVisitor;
import behavior.bpmn.events.StartEvent;
import groove.behaviorTransformer.bpmn.BPMNToGrooveTransformerHelper;
import groove.graph.GrooveNode;
import groove.graph.rule.GrooveRuleBuilder;

import java.util.Set;

import static groove.behaviorTransformer.bpmn.BPMNToGrooveTransformerHelper.addOutgoingTokensForFlowNodeToProcessInstance;
import static groove.behaviorTransformer.bpmn.BPMNToGrooveTransformerHelper.createProcessInstanceAndAddTokens;

public class BPMNEventRuleGeneratorImpl implements BPMNEventRuleGenerator {
    private final BPMNCollaboration collaboration;
    private final GrooveRuleBuilder ruleBuilder;

    public BPMNEventRuleGeneratorImpl(BPMNCollaboration collaboration, GrooveRuleBuilder ruleBuilder) {
        this.collaboration = collaboration;
        this.ruleBuilder = ruleBuilder;
    }

    @Override
    public void createStartEventRulesForProcess(AbstractProcess process, StartEvent startEvent) {
        process.accept(new AbstractProcessVisitor() {
            @Override
            public void handle(EventSubprocess eventSubprocess) {
                // Handled elsewhere for event subprocesses.
            }

            @Override
            public void handle(Process process) {
                createStartEventRule(startEvent, process);
            }
        });
    }

    void createStartEventRule(StartEvent startEvent, Process process) {
        switch (startEvent.getType()) {
            case NONE:
                createNoneStartEventRule(startEvent, ruleBuilder, process);
                break;
            case MESSAGE:
                createMessageStartEventRule(startEvent, ruleBuilder, collaboration);
                break;
            case SIGNAL:
                // Done in the corresponding throw rule.
                break;
        }
    }

    private void createNoneStartEventRule(StartEvent startEvent, GrooveRuleBuilder ruleBuilder, Process process) {
        ruleBuilder.startRule(startEvent.getName());
        GrooveNode processInstance = createProcessInstanceAndAddTokens(startEvent, ruleBuilder, process);
        BPMNToGrooveTransformerHelper.deleteTokenWithPosition(ruleBuilder,
                                                              processInstance,
                                                              getStartEventTokenName(process));
        ruleBuilder.buildRule();
    }

    private String getStartEventTokenName(Process process) {
        return process.getName() + "_" + process.getStartEvent().getName();
    }

    private void createMessageStartEventRule(StartEvent startEvent,
                                             GrooveRuleBuilder ruleBuilder,
                                             BPMNCollaboration collaboration) {
        Set<MessageFlow> incomingMessageFlows = collaboration.getIncomingMessageFlows(startEvent);
        incomingMessageFlows.forEach(incomingMessageFlow -> {
            ruleBuilder.startRule(incomingMessageFlows.size() > 1 ? incomingMessageFlow.getName() :
                                          startEvent.getName());
            GrooveNode processInstance = BPMNToGrooveTransformerHelper.deleteIncomingMessageAndCreateProcessInstance(
                    incomingMessageFlow,
                    collaboration,
                    ruleBuilder);
            addOutgoingTokensForFlowNodeToProcessInstance(startEvent, ruleBuilder, processInstance);
            ruleBuilder.buildRule();
        });
    }
}
