package behavior.bpmn.reader;

import behavior.bpmn.Process;
import behavior.bpmn.*;
import behavior.bpmn.activities.tasks.ReceiveTask;
import behavior.bpmn.events.*;
import com.google.common.collect.Sets;
import org.junit.jupiter.api.Test;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class BPMNFileReaderTest implements BPMNFileReaderTestHelper {

    public static final String BPMN_BPMN_MODELS_READER_TEST = "/bpmn/bpmnModelsReaderTest/";

    @Test
    void readTasks() {
        BPMNCollaboration result = readModelFromResource(BPMN_BPMN_MODELS_READER_TEST + "tasks.bpmn");

        // Expect the model shown here: https://cawemo.com/share/882d7c5b-bff0-4244-a39f-a234795035e5
        assertNotNull(result);
        String name = "tasks";
        assertThat(result.getName(), is(name));
        // No pools so only one participant.
        assertThat(result.getParticipants().size(), is(1));
        Process participant = result.getParticipants().iterator().next();
        assertThat(participant.getName(), is(name));

        assertThat(participant.getSequenceFlows().count(), is(11L));
        assertThat(participant.getControlFlowNodes().count(), is(12L));
        // Sequence flows between the right flow nodes.
        Set<String> sequenceFlowIds = participant.getSequenceFlows()
                                                 .map(SequenceFlow::getID)
                                                 .collect(Collectors.toCollection(LinkedHashSet::new));
        assertThat(
                sequenceFlowIds,
                is(Sets.newHashSet(
                        "start_task",
                        "task_sendTask",
                        "sendTask_receiveTask",
                        "receiveTask_userTask",
                        "userTask_manualTask",
                        "manualTask_businessRTask",
                        "businessRTask_serviceTask",
                        "serviceTask_scriptTask",
                        "scriptTask_callActivity",
                        "callActivity_subprocess",
                        "subprocess_end")));

        // Check instantiate receive task
        Map<String, FlowNode> flowNodes = participant.getControlFlowNodes()
                                                     .collect(Collectors.toMap(
                                                             FlowNode::getName,
                                                             Function.identity()));
        String receiveTaskName = "receiveTask";
        FlowNode instantiateReceiveTask = flowNodes.get(receiveTaskName);
        // Instantiate must be true!
        assertThat(instantiateReceiveTask, is(new ReceiveTask(receiveTaskName, true)));
    }

    @Test
    void readGateways() {
        BPMNCollaboration result = readModelFromResource(BPMN_BPMN_MODELS_READER_TEST + "gateways.bpmn");

        // Expect the model shown here: https://cawemo.com/share/bfb5f9e4-1b24-4ff7-bee1-278ea6aa80bc
        assertNotNull(result);
        String name = "gateways";
        assertThat(result.getName(), is(name));
        // No pools so only one participant.
        assertThat(result.getParticipants().size(), is(1));
        Process participant = result.getParticipants().iterator().next();
        assertThat(participant.getName(), is(name));

        assertThat(participant.getSequenceFlows().count(), is(3L));
        assertThat(participant.getControlFlowNodes().count(), is(4L));
        // Sequence flows between the right flow nodes.
        Set<String> sequenceFlowIds = participant.getSequenceFlows()
                                                 .map(SequenceFlow::getID)
                                                 .collect(Collectors.toCollection(LinkedHashSet::new));
        assertThat(
                sequenceFlowIds,
                is(Sets.newHashSet("inclusive gateway_parallel gateway",
                        "parallel gateway_exclusive gateway",
                        "exclusive gateway_event gateway")));
    }

    @Test
    void readEvents() {
        BPMNCollaboration result = readModelFromResource(BPMN_BPMN_MODELS_READER_TEST + "events.bpmn");

        // Expect the model shown here: https://cawemo.com/share/19b961cd-d4e2-4af8-8994-2e43e7ed094b
        assertNotNull(result);
        String name = "events";
        assertThat(result.getName(), is(name));
        // No pools so only one participant.
        assertThat(result.getParticipants().size(), is(1));
        Process participant = result.getParticipants().iterator().next();
        assertThat(participant.getName(), is(name));

        assertThat(participant.getSequenceFlows().count(), is(15L));
        assertThat(participant.getControlFlowNodes().count(), is(18L));
        // Sequence flows between the right flow nodes.
        Set<String> sequenceFlowIds = participant.getSequenceFlows()
                                                 .map(SequenceFlow::getID)
                                                 .collect(Collectors.toCollection(LinkedHashSet::new));
        assertThat(
                sequenceFlowIds,
                is(Sets.newHashSet(
                        "start_e1",
                        "messageStart_e1",
                        "signalStart_e1",
                        "e3_messageEnd",
                        "e3_signalEnd",
                        "e3_terminateEnd",
                        "e3_end",
                        "linkCEvent_e2",
                        "timerCEvent_e2",
                        "intermediateEvent_e2",
                        "messageCEvent_e2",
                        "messageTEvent_e2",
                        "e2_linkTEvent",
                        "signalCEvent_e2",
                        "signalTEvent_e2")));

        Map<String, FlowNode> flowNodes = participant.getControlFlowNodes()
                                                     .collect(Collectors.toMap(
                                                             FlowNode::getName,
                                                             Function.identity()));
        // Check start events
        String startEventName = "start";
        assertThat(flowNodes.get(startEventName), is(new StartEvent(startEventName)));
        String messageStartEventName = "messageStart";
        assertThat(flowNodes.get(messageStartEventName),
                is(new StartEvent(messageStartEventName, StartEventType.MESSAGE)));
        String signalStartEventName = "signalStart";
        String startEndSignalEventDefinition = "Signal_113rm6n";
        assertThat(flowNodes.get(signalStartEventName),
                is(new StartEvent(
                        signalStartEventName,
                        StartEventType.SIGNAL,
                        new EventDefinition(startEndSignalEventDefinition))));

        // Check intermediate events
        String intermediateEventName = "intermediateEvent";
        assertThat(flowNodes.get(intermediateEventName), is(new IntermediateThrowEvent(
                intermediateEventName,
                IntermediateThrowEventType.NONE)));
        String messageCEventName = "messageCEvent";
        assertThat(flowNodes.get(messageCEventName),
                is(new IntermediateCatchEvent(messageCEventName, IntermediateCatchEventType.MESSAGE)));
        String messageTEventName = "messageTEvent";
        assertThat(flowNodes.get(messageTEventName),
                is(new IntermediateThrowEvent(messageTEventName, IntermediateThrowEventType.MESSAGE)));
        String linkCEventName = "linkCEvent";
        assertThat(flowNodes.get(linkCEventName),
                is(new IntermediateCatchEvent(linkCEventName, IntermediateCatchEventType.LINK)));
        String signalCEventName = "signalCEvent";
        String intermediateSignalEventDefinition = "Signal_1ni52ju";
        assertThat(flowNodes.get(signalCEventName),
                is(new IntermediateCatchEvent(
                        signalCEventName,
                        IntermediateCatchEventType.SIGNAL,
                        new EventDefinition(intermediateSignalEventDefinition))));
        String signalTEventName = "signalTEvent";
        assertThat(flowNodes.get(signalTEventName),
                is(new IntermediateThrowEvent(
                        signalTEventName,
                        IntermediateThrowEventType.SIGNAL,
                        new EventDefinition(intermediateSignalEventDefinition))));
        String linkTEventName = "linkTEvent";
        assertThat(flowNodes.get(linkTEventName),
                is(new IntermediateThrowEvent(linkTEventName, IntermediateThrowEventType.LINK)));
        String timerCEventName = "timerCEvent";
        assertThat(flowNodes.get(timerCEventName),
                is(new IntermediateCatchEvent(timerCEventName, IntermediateCatchEventType.TIMER)));

        // Check end events
        String endEventName = "end";
        assertThat(flowNodes.get(endEventName), is(new EndEvent(endEventName)));
        String messageEndEventName = "messageEnd";
        assertThat(flowNodes.get(messageEndEventName), is(new EndEvent(messageEndEventName, EndEventType.MESSAGE)));
        String signalEndEventName = "signalEnd";
        assertThat(flowNodes.get(signalEndEventName),
                is(new EndEvent(
                        signalEndEventName,
                        EndEventType.SIGNAL,
                        new EventDefinition(startEndSignalEventDefinition))));
        String terminateEndEventName = "terminateEnd";
        assertThat(flowNodes.get(terminateEndEventName), is(new EndEvent(terminateEndEventName, EndEventType.TERMINATION)));
    }

    @Test
    void readPoolsAndMessageFlows() {
        BPMNCollaboration result = readModelFromResource(BPMN_BPMN_MODELS_READER_TEST + "pools-message-flows.bpmn");

        // Expect the model shown here: https://cawemo.com/share/a7b1034d-da01-4afd-afdc-26cfdb33ef06
        assertNotNull(result);
        assertThat(result.getName(), is("pools-message-flows"));
        // Two pools = two participants.
        assertThat(result.getParticipants().size(), is(2));
        Iterator<Process> it = result.getParticipants().iterator();

        // Check p1
        Process participant1 = it.next();
        assertThat(participant1.getName(), is("p1"));
        assertThat(participant1.getSequenceFlows().count(), is(3L));
        assertThat(participant1.getControlFlowNodes().count(), is(4L));

        // Check p2
        Process participant2 = it.next();
        assertThat(participant2.getName(), is("p2"));
        assertThat(participant2.getSequenceFlows().count(), is(3L));
        assertThat(participant2.getControlFlowNodes().count(), is(4L));

        // Check message flows
        assertThat(result.getMessageFlows().size(), is(3));
        Set<String> messageFlowNames = result.getMessageFlows().stream()
                                             .map(MessageFlow::getName)
                                             .collect(Collectors.toSet());
        assertThat(messageFlowNames, is(Sets.newHashSet("sendEvent_startP2", "SendTask_receiveEvent", "endP1_ReceiveTask")));
    }

    @Test
    void readContainedSubprocess() {
        BPMNCollaboration result = readModelFromResource(BPMN_BPMN_MODELS_READER_TEST + "call-activity-simple.bpmn");

        // Expect the model shown here: https://cawemo.com/share/be4afe81-4adf-4b45-a933-5745316ee533
        assertNotNull(result);
        assertThat(result.getName(), is("call-activity-simple"));
        assertThat(result.getParticipants().size(), is(1));
        Process participant = result.getParticipants().iterator().next();
        assertThat(participant.getName(), is("process1"));

        assertThat(participant.getControlFlowNodes().count(), is(2L));
        Map<String, FlowNode> flowNodes = participant.getControlFlowNodes()
                                                     .collect(Collectors.toMap(
                                                             FlowNode::getName,
                                                             Function.identity()));
        FlowNode instantiateReceiveTask = flowNodes.get("A");
        // Instantiate must be true!
        assertThat(instantiateReceiveTask, is(new ReceiveTask("A", true)));
    }
}