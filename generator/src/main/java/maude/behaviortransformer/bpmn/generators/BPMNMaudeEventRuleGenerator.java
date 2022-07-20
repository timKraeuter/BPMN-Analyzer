package maude.behaviortransformer.bpmn.generators;

import behavior.bpmn.AbstractProcess;
import behavior.bpmn.SequenceFlow;
import behavior.bpmn.auxiliary.exceptions.BPMNRuntimeException;
import behavior.bpmn.events.EndEvent;
import behavior.bpmn.events.StartEvent;
import maude.generation.MaudeObjectBuilder;
import maude.generation.MaudeRuleBuilder;

import static maude.behaviortransformer.bpmn.BPMNToMaudeTransformerHelper.*;

public class BPMNMaudeEventRuleGenerator {
    private final MaudeRuleBuilder ruleBuilder;
    private final MaudeObjectBuilder objectBuilder;

    public BPMNMaudeEventRuleGenerator(MaudeRuleBuilder ruleBuilder) {
        this.ruleBuilder = ruleBuilder;
        this.objectBuilder = new MaudeObjectBuilder();
    }

    public void createStartEventRulesForProcess(AbstractProcess process, StartEvent startEvent) {
        switch (startEvent.getType()) {
            case NONE:
                createNoneStartEventRule(startEvent, process);
                break;
            case MESSAGE:
                createMessageStartEventRule(startEvent);
                break;
            case MESSAGE_NON_INTERRUPTING:
                // Implemented in the event subprocess rule generator.
                break;
            case SIGNAL:
                // Done in the corresponding throw rule.
                break;
            case SIGNAL_NON_INTERRUPTING:
                // Implemented in the throw part.
                break;
        }
    }

    private void createNoneStartEventRule(StartEvent startEvent, AbstractProcess process) {
        ruleBuilder.ruleName(getFlowNodeNameAndID(startEvent));
        String preToken = getStartEventTokenName(startEvent) + ANY_OTHER_TOKENS;
        String postToken = getOutgoingTokensForFlowNode(startEvent) + ANY_OTHER_TOKENS;
        ruleBuilder.addPreObject(createProcessSnapshotObjectAnySubProcess(objectBuilder, process, preToken));
        ruleBuilder.addPostObject(createProcessSnapshotObjectAnySubProcess(objectBuilder, process, postToken));
        ruleBuilder.build();
    }

    private void createMessageStartEventRule(StartEvent startEvent) {
        throw new UnsupportedOperationException();
    }

    public void createEndEventRule(AbstractProcess process, EndEvent endEvent) {
        if (endEvent.getIncomingFlows().count() != 1) {
            throw new BPMNRuntimeException("End events should have exactly one incoming flow!");
        }
        // Throw never reached due to check before.
        SequenceFlow incomingFlow = endEvent.getIncomingFlows().findFirst().orElseThrow();
        String preTokens = getTokenForSequenceFlow(incomingFlow) + ANY_OTHER_TOKENS;

        ruleBuilder.ruleName(getFlowNodeNameAndID(endEvent));
        ruleBuilder.addPreObject(createProcessSnapshotObjectAnySubProcess(objectBuilder, process, preTokens));

        switch (endEvent.getType()) {
            case NONE:
                ruleBuilder.addPostObject(createProcessSnapshotObjectAnySubProcess(objectBuilder, process, ANY_TOKEN));
                break;
            case TERMINATION:
                // TODO: Implement termination end event.
                break;
            case MESSAGE:
                // TODO: Implement message end event.
                break;
            case SIGNAL:
                // TODO: Implement signal end event.
                break;
        }
        ruleBuilder.build();
    }
}
