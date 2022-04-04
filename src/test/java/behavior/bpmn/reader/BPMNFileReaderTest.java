package behavior.bpmn.reader;

import behavior.bpmn.BPMNCollaboration;
import behavior.bpmn.FlowNode;
import behavior.bpmn.Process;
import behavior.bpmn.SequenceFlow;
import behavior.bpmn.events.*;
import com.google.common.collect.Sets;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class BPMNFileReaderTest {

    @Test
    void readTasks() {
        BPMNCollaboration result = readModelFromResource("/bpmn/bpmnModels/tasks.bpmn");

        // Expect the model shown here: https://cawemo.com/share/882d7c5b-bff0-4244-a39f-a234795035e5
        assertNotNull(result);
        // No pools so only one participant.
        assertThat(result.getParticipants().size(), is(1));
        Process participant = result.getParticipants().iterator().next();

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
    }

    @Test
    void readGateways() {
        BPMNCollaboration result = readModelFromResource("/bpmn/bpmnModels/gateways.bpmn");

        // Expect the model shown here: https://cawemo.com/share/bfb5f9e4-1b24-4ff7-bee1-278ea6aa80bc
        assertNotNull(result);
        // No pools so only one participant.
        assertThat(result.getParticipants().size(), is(1));
        Process participant = result.getParticipants().iterator().next();

        assertThat(participant.getSequenceFlows().count(), is(2L));
        assertThat(participant.getControlFlowNodes().count(), is(3L));
        // Sequence flows between the right flow nodes.
        Set<String> sequenceFlowIds = participant.getSequenceFlows()
                                                 .map(SequenceFlow::getID)
                                                 .collect(Collectors.toCollection(LinkedHashSet::new));
        assertThat(
                sequenceFlowIds,
                is(Sets.newHashSet(
                        "parallel gateway_exclusive gateway",
                        "exclusive gateway_event gateway")));
    }

    @Test
    void readEvents() {
        BPMNCollaboration result = readModelFromResource("/bpmn/bpmnModels/events.bpmn");

        // Expect the model shown here: https://cawemo.com/share/19b961cd-d4e2-4af8-8994-2e43e7ed094b
        assertNotNull(result);
        // No pools so only one participant.
        assertThat(result.getParticipants().size(), is(1));
        Process participant = result.getParticipants().iterator().next();

        assertThat(participant.getSequenceFlows().count(), is(14L));
        assertThat(participant.getControlFlowNodes().count(), is(17L));
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
        assertThat(flowNodes.get(signalStartEventName),
                is(new StartEvent(
                        signalStartEventName,
                        StartEventType.SIGNAL,
                        new EventDefinition(signalStartEventName))));

        // Check intermediate events
        String intermediateEventName = "intermediateEvent";
        assertThat(flowNodes.get(intermediateEventName), is(new IntermediateThrowEvent(
                intermediateEventName,
                // TODO: Should be type none! But that does not exist yet.
                IntermediateThrowEventType.MESSAGE)));
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
        assertThat(flowNodes.get(signalCEventName),
                is(new IntermediateCatchEvent(
                        signalCEventName,
                        IntermediateCatchEventType.SIGNAL,
                        new EventDefinition(signalCEventName))));
        String signalTEventName = "signalTEvent";
        assertThat(flowNodes.get(signalTEventName),
                is(new IntermediateThrowEvent(
                        signalTEventName,
                        IntermediateThrowEventType.SIGNAL,
                        new EventDefinition(signalTEventName))));
        String linkTEventName = "linkTEvent";
        assertThat(flowNodes.get(linkTEventName),
                is(new IntermediateThrowEvent(linkTEventName, IntermediateThrowEventType.LINK)));

        // Check end events
        String endEventName = "end";
        assertThat(flowNodes.get(endEventName), is(new EndEvent(endEventName)));
        String messageEndEventName = "messageEnd";
        assertThat(flowNodes.get(messageEndEventName), is(new EndEvent(messageEndEventName, EndEventType.MESSAGE)));
        String signalEndEventName = "signalEnd";
        assertThat(flowNodes.get(signalEndEventName),
                is(new EndEvent(signalEndEventName, EndEventType.SIGNAL, new EventDefinition(signalEndEventName))));
        String terminateEndEventName = "terminateEnd";
        assertThat(flowNodes.get(terminateEndEventName), is(new EndEvent(terminateEndEventName, EndEventType.TERMINATION)));
    }

    private BPMNCollaboration readModelFromResource(String name) {
        @SuppressWarnings("ConstantConditions") File model = new File(this.getClass().getResource(name).getFile());
        BPMNFileReader bpmnFileReader = new BPMNFileReader();
        return bpmnFileReader.readModelFromFile(model);
    }
}