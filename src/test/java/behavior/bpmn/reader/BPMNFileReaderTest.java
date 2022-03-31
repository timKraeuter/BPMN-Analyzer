package behavior.bpmn.reader;

import behavior.bpmn.BPMNCollaboration;
import behavior.bpmn.Process;
import behavior.bpmn.SequenceFlow;
import com.google.common.collect.Sets;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.LinkedHashSet;
import java.util.Set;
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

    private BPMNCollaboration readModelFromResource(String name) {
        @SuppressWarnings("ConstantConditions") File model = new File(this.getClass().getResource(name).getFile());
        BPMNFileReader bpmnFileReader = new BPMNFileReader();
        return bpmnFileReader.readModelFromFile(model);
    }
}