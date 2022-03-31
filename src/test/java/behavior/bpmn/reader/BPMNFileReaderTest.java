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
        File model = new File(this.getClass().getResource("/bpmn/bpmnModels/tasks.bpmn").getFile());
        BPMNFileReader bpmnFileReader = new BPMNFileReader();
        BPMNCollaboration result = bpmnFileReader.readModelFromFile(model);

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
}