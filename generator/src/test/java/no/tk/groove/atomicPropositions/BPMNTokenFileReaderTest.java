package no.tk.groove.atomicPropositions;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.common.collect.Lists;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import no.tk.behavior.bpmn.reader.token.BPMNTokenFileReader;
import no.tk.behavior.bpmn.reader.token.model.BPMNProcessSnapshot;
import no.tk.behavior.bpmn.reader.token.model.ProcessSnapshot;
import no.tk.behavior.bpmn.reader.token.model.Token;
import no.tk.util.FileTestHelper;
import org.junit.jupiter.api.Test;

class BPMNTokenFileReaderTest {

  public static final String AP_TEST_PATH = "bpmn/atomicPropositions/";

  @Test
  void readSnapshotWithTokens() throws IOException {
    BPMNProcessSnapshot bpmnSnapshot = readBPMNSnapshotFromResource("snapshotWithTokens.xml");

    assertNotNull(bpmnSnapshot);
    assertThat(bpmnSnapshot.getName(), is("snapshotWithTokens"));

    // Check for tokens and process snapshots
    assertThat(bpmnSnapshot.getProcessSnapshots().size(), is(1));
    ProcessSnapshot processSnapshot = bpmnSnapshot.getProcessSnapshots().iterator().next();
    assertThat(processSnapshot.getSnapshotID(), is("ProcessSnapshot_1hrwrvp"));
    assertThat(processSnapshot.getProcessID(), is("Process_1"));
    assertThat(processSnapshot.getTokens().size(), is(3));
    assertTrue(processSnapshot.isShouldExist());
    assertThat(
        getElementIDsForTokens(processSnapshot),
        is(Lists.newArrayList("Flow_1df3b4l", "Activity_1iduxj0", "Flow_1agdsil")));
    assertThat(
        getShouldExistListForTokens(processSnapshot), is(Lists.newArrayList(true, true, false)));
  }

  @Test
  void readMultipleSnapshotWithTokens() throws IOException {
    BPMNProcessSnapshot bpmnSnapshot =
        readBPMNSnapshotFromResource("multipleSnapshotsWithTokens.xml");

    assertNotNull(bpmnSnapshot);
    assertThat(bpmnSnapshot.getName(), is("multipleSnapshotsWithTokens"));

    // Check for tokens and process snapshots
    assertThat(bpmnSnapshot.getProcessSnapshots().size(), is(3));

    // Check snapshot 1
    ProcessSnapshot snapshot1 = getSnapshotWithID(bpmnSnapshot, "ProcessSnapshot_1hrwrvp");
    assertThat(snapshot1.getSnapshotID(), is("ProcessSnapshot_1hrwrvp"));
    assertThat(snapshot1.getProcessID(), is("Process_1"));
    assertThat(snapshot1.getTokens().size(), is(2));
    assertTrue(snapshot1.isShouldExist());
    assertThat(
        getElementIDsForTokens(snapshot1), is(Lists.newArrayList("Flow_1df3b4l", "Flow_1agdsil")));
    assertThat(getShouldExistListForTokens(snapshot1), is(Lists.newArrayList(true, false)));

    // Check snapshot 2
    ProcessSnapshot snapshot2 = getSnapshotWithID(bpmnSnapshot, "ProcessSnapshot_01ch00c");
    assertThat(snapshot2.getSnapshotID(), is("ProcessSnapshot_01ch00c"));
    assertThat(snapshot2.getProcessID(), is("Process_1"));
    assertThat(snapshot2.getTokens().size(), is(1));
    assertTrue(snapshot2.isShouldExist());
    assertThat(getElementIDsForTokens(snapshot2), is(Lists.newArrayList("Activity_1iduxj0")));
    assertThat(getShouldExistListForTokens(snapshot2), is(Lists.newArrayList(true)));

    // Check snapshot 3
    ProcessSnapshot snapshot3 = getSnapshotWithID(bpmnSnapshot, "ProcessSnapshot_0jd6wrd");
    assertThat(snapshot3.getSnapshotID(), is("ProcessSnapshot_0jd6wrd"));
    assertThat(snapshot3.getProcessID(), is("Process_1"));
    assertThat(snapshot3.getTokens().size(), is(0));
    assertFalse(snapshot3.isShouldExist());
  }

  private static List<Boolean> getShouldExistListForTokens(ProcessSnapshot snapshot2) {
    return snapshot2.getTokens().stream().map(Token::isShouldExist).collect(Collectors.toList());
  }

  private static List<String> getElementIDsForTokens(ProcessSnapshot snapshot1) {
    return snapshot1.getTokens().stream().map(Token::getElementID).collect(Collectors.toList());
  }

  private static ProcessSnapshot getSnapshotWithID(BPMNProcessSnapshot bpmnSnapshot, String id) {
    return bpmnSnapshot.getProcessSnapshots().stream()
        .filter(processSnapshot -> processSnapshot.getSnapshotID().equals(id))
        .findFirst()
        .orElseThrow();
  }

  BPMNProcessSnapshot readBPMNSnapshotFromResource(String resourcePath) throws IOException {
    @SuppressWarnings("ConstantConditions")
    Path model = FileTestHelper.getResource(AP_TEST_PATH + resourcePath);
    BPMNTokenFileReader bpmnTokenFileReader = new BPMNTokenFileReader();
    return bpmnTokenFileReader.readModelFromFilePath(model);
  }
}
