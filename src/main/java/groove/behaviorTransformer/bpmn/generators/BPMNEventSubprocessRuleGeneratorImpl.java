package groove.behaviorTransformer.bpmn.generators;

import behavior.bpmn.AbstractProcess;
import behavior.bpmn.BPMNCollaboration;
import behavior.bpmn.EventSubprocess;
import behavior.bpmn.MessageFlow;
import behavior.bpmn.events.StartEvent;
import groove.behaviorTransformer.bpmn.BPMNRuleGenerator;
import groove.behaviorTransformer.bpmn.BPMNToGrooveTransformerHelper;
import groove.graph.GrooveNode;
import groove.graph.rule.GrooveRuleBuilder;

import java.util.Set;

import static groove.behaviorTransformer.GrooveTransformerHelper.createStringNodeLabel;
import static groove.behaviorTransformer.bpmn.BPMNToGrooveTransformerConstants.*;

public class BPMNEventSubprocessRuleGeneratorImpl implements BPMNEventSubprocessRuleGenerator {
    private final BPMNRuleGenerator bpmnRuleGenerator;
    private final BPMNCollaboration collaboration;
    private final GrooveRuleBuilder ruleBuilder;

    public BPMNEventSubprocessRuleGeneratorImpl(BPMNRuleGenerator bpmnRuleGenerator,
                                                BPMNCollaboration collaboration,
                                                GrooveRuleBuilder ruleBuilder) {
        this.bpmnRuleGenerator = bpmnRuleGenerator;
        this.collaboration = collaboration;
        this.ruleBuilder = ruleBuilder;
    }

    @Override
    public void generateRulesForEventSubprocesses(AbstractProcess process) {
        process.getEventSubprocesses().forEach(eventSubprocess -> this.generateRulesForEventSubprocess(process,
                                                                                                       eventSubprocess));
    }


    private void generateRulesForEventSubprocess(AbstractProcess process, EventSubprocess eventSubprocess) {
        // Start event rule generation is special
        generateRulesForStartEvents(process, eventSubprocess, collaboration, ruleBuilder);
        // Standard rule generation for other elements.
        bpmnRuleGenerator.generateRulesForProcess(eventSubprocess);
        // Termination rule
        generateTerminateEventSubProcessRule(process, eventSubprocess, ruleBuilder);
    }

    private void generateTerminateEventSubProcessRule(AbstractProcess process,
                                                      EventSubprocess eventSubprocess,
                                                      GrooveRuleBuilder ruleBuilder) {
        String eSubprocessName = eventSubprocess.getName();
        ruleBuilder.startRule(eSubprocessName + END);
        GrooveNode processInstance = BPMNToGrooveTransformerHelper.createProcessInstanceWithName(process, ruleBuilder);
        bpmnRuleGenerator.deleteTerminatedSubprocess(ruleBuilder, eSubprocessName, processInstance);
        ruleBuilder.buildRule();
    }


    private void generateRulesForStartEvents(AbstractProcess process,
                                             EventSubprocess eventSubprocess,
                                             BPMNCollaboration collaboration,
                                             GrooveRuleBuilder ruleBuilder) {
        eventSubprocess.getStartEvents().forEach(startEvent -> {
            switch (startEvent.getType()) {
                case NONE:
                    throw new RuntimeException("None start events in event subprocesses are useless!");
                case MESSAGE:
                    // TODO: Implement interrupting behavior.
                    break;
                case MESSAGE_NON_INTERRUPTING:
                    createStartNonInterruptingEvenSubprocessFromMessageRules(process,
                                                                             eventSubprocess,
                                                                             collaboration,
                                                                             ruleBuilder,
                                                                             startEvent);

                    break;
                case SIGNAL:
                    // TODO: Implement interrupting behavior.
                    break;
                case SIGNAL_NON_INTERRUPTING:
                    // TODO: Implement.
                    break;
                default:
                    throw new RuntimeException("Unexpected start event type encountered: " + startEvent.getType());
            }
        });
    }

    private void createStartNonInterruptingEvenSubprocessFromMessageRules(AbstractProcess process,
                                                                          EventSubprocess eventSubprocess,
                                                                          BPMNCollaboration collaboration,
                                                                          GrooveRuleBuilder ruleBuilder,
                                                                          StartEvent startEvent) {
        Set<MessageFlow> incomingMessageFlows = collaboration.getIncomingMessageFlows(startEvent);
        incomingMessageFlows.forEach(incomingMessageFlow -> {
            ruleBuilder.startRule(incomingMessageFlows.size() > 1 ? incomingMessageFlow.getName() :
                                          startEvent.getName());
            // Needs a running parent process
            GrooveNode processInstance = BPMNToGrooveTransformerHelper.createContextRunningProcessInstance(process,
                                                                                                           ruleBuilder);

            // Start new subprocess instance of process
            GrooveNode eventSubProcessInstance = BPMNToGrooveTransformerHelper.createNewProcessInstance(ruleBuilder, eventSubprocess.getName());
            ruleBuilder.addEdge(SUBPROCESS, processInstance, eventSubProcessInstance);

            // Consumes the message
            GrooveNode message = ruleBuilder.deleteNode(TYPE_MESSAGE);
            ruleBuilder.deleteEdge(POSITION,
                                   message,
                                   ruleBuilder.contextNode(createStringNodeLabel(incomingMessageFlow.getName())));

            // Spawns a new token at each outgoing flow.
            BPMNToGrooveTransformerHelper.addOutgoingTokensForFlowNodeToProcessInstance(startEvent,
                                                                                        ruleBuilder,
                                                                                        eventSubProcessInstance);
            ruleBuilder.buildRule();
        });
    }
}
