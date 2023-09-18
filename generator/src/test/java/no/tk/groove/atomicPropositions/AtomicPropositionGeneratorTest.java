package no.tk.groove.atomicPropositions;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import no.tk.behavior.bpmn.BPMNCollaboration;
import no.tk.behavior.bpmn.BPMNProcess;
import no.tk.behavior.bpmn.FlowNode;
import no.tk.behavior.bpmn.SequenceFlow;
import no.tk.behavior.bpmn.activities.tasks.ReceiveTask;
import no.tk.reader.BPMNFileReaderTestHelper;
import com.google.common.collect.Sets;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

class AtomicPropositionGeneratorTest implements BPMNFileReaderTestHelper {

  public static final String AP_TEST_PATH = "bpmn/atomicPropositions/";

  @Test
  void generateSnapshots() throws IOException {
    BPMNCollaboration result = readModelFromResource(AP_TEST_PATH + "snapshots.xml");

    assertNotNull(result);
    String name = "tasks";
    assertThat(result.getName(), is(name));
    // No pools so only one participant.
    assertThat(result.getParticipants().size(), is(1));
    BPMNProcess participant = result.getParticipants().iterator().next();
    assertThat(participant.getName(), is(name));

    assertThat(participant.getSequenceFlows().count(), is(10L));
    assertThat(participant.getFlowNodes().count(), is(11L));
    // Sequence flows between the right flow nodes.
    Set<String> sequenceFlowIds = getSequenceFlowIdsForProcess(participant);
    assertThat(
        sequenceFlowIds,
        is(
            Sets.newHashSet(
                "start_task",
                "task_sendTask",
                "sendTask_receiveTask",
                "receiveTask_userTask",
                "userTask_manualTask",
                "manualTask_businessRTask",
                "businessRTask_serviceTask",
                "serviceTask_scriptTask",
                "scriptTask_subprocess",
                "subprocess_end")));

    // Check instantiate receive task
    Map<String, FlowNode> flowNodes = createFlowNodeNameToFlowNodeMap(participant);
    String receiveTaskName = "receiveTask";
    FlowNode instantiateReceiveTask = flowNodes.get(receiveTaskName);
    // Instantiate must be true!
    assertThat(
        instantiateReceiveTask, is(new ReceiveTask("Activity_0xr3zq4", receiveTaskName, true)));
  }

  private Set<String> getSequenceFlowIdsForProcess(BPMNProcess participant) {
    return participant
        .getSequenceFlows()
        .map(SequenceFlow::getDescriptiveName)
        .collect(Collectors.toCollection(LinkedHashSet::new));
  }

  private Map<String, FlowNode> createFlowNodeNameToFlowNodeMap(BPMNProcess participant) {
    return participant
        .getFlowNodes()
        .collect(Collectors.toMap(FlowNode::getName, Function.identity()));
  }
}
